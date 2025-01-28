package com.example.duolingolite.dao
import androidx.room.Dao
import androidx.room.*
import com.example.duolingolite.data.Word

interface Dao  {
    // Вставка або оновлення запису в таблиці
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    // Видалення
    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT * FROM words ORDER BY word ASC")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): Word
}