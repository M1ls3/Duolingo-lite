package com.example.duolingolite.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "words"
)
data class Word (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word:String,
    val translate: String,
    val topic: String = "Default"
)