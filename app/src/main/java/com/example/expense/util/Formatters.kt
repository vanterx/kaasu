package com.example.expense.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance(Locale.getDefault())
    return format.format(amount)
}

fun formatDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

fun formatMonthYear(month: Int, year: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month, 1)
    val sdf = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}
