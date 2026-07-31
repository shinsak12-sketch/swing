package com.swing.score.domain

import kotlin.math.min

data class Transfer(val from: String, val to: String, val amount: Int)

data class Settlement(
    val net: List<Int>,
    val transfers: List<Transfer>,
)

/**
 * Stroke ("타당") game: on every hole each player settles against every other
 * player by the stroke difference × [tadang]. The sum is zero across the table.
 */
object BetEngine {

    fun stroke(players: List<String>, strokesPerHole: List<List<Int>>, tadang: Int): Settlement {
        val n = players.size
        val net = IntArray(n)
        for (hole in strokesPerHole) {
            if (hole.size != n) continue
            for (i in 0 until n) {
                var diff = 0
                for (j in 0 until n) if (j != i) diff += hole[j] - hole[i]
                net[i] += diff * tadang
            }
        }
        return Settlement(net.toList(), minimalTransfers(players, net.toList()))
    }

    /** Greedy min-cash-flow: match biggest debtor to biggest creditor. */
    private fun minimalTransfers(players: List<String>, net: List<Int>): List<Transfer> {
        val creditors = ArrayList<IntArray>() // [index, amount]
        val debtors = ArrayList<IntArray>()
        net.forEachIndexed { i, v ->
            when {
                v > 0 -> creditors.add(intArrayOf(i, v))
                v < 0 -> debtors.add(intArrayOf(i, -v))
            }
        }
        val out = ArrayList<Transfer>()
        var c = 0
        var d = 0
        while (c < creditors.size && d < debtors.size) {
            val pay = min(creditors[c][1], debtors[d][1])
            if (pay > 0) {
                out.add(Transfer(players[debtors[d][0]], players[creditors[c][0]], pay))
            }
            creditors[c][1] -= pay
            debtors[d][1] -= pay
            if (creditors[c][1] == 0) c++
            if (debtors[d][1] == 0) d++
        }
        return out
    }
}
