package com.example.expense.util

import java.text.NumberFormat
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
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

fun formatDateShort(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

fun formatDayWithWeekday(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

fun isToday(millis: Long): Boolean {
    val cal = java.util.Calendar.getInstance()
    val today = java.util.Calendar.getInstance()
    cal.timeInMillis = millis
    return cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)
}

fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = java.util.Calendar.getInstance()
    val cal2 = java.util.Calendar.getInstance()
    cal1.timeInMillis = millis1
    cal2.timeInMillis = millis2
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

fun formatMonthYear(month: Int, year: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month, 1)
    val sdf = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}
