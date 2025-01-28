package com.example.duolingolite.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.duolingolite.dao.Dao
import com.example.duolingolite.data.Word

@Database(entities = [Word::class], version = 1)
abstract class WordsDatabase : RoomDatabase() {
    abstract fun wordDao() : Dao

    companion object {
        @Volatile
        private var INSTANCE: WordsDatabase? = null

        fun getDatabase(context: Context): WordsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordsDatabase::class.java,
                    "words_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}