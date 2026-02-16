package me.busta.barksaccountant.data

import platform.Foundation.NSUserDefaults

class IosLocalStorage : LocalStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
        defaults.synchronize()
    }
}
