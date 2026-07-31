package com.swing.score.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory store of rounds, seeded with real sample data.
 * Swapped for a Room-backed implementation in the next milestone;
 * the UI only depends on this surface.
 */
object RoundRepository {

    private val _rounds = MutableStateFlow(seedRounds())
    val rounds: StateFlow<List<Round>> = _rounds.asStateFlow()

    private var seq = 1000

    fun add(round: Round) {
        _rounds.value = listOf(round) + _rounds.value
    }

    fun find(id: String): Round? = _rounds.value.firstOrNull { it.id == id }

    fun newId(): String = "r${seq++}"

    /** Average of all recorded totals, or null if none. */
    fun average(): Int? =
        _rounds.value.map { it.total }.takeIf { it.isNotEmpty() }?.average()?.let { Math.round(it).toInt() }

    fun best(): Int? = _rounds.value.minOfOrNull { it.total }

    private fun seedRounds(): List<Round> = listOf(
        Round(
            id = "r1",
            courseName = "소노펠리체CC 비발디파크",
            subtitle = "WEST",
            dateLabel = "2026.07.25",
            pars = listOf(4, 5, 3, 4, 4, 3, 4, 4, 5) + listOf(4, 3, 4, 4, 4, 5, 4, 3, 5),
            strokes = listOf(4, 7, 5, 5, 4, 5, 7, 5, 7) + listOf(4, 3, 6, 5, 5, 7, 6, 3, 7),
            source = RoundSource.Capture,
        ),
        Round(
            id = "r2",
            courseName = "썬힐 골프클럽",
            subtitle = "힐 → 썬",
            dateLabel = "2026.04.25",
            pars = listOf(4, 5, 4, 3, 4, 4, 5, 3, 4) + listOf(4, 3, 4, 5, 3, 4, 5, 4, 4),
            strokes = listOf(5, 6, 8, 4, 4, 6, 6, 4, 7) + listOf(4, 3, 5, 9, 4, 6, 7, 5, 5),
            source = RoundSource.Capture,
        ),
        Round(
            id = "r3",
            courseName = "Pebble Beach GL",
            subtitle = "수기 입력",
            dateLabel = "2026.03.11",
            pars = listOf(4, 5, 4, 4, 3, 5, 3, 4, 4) + listOf(4, 4, 4, 3, 5, 4, 4, 3, 5),
            strokes = listOf(5, 7, 5, 5, 4, 6, 4, 5, 4) + listOf(5, 5, 5, 4, 6, 5, 6, 4, 6),
            source = RoundSource.Manual,
        ),
    )
}
