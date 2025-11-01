package com.example.duolingolite.ui.database

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duolingolite.R
import com.example.duolingolite.adapter.WordAdapter
import com.example.duolingolite.data.Word
import com.example.duolingolite.databinding.FragmentDatabaseBinding
import com.example.duolingolite.factory.WordViewModelFactory

class DatabaseFragment : Fragment() {

    private var _binding: FragmentDatabaseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: DatabaseViewModel
    private lateinit var adapter: WordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Ініціалізуємо ViewModel з фабрикою
        val application = requireNotNull(this.activity).application
        val factory = WordViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(DatabaseViewModel::class.java)

        // Ініціалізуємо RecyclerView
        val recyclerView = binding.fragmentRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Ініціалізуємо адаптер і зв'язуємо з RecyclerView
        adapter = WordAdapter(
            items = emptyList(), // Порожній список, поки не отримані дані
            onEditClick = { item ->
                showEditDialog(item)
            },
            onDeleteClick = { item ->
                viewModel.deleteWord(item)
            },
            onTranslateClick = {item -> null},

            topics = emptyList()
        )

        recyclerView.adapter = adapter

        // Спостерігаємо за змінами в LiveData з ViewModel
        viewModel.wordItems.observe(viewLifecycleOwner, { items ->
            adapter.updateData(items, true)
        })

        // Додаємо дію для кнопки додавання нового слова
        binding.fabAddWord.setOnClickListener {
            // Викликаємо діалог без параметрів для створення нового слова
            showEditDialog(null)
        }
        return root
    }

    // Метод для відображення діалогу редагування
    private fun showEditDialog(item: Word?) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)

        val wordEditText = dialogView.findViewById<EditText>(R.id.editTextWord)
        val translateEditText = dialogView.findViewById<EditText>(R.id.editTextTranslate)

        if (item != null) {
            // Якщо редагуємо існуюче слово, заповнюємо поля
            wordEditText.setText(item.word)
            translateEditText.setText(item.translate)
        }

        dialogBuilder.setView(dialogView)
            .setPositiveButton("Save", null) // Встановлюємо null, щоб переоприділити кнопку
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()

        // Встановлюємо слухача для кнопки "Save" вручну
        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val newWord = wordEditText.text.toString().trim()
                val newTranslate = translateEditText.text.toString().trim()

                var hasError = false

                // Перевіряємо чи поля не порожні
                if (newWord.isEmpty()) {
                    wordEditText.error = "Please enter a word"
                    hasError = true
                } else {
                    wordEditText.error = null // Забираємо помилку, якщо є текст
                }

                if (newTranslate.isEmpty()) {
                    translateEditText.error = "Please enter a translation"
                    hasError = true
                } else {
                    translateEditText.error = null
                }

                // Якщо немає помилок, зберігаємо або оновлюємо дані
                if (!hasError) {
                    if (item == null) {
                        // Створюємо нове слово і додаємо його в базу
                        val newWordEntry = Word(word = newWord, translate = newTranslate)
                        viewModel.insertWord(newWordEntry)
                    } else {
                        // Оновлюємо існуюче слово
                        val updatedWord = item.copy(word = newWord, translate = newTranslate)
                        viewModel.updateWord(updatedWord)
                    }

                    dialog.dismiss() // Закриваємо діалог після збереження
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}