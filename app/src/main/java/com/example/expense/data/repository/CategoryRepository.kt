package com.example.expense.data.repository

import com.example.expense.data.db.CategoryDao
import com.example.expense.data.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories()

    suspend fun getCategoryById(id: Long): Category? = dao.getCategoryById(id)

    suspend fun saveCategory(category: Category): Long = dao.insert(category)

    suspend fun updateCategory(category: Category) = dao.update(category)

    suspend fun deleteCategory(category: Category) = dao.delete(category)

    suspend fun seedDefaultCategories() {
        if (dao.getCategoryCount() == 0) {
            dao.insertAll(
                listOf(
                    Category(name = "Food & Dining", colorIndex = 0),
                    Category(name = "Transport", colorIndex = 1),
                    Category(name = "Shopping", colorIndex = 2),
                    Category(name = "Bills & Utilities", colorIndex = 3),
                    Category(name = "Entertainment", colorIndex = 4),
                    Category(name = "Health", colorIndex = 5),
                    Category(name = "Education", colorIndex = 6),
                    Category(name = "Other", colorIndex = 7)
                )
            )
        }
    }
}
