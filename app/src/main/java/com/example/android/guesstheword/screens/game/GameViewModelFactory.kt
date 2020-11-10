package com.example.android.guesstheword.screens.game

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

/**
 * created by Di Wu: stevenwudi@gmail.com on 2020/11/10.
 */
class GameViewModelFactory(private val application: Application): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(GameViewModel::class.java)){
            return GameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}