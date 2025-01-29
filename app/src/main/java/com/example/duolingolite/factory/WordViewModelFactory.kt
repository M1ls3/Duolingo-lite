package com.example.duolingolite.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.duolingolite.ui.database.DatabaseViewModel

class WordViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}