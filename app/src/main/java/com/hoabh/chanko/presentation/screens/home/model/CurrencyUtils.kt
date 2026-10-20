package com.hoabh.chanko.presentation.screens.home.model

import kotlin.math.abs

fun Long.toVndCurrency(): String {
    val sign = if (this < 0) "-" else ""
    val raw = abs(this).toString()
    val grouped = raw.reversed().chunked(3).joinToString(".").reversed()
    return "$sign$grouped ₫"
}
