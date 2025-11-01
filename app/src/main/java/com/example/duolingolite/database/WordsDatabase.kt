package com.example.duolingolite.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.duolingolite.dao.Dao
import com.example.duolingolite.data.Word

@Database(entities = [Word::class], version = 2)
abstract class WordsDatabase : RoomDatabase() {
    abstract fun wordDao() : Dao

    companion object {
        @Volatile
        private var INSTANCE: WordsDatabase? = null

        // Миграция с версии 1 на 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новую колонку с значением по умолчанию
                database.execSQL(
                    "ALTER TABLE words ADD COLUMN topic TEXT NOT NULL DEFAULT 'Default'"
                )
            }
        }

        fun getDatabase(context: Context): WordsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordsDatabase::class.java,
                    "words_database"
                )
                    .addMigrations(MIGRATION_1_2)
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}