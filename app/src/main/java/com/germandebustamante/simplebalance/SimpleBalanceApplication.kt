package com.germandebustamante.simplebalance

import android.app.Application
import com.germandebustamante.simplebalance.di.appModule
import com.germandebustamante.simplebalance.di.dataModule
import com.germandebustamante.simplebalance.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SimpleBalanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SimpleBalanceApplication)
            modules(appModule, dataModule, domainModule)
        }
    }
}