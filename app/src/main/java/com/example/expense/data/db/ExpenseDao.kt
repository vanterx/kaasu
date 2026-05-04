package com.example.expense.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseWithCategory?

    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis >= :startOfMonth AND dateMillis < :startOfNextMonth")
    fun getMonthlyTotal(startOfMonth: Long, startOfNextMonth: Long): Flow<Double?>

    @Query("""
        SELECT c.id, c.name, c.colorIndex, SUM(e.amount) as total
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId
            AND e.dateMillis >= :startOfMonth AND e.dateMillis < :startOfNextMonth
        GROUP BY c.id, c.name, c.colorIndex
        ORDER BY total DESC
    """)
    fun getCategoryTotalsForMonth(startOfMonth: Long, startOfNextMonth: Long): Flow<List<CategoryTotal>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE dateMillis >= :startOfMonth AND dateMillis < :startOfNextMonth ORDER BY dateMillis DESC")
    fun getExpensesForMonth(startOfMonth: Long, startOfNextMonth: Long): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY dateMillis DESC")
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis >= :startOfMonth AND dateMillis < :startOfNextMonth AND categoryId IS NULL")
    fun getUncategorizedTotalForMonth(startOfMonth: Long, startOfNextMonth: Long): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategory>
}

data class CategoryTotal(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val total: Double?
)
