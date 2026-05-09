package com.example.expense.core.data.repository

import com.example.expense.core.data.mapper.toDomain
import com.example.expense.core.data.mapper.toEntity
import com.example.expense.core.database.dao.CategoryDao
import com.example.expense.core.database.entity.CategoryEntity
import com.example.expense.core.domain.model.Category
import com.example.expense.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        dao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        dao.getCategoryById(id)?.toDomain()

    override suspend fun saveCategory(category: Category): Long =
        dao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category) =
        dao.update(category.toEntity())

    override suspend fun deleteCategory(id: Long) {
        dao.getCategoryById(id)?.let { dao.delete(it) }
    }

    override suspend fun seedDefaultCategories() {
        if (dao.getCategoryCount() == 0) {
            dao.insertAll(
                listOf(
                    CategoryEntity(name = "Food & Dining", colorIndex = 0),
                    CategoryEntity(name = "Transport", colorIndex = 1),
                    CategoryEntity(name = "Shopping", colorIndex = 2),
                    CategoryEntity(name = "Bills & Utilities", colorIndex = 3),
                    CategoryEntity(name = "Entertainment", colorIndex = 4),
                    CategoryEntity(name = "Health", colorIndex = 5),
                    CategoryEntity(name = "Education", colorIndex = 6),
                    CategoryEntity(name = "Other", colorIndex = 7)
                )
            )
        }
    }
}
