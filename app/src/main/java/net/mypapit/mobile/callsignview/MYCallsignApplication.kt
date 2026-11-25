package net.mypapit.mobile.callsignview

import android.app.Application
import net.mypapit.mobile.callsignview.data.local.AppDatabase
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import net.mypapit.mobile.callsignview.data.repository.AprsRepository
import net.mypapit.mobile.callsignview.data.repository.CallsignRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(application: Application) {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val database: AppDatabase by lazy {
        AppDatabase.create(application)
    }

    val preferences: AppPreferences by lazy {
        AppPreferences(application)
    }

    val callsignRepository: CallsignRepository by lazy {
        CallsignRepository(
            context = application,
            dao = database.callsignDao(),
            preferences = preferences,
            httpClient = httpClient
        )
    }

    val aprsRepository: AprsRepository by lazy {
        AprsRepository(
            context = application,
            preferences = preferences,
            httpClient = httpClient
        )
    }
}

class MYCallsignApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
