package me.busta.barksaccountant.data

interface LocalStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}
