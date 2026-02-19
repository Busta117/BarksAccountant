package me.busta.barksaccountant.model

data class Client(
    val id: String,
    val name: String,
    val responsible: String?,
    val nif: String?,
    val address: String?,
    val ivaPct: Double?,
    val recargoPct: Double?
)
