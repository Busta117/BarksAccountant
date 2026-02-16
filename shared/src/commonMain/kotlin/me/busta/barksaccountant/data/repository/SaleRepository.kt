package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Sale

interface SaleRepository {
    suspend fun getSales(): List<Sale>
    suspend fun getSale(id: String): Sale?
    suspend fun saveSale(sale: Sale): Sale
    suspend fun updateSale(sale: Sale): Sale
    suspend fun deleteSale(id: String)
}
