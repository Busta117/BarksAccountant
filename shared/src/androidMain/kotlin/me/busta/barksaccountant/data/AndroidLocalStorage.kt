package me.busta.barksaccountant.data

import android.content.Context
import android.content.SharedPreferences

class AndroidLocalStorage(context: Context) : LocalStorage {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("barks_accountant", Context.MODE_PRIVATE)

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
