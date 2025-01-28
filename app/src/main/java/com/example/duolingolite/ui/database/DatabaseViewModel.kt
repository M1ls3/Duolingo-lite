package com.example.duolingolite.ui.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.duolingolite.data.Word
import com.example.duolingolite.database.WordsDatabase
import com.example.duolingolite.repo.Repo
import kotlinx.coroutines.launch

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repo

    // MutableLiveData для спостереження за списком
    private val _wordItems = MutableLiveData<List<Word>>()
    val wordItems: LiveData<List<Word>> = _wordItems

    // LiveData для відображення помилок
    private val _errorMessage = MutableLiveData<String>()

    init {
        val wordDao = WordsDatabase.getDatabase(application).wordDao()
        repository = Repo(wordDao)
        loadItems() // Завантажуємо дані при ініціалізації
    }

    // Завантаження всіх елементів
    fun loadItems() {
        viewModelScope.launch {
            try {
                val items = repository.getAllWords()
                _wordItems.postValue(items)
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading data: ${e.message}")
            }
        }
    }

    // Оновлення націоналізації та країн
    fun updateWord(word: Word) {
        viewModelScope.launch {
            try {
                repository.updateWord(word)
                loadItems() // Оновлюємо список після оновлення
            } catch (e: Exception) {
                _errorMessage.postValue("Error updating data: ${e.message}")
            }
        }
    }

    // Видалення націоналізації
    fun deleteWord(word: Word) {
        viewModelScope.launch {
            try {
                repository.deleteWord(word)
                loadItems() // Оновлюємо список після видалення
            } catch (e: Exception) {
                _errorMessage.postValue("Error deleting data: ${e.message}")
            }
        }
    }

    fun insertWord(word: Word) {
        viewModelScope.launch {
            try {
                repository.insertWord(word)
                loadItems()
            } catch (e: Exception) {
                _errorMessage.postValue("Error insert data: ${e.message}")
            }
        }
    }
}