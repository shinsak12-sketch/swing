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

    fun update(round: Round) {
        _rounds.value = _rounds.value.map { if (it.id == round.id) round else it }
    }

    fun delete(id: String) {
        _rounds.value = _rounds.value.filterNot { it.id == id }
    }

    fun delete(ids: Set<String>) {
        _rounds.value = _rounds.value.filterNot { it.id in ids }
    }

    fun find(id: String): Round? = _rounds.value.firstOrNull { it.id == id }

    fun bestRoundId(): String? = _rounds.value.minByOrNull { it.total }?.id

    fun newId(): String = "r${seq++}"

    /** Average of all recorded totals, or null if none. */
    fun average(): Int? =
        _rounds.value.map { it.total }.takeIf { it.isNotEmpty() }?.average()?.let { Math.round(it).toInt() }

    fun best(): Int? = _rounds.value.minOfOrNull { it.total }

    /** Build a round from par-relative values (par = 0, birdie = -1, bogey = +1…). */
    private fun rel(
        id: String,
        name: String,
        sub: String,
        date: String,
        pars: List<Int>,
        rels: List<Int>,
        source: RoundSource,
    ) = Round(
        id = id,
        courseName = name,
        subtitle = sub,
        dateLabel = date,
        pars = pars,
        strokes = pars.mapIndexed { i, p -> p + rels[i] },
        source = source,
    )

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
            dateLabel = "2026.06.25",
            pars = listOf(4, 5, 4, 3, 4, 4, 5, 3, 4) + listOf(4, 3, 4, 5, 3, 4, 5, 4, 4),
            strokes = listOf(5, 6, 8, 4, 4, 6, 6, 4, 7) + listOf(4, 3, 5, 9, 4, 6, 7, 5, 5),
            source = RoundSource.Capture,
        ),
        rel("r3", "잭니클라우스 GC", "회원제", "2026.06.10", StandardPars,
            listOf(0, 1, 1, 1, -1, 0, 1, 0, 1) + listOf(1, 1, 0, 1, 0, 1, 2, 0, 1), RoundSource.Manual),
        rel("r4", "남춘천CC", "레이크→마운틴", "2026.05.28", StandardPars,
            listOf(1, 2, 0, 1, 1, 0, 2, 1, 0) + listOf(1, 1, 1, 0, 1, 1, 1, 0, 2), RoundSource.Capture),
        rel("r5", "오크밸리CC", "18H", "2026.05.14", StandardPars,
            listOf(1, 1, 1, 1, 1, 1, 1, 0, 1) + listOf(1, 1, 0, 1, 1, 1, 1, 1, 1), RoundSource.Manual),
        rel("r6", "레이크사이드CC", "남코스", "2026.04.30", StandardPars,
            listOf(1, 2, 1, 1, 1, 1, 2, 1, 1) + listOf(1, 2, 1, 1, 1, 1, 2, 0, 1), RoundSource.Capture),
        rel("r7", "Pebble Beach GL", "수기 입력", "2026.04.11", StandardPars,
            listOf(1, 2, 1, 1, 0, 2, 0, 1, 1) + listOf(1, 1, 1, 0, 2, 1, 1, 0, 2), RoundSource.Manual),
        rel("r8", "인천 스카이72", "하늘코스", "2026.03.22", StandardPars,
            listOf(2, 2, 1, 2, 2, 1, 2, 1, 2) + listOf(2, 2, 1, 2, 2, 1, 2, 1, 2), RoundSource.Capture),
        rel("r9", "몽베르CC", "A→B", "2026.03.05", StandardPars,
            listOf(2, 2, 2, 1, 2, 2, 2, 1, 2) + listOf(2, 2, 1, 2, 2, 2, 1, 2, 2), RoundSource.Manual),
        rel("r10", "세인트포CC", "18H", "2026.02.18", StandardPars,
            listOf(2, 3, 1, 2, 2, 2, 2, 1, 2) + listOf(2, 2, 2, 2, 2, 2, 2, 1, 2), RoundSource.Capture),
    )
}
