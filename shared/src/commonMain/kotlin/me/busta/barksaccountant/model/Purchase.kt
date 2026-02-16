package me.busta.barksaccountant.model

data class Purchase(
    val id: String,
    val title: String,
    val description: String?,
    val value: Double,
    val date: String,
    val createdBy: String = ""
)
