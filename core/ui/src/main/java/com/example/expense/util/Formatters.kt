package com.example.expense.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String = "NZD"): String {
    return try {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: Exception) {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("NZD")
        format.format(amount)
    }
}

fun formatDate(millis: Long): String {
    val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()))
}

fun formatDateShort(millis: Long): String {
    val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    return date.format(DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault()))
}

fun formatDayWithWeekday(millis: Long): String {
    val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    return date.format(DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault()))
}

fun isToday(millis: Long): Boolean {
    val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    return date == LocalDate.now(ZoneId.systemDefault())
}

fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val zone = ZoneId.systemDefault()
    return LocalDate.ofInstant(Instant.ofEpochMilli(millis1), zone) ==
           LocalDate.ofInstant(Instant.ofEpochMilli(millis2), zone)
}

fun dayStartMillis(millis: Long): Long {
    val zone = ZoneId.systemDefault()
    return LocalDate.ofInstant(Instant.ofEpochMilli(millis), zone)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()
}

fun formatMonthYear(month: Int, year: Int): String {
    val ym = YearMonth.of(year, month + 1) // month is 0-based
    return ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
}
