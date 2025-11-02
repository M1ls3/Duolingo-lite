package com.example.duolingolite.repo

import com.example.duolingolite.dao.Dao
import com.example.duolingolite.data.Word

class Repo(private val dao: Dao) {
    suspend fun insertWord(word: Word){
        dao.insertWord(word)
    }

    suspend fun updateWord(word: Word){
        dao.updateWord(word)
    }

    suspend fun deleteWord(word: Word){
        dao.deleteWord(word)
    }

    suspend fun getAllWords(): List<Word>{
        return dao.getAllWords()
    }

    suspend fun getRandomWord(): Word? {
        return dao.getRandomWord()
    }

    suspend fun getAllWordsByTopic(topic: String): List<Word>{
        return dao.getWordsByTopic(topic)
    }

    suspend fun getAllTopics(): List<String>{
        return dao.getAllTopics()
    }

    suspend fun deleteWordsByTopic(topic: String): Int {
        return dao.deleteWordsByTopic(topic)
    }

    suspend fun getRandomWordByTopic(topic: String): Word? {
        return dao.getRandomWordByTopic(topic)
    }
}