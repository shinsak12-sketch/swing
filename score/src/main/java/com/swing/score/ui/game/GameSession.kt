package com.swing.score.ui.game

import com.swing.score.domain.StandardPars

/** Holds the in-progress betting round between the setup, live and settle screens. */
object GameSession {
    var players: List<String> = emptyList()
    var tadang: Int = 1000
    var pars: List<Int> = StandardPars
    // Committed strokes at settlement: [hole][player].
    var result: List<List<Int>> = emptyList()

    fun start(players: List<String>, tadang: Int) {
        this.players = players
        this.tadang = tadang
        this.pars = StandardPars
        this.result = emptyList()
    }
}
