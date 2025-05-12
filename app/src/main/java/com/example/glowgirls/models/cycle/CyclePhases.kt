package com.example.glowgirls.models.cycle

data class CyclePhases(
    val menstrual: PhaseData = PhaseData(),
    val follicular: PhaseData = PhaseData(),
    val ovulatory: PhaseData = PhaseData(),
    val luteal: PhaseData = PhaseData()
)
