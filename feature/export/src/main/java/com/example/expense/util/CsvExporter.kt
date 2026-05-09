package com.example.expense.util

import android.content.Context
import android.net.Uri
import com.example.expense.core.domain.model.ExpenseItem
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExporter(private val context: Context) {

    fun export(expenses: List<ExpenseItem>, uri: Uri) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.write("Date,Amount,Category,Account,Description")
                writer.newLine()
                for (item in expenses) {
                    val date = dateFormat.format(Date(item.dateMillis))
                    val amount = String.format(Locale.US, "%.2f", item.amount)
                    val category = item.categoryName ?: "Uncategorized"
                    val account = item.account ?: ""
                    val desc = item.description.replace("\"", "\"\"")
                    writer.write("$date,$amount,\"$category\",\"$account\",\"$desc\"")
                    writer.newLine()
                }
            }
        }
    }

    companion object {
        fun defaultFilename(): String {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            return "expenses_$dateStr.csv"
        }
    }
}
