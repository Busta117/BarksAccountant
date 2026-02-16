package me.busta.barksaccountant.android

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import me.busta.barksaccountant.data.AndroidFirestoreService
import me.busta.barksaccountant.data.AndroidLocalStorage
import me.busta.barksaccountant.di.ServiceLocator

class BarksAccountantApp : Application() {
    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        serviceLocator = ServiceLocator(
            localStorage = AndroidLocalStorage(this),
            firestoreService = AndroidFirestoreService()
        )
    }
}
