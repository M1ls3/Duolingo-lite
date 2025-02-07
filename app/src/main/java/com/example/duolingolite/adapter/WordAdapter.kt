package com.example.duolingolite.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.duolingolite.R
import com.example.duolingolite.data.Word
import com.example.duolingolite.viewholder.WordViewHolder

class WordAdapter(
    private var items: List<Word>,
    private var showButtons: Boolean = true,
    private var showTranslate: Boolean = false,
    private var onEditClick: (Word) -> Unit,
    private var onDeleteClick: (Word) -> Unit,
    private var onTranslateClick: (Word) -> Unit
) : RecyclerView.Adapter<WordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val item = items[position]

        holder.wordTextView.text = item.word
        // Контролюємо видимість тексту перекладу
        if (showTranslate) {
            holder.translateTextView.text = item.translate
            holder.translateTextView.visibility = View.VISIBLE
        } else {
            holder.translateTextView.visibility = View.GONE
        }

        if (showButtons) {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            holder.translateButton.visibility = View.GONE

            holder.editButton.setOnClickListener { onEditClick(item) }
            holder.deleteButton.setOnClickListener { onDeleteClick(item) }
        } else {
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            holder.translateButton.visibility = View.VISIBLE

            // Обробка кліку по кнопці "Translate" у елементі списку
            holder.translateButton.setOnClickListener { onTranslateClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    // Оновлення даних у списку
    fun updateData(newItems: List<Word>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Оновлення даних у списку
    fun updateData(newItems: List<Word>, showTranslate: Boolean) {
        items = newItems
        this.showTranslate = showTranslate
        notifyDataSetChanged()
    }
}