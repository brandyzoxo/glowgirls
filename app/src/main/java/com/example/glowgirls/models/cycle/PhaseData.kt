package com.example.glowgirls.models.cycle

import java.time.LocalDate

data class PhaseData(
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val name: String = "",
    val description: String = "",
    val tips: List<String> = emptyList(),
    val nutrients: List<String> = emptyList()  // Recommended nutrients for this phase
)
