package com.example.expense.data.repository

import com.example.expense.data.db.CategoryDao
import com.example.expense.data.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories()

    override suspend fun getCategoryById(id: Long): Category? = dao.getCategoryById(id)

    override suspend fun saveCategory(category: Category): Long = dao.insert(category)

    override suspend fun updateCategory(category: Category) = dao.update(category)

    override suspend fun deleteCategory(category: Category) = dao.delete(category)

    override suspend fun seedDefaultCategories() {
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
