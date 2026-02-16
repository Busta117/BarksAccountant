package me.busta.barksaccountant.feature.stats

import me.busta.barksaccountant.model.Purchase
import me.busta.barksaccountant.model.Sale

data class MonthlySale(
    val month: Int,
    val total: Double
)

data class ProductStat(
    val name: String,
    val unitsSold: Int,
    val revenue: Double
)

data class ClientStat(
    val name: String,
    val orderCount: Int,
    val totalAmount: Double
)

data class StatsState(
    val allSales: List<Sale> = emptyList(),
    val allPurchases: List<Purchase> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = 0,
    val selectedMonth: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    private val filteredSales: List<Sale> get() = allSales.filter { sale ->
        val year = sale.orderDate.substring(0, 4).toIntOrNull()
        val month = sale.orderDate.substring(5, 7).toIntOrNull()
        year == selectedYear && (selectedMonth == null || month == selectedMonth)
    }

    private val filteredPurchases: List<Purchase> get() = allPurchases.filter { purchase ->
        val year = purchase.date.substring(0, 4).toIntOrNull()
        val month = purchase.date.substring(5, 7).toIntOrNull()
        year == selectedYear && (selectedMonth == null || month == selectedMonth)
    }

    // Section 1: Financial Summary
    val totalSales: Double get() = filteredSales.sumOf { it.totalPrice }
    val totalPurchases: Double get() = filteredPurchases.sumOf { it.value }
    val netProfit: Double get() = totalSales - totalPurchases
    val marginPercent: Double get() = if (totalSales > 0.0) (netProfit / totalSales) * 100.0 else 0.0

    // Section 2: Counters
    val salesCount: Int get() = filteredSales.size
    val averageTicket: Double get() = if (salesCount > 0) totalSales / salesCount else 0.0
    val unpaidTotal: Double get() = filteredSales.filter { !it.isPaid }.sumOf { it.totalPrice }
    val undeliveredCount: Int get() = filteredSales.count { !it.isDelivered }

    // Section 3: Monthly Breakdown (only when viewing full year)
    val monthlyBreakdown: List<MonthlySale> get() {
        if (selectedMonth != null) return emptyList()
        return filteredSales
            .groupBy { it.orderDate.substring(5, 7).toIntOrNull() ?: 0 }
            .map { (month, sales) -> MonthlySale(month = month, total = sales.sumOf { it.totalPrice }) }
            .filter { it.month in 1..12 }
            .sortedBy { it.month }
    }

    // Section 4: Top Products
    val topProducts: List<ProductStat> get() {
        val productMap = mutableMapOf<String, ProductStat>()
        for (sale in filteredSales) {
            for (product in sale.products) {
                val existing = productMap[product.name]
                if (existing != null) {
                    productMap[product.name] = existing.copy(
                        unitsSold = existing.unitsSold + product.quantity,
                        revenue = existing.revenue + product.totalPrice
                    )
                } else {
                    productMap[product.name] = ProductStat(
                        name = product.name,
                        unitsSold = product.quantity,
                        revenue = product.totalPrice
                    )
                }
            }
        }
        return productMap.values.sortedByDescending { it.unitsSold }
    }

    // Section 5: Top Clients
    val topClients: List<ClientStat> get() {
        val clientMap = mutableMapOf<String, ClientStat>()
        for (sale in filteredSales) {
            val existing = clientMap[sale.clientName]
            if (existing != null) {
                clientMap[sale.clientName] = existing.copy(
                    orderCount = existing.orderCount + 1,
                    totalAmount = existing.totalAmount + sale.totalPrice
                )
            } else {
                clientMap[sale.clientName] = ClientStat(
                    name = sale.clientName,
                    orderCount = 1,
                    totalAmount = sale.totalPrice
                )
            }
        }
        return clientMap.values.sortedByDescending { it.totalAmount }
    }
}
