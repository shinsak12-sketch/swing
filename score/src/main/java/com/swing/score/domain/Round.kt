package com.swing.score.domain

/**
 * A single round. Scores are stored as raw strokes per hole; anything
 * relative-to-par is derived so the score-card and stats stay consistent.
 */
data class Round(
    val id: String,
    val courseName: String,
    val subtitle: String,
    val dateLabel: String,
    val pars: List<Int>,
    val strokes: List<Int>,
    val source: RoundSource = RoundSource.Manual,
) {
    val holeCount: Int get() = strokes.size
    val total: Int get() = strokes.sum()
    val parTotal: Int get() = pars.take(holeCount).sum()
    val toPar: Int get() = total - parTotal

    fun diffAt(index: Int): Int = strokes[index] - pars[index]

    /** Count of holes at each score bucket: birdie-or-better, par, bogey, double+. */
    fun distribution(): IntArray {
        val out = IntArray(4)
        for (i in 0 until holeCount) {
            when (diffAt(i)) {
                in Int.MIN_VALUE..-1 -> out[0]++
                0 -> out[1]++
                1 -> out[2]++
                else -> out[3]++
            }
        }
        return out
    }
}

enum class RoundSource { Manual, Capture, Live }

/** A standard 18-hole par layout (par 72) used as a sensible default. */
val StandardPars: List<Int> =
    listOf(4, 5, 4, 3, 4, 4, 5, 3, 4) + listOf(4, 3, 4, 5, 3, 4, 5, 4, 4)
