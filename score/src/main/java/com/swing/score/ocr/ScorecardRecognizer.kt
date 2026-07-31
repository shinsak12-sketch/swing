package com.swing.score.ocr

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result of reading a score-card screenshot.
 * [pars] and [strokes] are aligned per hole (9 or 18). Everything is a
 * best effort — [checksumOk] tells whether the per-hole sum matched a
 * detected subtotal, so the UI can flag low-confidence reads for review.
 */
data class Recognition(
    val pars: List<Int>,
    val strokes: List<Int>,
    val checksumOk: Boolean,
    val success: Boolean,
)

private class NumRow(val cy: Int, val values: List<Int>, val boxes: List<Rect>)

object ScorecardRecognizer {

    suspend fun recognize(context: Context, uri: Uri): Recognition = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val text = Tasks.await(client.process(image))
            parse(text)
        } catch (t: Throwable) {
            Recognition(emptyList(), emptyList(), checksumOk = false, success = false)
        }
    }

    /**
     * Strategy: collect every integer token with its position, group tokens
     * into horizontal rows, then look for the recurring "9 numbers + subtotal"
     * shape a score-card uses. The PAR row (values mostly 3..5) anchors each
     * IN/OUT block; the following numeric row is the player's per-hole line,
     * which score-cards render relative to par — so strokes = par + value,
     * unless the values already look like raw strokes.
     */
    private fun parse(text: Text): Recognition {
        val tokens = ArrayList<Pair<Int, Rect>>() // value, box
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (el in line.elements) {
                    val raw = el.text.trim()
                    val n = raw.toIntOrNull() ?: continue
                    val box = el.boundingBox ?: continue
                    if (n in 0..19) tokens.add(n to box)
                }
            }
        }
        if (tokens.size < 12) return fail()

        val rows = clusterRows(tokens)
        // Candidate score rows: 8+ values (9 holes, maybe with subtotal/label noise).
        val scoreRows = rows.filter { it.values.size >= 8 }
        if (scoreRows.isEmpty()) return fail()

        val parRows = scoreRows.filter { row ->
            val nine = row.values.take(9)
            nine.count { it in 3..5 } >= 6
        }

        val pars = ArrayList<Int>()
        val strokes = ArrayList<Int>()
        var checksumHits = 0
        var checksumTotal = 0

        for (parRow in parRows.take(2)) {
            val parVals = parRow.values.take(9)
            if (parVals.size < 9) continue
            // The player's row is the closest score row below the par row.
            val playerRow = scoreRows
                .filter { it !== parRow && it.cy > parRow.cy }
                .minByOrNull { it.cy - parRow.cy } ?: continue

            val playerVals = playerRow.values.take(9)
            if (playerVals.size < 9) continue

            // Relative (values small, <= par) vs raw strokes.
            val looksRelative = playerVals.zip(parVals).all { (v, p) -> v <= p + 1 } &&
                playerVals.sum() < parVals.sum()
            val holeStrokes = if (looksRelative) {
                playerVals.mapIndexed { i, v -> parVals[i] + v }
            } else {
                playerVals
            }

            // Checksum against a subtotal token, if the row carries one.
            val subtotal = playerRow.values.getOrNull(9)
            if (subtotal != null) {
                checksumTotal++
                if (subtotal == holeStrokes.sum() || subtotal == playerVals.sum()) checksumHits++
            }

            pars.addAll(parVals)
            strokes.addAll(holeStrokes)
        }

        if (pars.isEmpty() || strokes.size != pars.size) return fail()

        val checksumOk = checksumTotal > 0 && checksumHits == checksumTotal
        return Recognition(pars = pars, strokes = strokes, checksumOk = checksumOk, success = true)
    }

    private fun clusterRows(tokens: List<Pair<Int, Rect>>): List<NumRow> {
        if (tokens.isEmpty()) return emptyList()
        val heights = tokens.map { it.second.height() }.sorted()
        val medianH = heights[heights.size / 2].coerceAtLeast(1)
        val threshold = medianH * 0.6

        val sorted = tokens.sortedBy { it.second.centerY() }
        val rows = ArrayList<MutableList<Pair<Int, Rect>>>()
        for (tk in sorted) {
            val cy = tk.second.centerY()
            val row = rows.lastOrNull()
            if (row != null && kotlin.math.abs(cy - row.last().second.centerY()) <= threshold) {
                row.add(tk)
            } else {
                rows.add(mutableListOf(tk))
            }
        }
        return rows.map { r ->
            val ordered = r.sortedBy { it.second.left }
            NumRow(
                cy = ordered.map { it.second.centerY() }.average().toInt(),
                values = ordered.map { it.first },
                boxes = ordered.map { it.second },
            )
        }
    }

    private fun Rect.centerY(): Int = (top + bottom) / 2

    private fun fail() = Recognition(emptyList(), emptyList(), checksumOk = false, success = false)
}
