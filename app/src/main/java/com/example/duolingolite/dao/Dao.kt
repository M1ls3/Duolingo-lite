package com.example.duolingolite.dao
import androidx.room.Dao
import androidx.room.*
import com.example.duolingolite.data.Word

@Dao
interface Dao  {
    // Вставка або оновлення запису в таблиці
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    // Видалення
    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT * FROM words ORDER BY word DESC")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): Word?

    // Дополнительные методы для работы с topic (опционально)
    @Query("SELECT * FROM words WHERE topic = :topic ORDER BY word DESC")
    suspend fun getWordsByTopic(topic: String): List<Word>

    @Query("SELECT DISTINCT topic FROM words")
    suspend fun getAllTopics(): List<String>

    @Query("DELETE FROM words WHERE topic = :topic")
    suspend fun deleteWordsByTopic(topic: String): Int

    @Query("SELECT * FROM words WHERE topic = :topic ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByTopic(topic: String): Word?

//    @Query("SELECT * FROM words WHERE topic = :topic ORDER BY RANDOM() LIMIT 1")
//    suspend fun getRandomWordByTopic(topic: String): Word?

    /*PRAGMA foreign_keys = 0;

CREATE TABLE sqlitestudio_temp_table AS SELECT *
                                          FROM word;

DROP TABLE word;

CREATE TABLE word (
    id          INTEGER PRIMARY KEY,
    word        TEXT,
    translation TEXT,
    topik       TEXT DEFAULT 'Default'
);

INSERT INTO word (
                     id,
                     word,
                     translation
                 )
                 SELECT id,
                        word,
                        translation
                   FROM sqlitestudio_temp_table;

DROP TABLE sqlitestudio_temp_table;

PRAGMA foreign_keys = 1;
*/

}