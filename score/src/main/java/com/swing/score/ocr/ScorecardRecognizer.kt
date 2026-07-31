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
import kotlin.math.abs

/**
 * Result of reading a score-card screenshot. [pars]/[strokes] are aligned per
 * hole (9 or 18). [checksumOk] means a detected subtotal matched the per-hole
 * sum, so the UI can flag low-confidence reads for review.
 */
data class Recognition(
    val pars: List<Int>,
    val strokes: List<Int>,
    val checksumOk: Boolean,
    val success: Boolean,
)

private class NumRow(val cy: Int, val values: List<Int>)

object ScorecardRecognizer {

    private val DEFAULT_PARS = listOf(4, 4, 3, 4, 5, 4, 4, 3, 5) + listOf(4, 4, 3, 4, 5, 4, 4, 3, 5)

    suspend fun recognize(context: Context, uri: Uri): Recognition = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val text = Tasks.await(client.process(image))
            parse(text)
        } catch (t: Throwable) {
            fail()
        }
    }

    private fun parse(text: Text): Recognition {
        val tokens = ArrayList<Pair<Int, Rect>>()
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (el in line.elements) {
                    val n = el.text.trim().toIntOrNull() ?: continue
                    val box = el.boundingBox ?: continue
                    if (n in 0..199) tokens.add(n to box)
                }
            }
        }
        if (tokens.size < 10) return fail()

        val rows = clusterRows(tokens)
        // Rows that carry a run of per-hole cells.
        val candidates = rows.filter { it.values.count { v -> v in 0..15 } >= 8 }
        if (candidates.isEmpty()) return fail()

        // A PAR row: nine values in 3..5-ish, summing to a 9-hole par (~36).
        val parRows = candidates.filter { row ->
            val nine = row.values.filter { it in 0..15 }.take(9)
            nine.size >= 9 && nine.sum() in 30..41 && nine.count { it in 3..5 } >= 6
        }

        val pars = ArrayList<Int>()
        val strokes = ArrayList<Int>()
        var checkTotal = 0
        var checkHit = 0

        for (parRow in parRows.take(2)) {
            val parVals = parRow.values.filter { it in 0..15 }.take(9)
            if (parVals.size < 9) continue
            val player = candidates
                .filter { it !== parRow && it.cy > parRow.cy }
                .minByOrNull { it.cy - parRow.cy } ?: continue
            val playerVals = player.values.filter { it in 0..15 }.take(9)
            if (playerVals.size < 9) continue

            val chosen = interpret(parVals, playerVals, player.values.firstOrNull { it in 30..70 })
            val subtotal = player.values.firstOrNull { it in 30..70 }
            if (subtotal != null) {
                checkTotal++
                if (subtotal == chosen.sum()) checkHit++
            }
            pars.addAll(parVals)
            strokes.addAll(chosen)
        }

        if (pars.isNotEmpty() && strokes.size == pars.size) {
            val ok = checkTotal > 0 && checkHit == checkTotal
            return Recognition(pars, strokes, ok, true)
        }

        // Fallback: no clean PAR row — prefill the strongest cell row so the
        // user has something to correct rather than a blank failure.
        val best = candidates.maxByOrNull { it.values.count { v -> v in 0..15 } } ?: return fail()
        val vals = best.values.filter { it in 0..15 }
        val n = if (vals.size >= 18) 18 else 9
        if (vals.size < n) return fail()
        val cells = vals.take(n)
        val defPars = DEFAULT_PARS.take(n)
        val looksRelative = cells.any { it == 0 } || cells.sorted()[cells.size / 2] <= 2
        val guessed = if (looksRelative) cells.mapIndexed { i, v -> defPars[i] + v } else cells
        return Recognition(defPars, guessed, checksumOk = false, success = true)
    }

    /** Score-cards render the player line relative to par; detect and convert. */
    private fun interpret(pars: List<Int>, values: List<Int>, subtotal: Int?): List<Int> {
        val asRelative = pars.zip(values).map { (p, v) -> p + v }
        val asRaw = values
        return when {
            subtotal == asRelative.sum() -> asRelative
            subtotal == asRaw.sum() -> asRaw
            // Values at or below par (with the odd birdie) read as relative.
            values.zip(pars).count { (v, p) -> v <= p } >= 7 && asRelative.sum() in 30..70 -> asRelative
            asRaw.sum() in 30..70 -> asRaw
            else -> asRelative
        }
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
            if (row != null && abs(cy - row.last().second.centerY()) <= threshold) {
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
            )
        }
    }

    private fun Rect.centerY(): Int = (top + bottom) / 2

    private fun fail() = Recognition(emptyList(), emptyList(), checksumOk = false, success = false)
}
