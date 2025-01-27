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

    // Отримання всіх записів
    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<Word>
}