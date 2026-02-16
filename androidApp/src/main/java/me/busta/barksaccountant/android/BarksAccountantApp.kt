package me.busta.barksaccountant.android

import android.app.Application
import me.busta.barksaccountant.data.AndroidLocalStorage
import me.busta.barksaccountant.di.ServiceLocator

class BarksAccountantApp : Application() {
    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(localStorage = AndroidLocalStorage(this))
    }
}
