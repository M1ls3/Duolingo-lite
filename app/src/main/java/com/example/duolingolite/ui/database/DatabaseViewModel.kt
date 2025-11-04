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

    private val _allTopics = MutableLiveData<List<String>>()
    val allTopics: LiveData<List<String>> = _allTopics

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val wordDao = WordsDatabase.getDatabase(application).wordDao()
        repository = Repo(wordDao)
        //loadItems() // Завантажуємо дані при ініціалізації
        println("DEBUG: DatabaseViewModel initialized")
    }

    // Завантаження всіх елементів
    fun loadItems() {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading all items...")
                val items = repository.getAllWords()
                println("DEBUG: Loaded ${items.size} items")
                _wordItems.postValue(items)
            } catch (e: Exception) {
                println("DEBUG: Error loading items: ${e.message}")
                _errorMessage.postValue("Error loading data: ${e.message}")
            }
        }
    }

    fun loadItemsByTopic(topic: String) {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading items for topic: $topic")
                val items = repository.getAllWordsByTopic(topic)
                println("DEBUG: Loaded ${items.size} items for topic: $topic")
                _wordItems.postValue(items)
            } catch (e: Exception) {
                println("DEBUG: Error loading items for topic $topic: ${e.message}")
                _errorMessage.postValue("Error loading data: ${e.message}")
            }
        }
    }

    fun loadAllTopics() {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading all topics...")
                val topics = repository.getAllTopics()
                println("DEBUG: Loaded topics: $topics")
                _allTopics.postValue(topics)
            } catch (e: Exception) {
                println("DEBUG: Error loading topics: ${e.message}")
                _errorMessage.postValue("Error loading topics: ${e.message}")
            }
        }
    }

    fun deleteTopic(topic: String) {
        viewModelScope.launch {
            try {
                repository.deleteWordsByTopic(topic)
                loadAllTopics() // Обновляем список топиков после удаления
            } catch (e: Exception) {
                _errorMessage.postValue("Error deleting topic: ${e.message}")
            }
        }
    }

    // Оновлення націоналізації та країн
    suspend fun updateWord(word: Word) {
        viewModelScope.launch {
            try {
                repository.updateWord(word)
                //loadItems() // Оновлюємо список після оновлення
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
                //loadItems() // Оновлюємо список після видалення
            } catch (e: Exception) {
                _errorMessage.postValue("Error deleting data: ${e.message}")
            }
        }
    }

    suspend fun insertWord(word: Word) {
        viewModelScope.launch {
            try {
                repository.insertWord(word)
            } catch (e: Exception) {
                _errorMessage.postValue("Error insert data: ${e.message}")
            }
        }
    }
}