package com.example.android.guesstheword

import android.app.Application
import timber.log.Timber

/**
 * created by Di Wu: stevenwudi@gmail.com on 2020/10/22.
 */
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

    }
}