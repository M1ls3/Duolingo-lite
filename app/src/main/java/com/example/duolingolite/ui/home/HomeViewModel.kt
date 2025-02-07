package com.example.duolingolite.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.duolingolite.database.WordsDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.duolingolite.dao.Dao
import com.example.duolingolite.data.Word
import com.example.duolingolite.repo.Repo
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // Ініціалізуємо Repo через базу даних
    private val repo: Repo

    // LiveData для поточного випадкового слова
    private val _currentWord = MutableLiveData<Word>()
    val currentWord: LiveData<Word> get() = _currentWord

    init {
        // Приклад отримання Dao з бази даних (переконайтеся, що WordDatabase реалізовано)
        val dao = WordsDatabase.getDatabase(application).wordDao()
        repo = Repo(dao)
    }

    // Метод для отримання випадкового слова з бази даних
    fun fetchRandomWord() {
        viewModelScope.launch {
            try {
                val word = repo.getRandomWord()
                _currentWord.value = word
            } catch (e: Exception) {
                // Обробка помилки (за потреби)
                e.printStackTrace()
            }
        }
    }
}
