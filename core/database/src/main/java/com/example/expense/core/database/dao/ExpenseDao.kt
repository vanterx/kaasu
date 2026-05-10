package com.example.expense.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expense.core.database.entity.ExpenseEntity
import com.example.expense.core.database.entity.ExpenseWithCategoryEntity
import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseWithCategoryEntity>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseWithCategoryEntity?

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
    fun getExpensesForMonth(startOfMonth: Long, startOfNextMonth: Long): Flow<List<ExpenseWithCategoryEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis >= :startOfMonth AND dateMillis < :startOfNextMonth AND categoryId IS NULL")
    fun getUncategorizedTotalForMonth(startOfMonth: Long, startOfNextMonth: Long): Flow<Double?>

    @Query("""
        SELECT account, SUM(amount) as total
        FROM expenses
        WHERE dateMillis >= :start AND dateMillis < :end
        GROUP BY account
    """)
    fun getAccountTotalsForRange(start: Long, end: Long): Flow<List<AccountTotal>>

    @Transaction
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT categoryId, COUNT(*) as count FROM expenses WHERE categoryId IS NOT NULL GROUP BY categoryId")
    fun getCategoryUsageCounts(): Flow<List<com.example.expense.core.domain.model.CategoryUsage>>
}
