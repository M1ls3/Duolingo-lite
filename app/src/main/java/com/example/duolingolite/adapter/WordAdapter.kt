package com.example.duolingolite.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
    private var onTranslateClick: (Word) -> Unit,
    private var topics: List<String>
) : RecyclerView.Adapter<WordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val item = items[position]
        val currentTopic = item.topic

        setupTopicSpinner(holder, currentTopic, item)

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

    private fun setupTopicSpinner(holder: WordViewHolder, currentTopic: String, item: Word) {
        // Создаем ArrayAdapter для Spinner
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            topics
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Устанавливаем адаптер в Spinner
        holder.fragmentSpinner.adapter = adapter

        // Устанавливаем выбранную тему
        val topicPosition = topics.indexOf(currentTopic)
        if (topicPosition >= 0) {
            holder.fragmentSpinner.setSelection(topicPosition)
        }

        // Обработчик выбора темы
        holder.fragmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTopic = parent.getItemAtPosition(position) as String
                // Здесь можно обработать изменение темы
                if (selectedTopic != currentTopic) {
                    // Вызовите колбэк или обновите данные
                    onTopicSelected(item, selectedTopic)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не выбрано
            }
        }
    }

    private fun onTopicSelected(word: Word, newTopic: String) {
        // Здесь обработайте изменение темы
        // Например, обновите в базе данных
        println("Тема слова ${word.word} изменена на: $newTopic")
    }

}