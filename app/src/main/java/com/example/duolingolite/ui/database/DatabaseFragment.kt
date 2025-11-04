package com.example.duolingolite.ui.database

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duolingolite.R
import com.example.duolingolite.adapter.WordAdapter
import com.example.duolingolite.data.Word
import com.example.duolingolite.databinding.FragmentDatabaseBinding
import com.example.duolingolite.factory.WordViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseFragment : Fragment() {

    private var _binding: FragmentDatabaseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: DatabaseViewModel
    private lateinit var adapter: WordAdapter
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    var currentTopic: String = "Default"
    private val topics = mutableListOf("Default")

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

        setupRecyclerView()
        setupSpinner()
        setupObservers()
        setupClickListeners()

        // Загружаем начальные данные
        viewModel.loadAllTopics()
        viewModel.loadItems()

        return root
    }

    private fun setupRecyclerView() {
        binding.fragmentRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WordAdapter(
            items = emptyList(),
            onEditClick = { showEditDialog(it) },
            onDeleteClick = { deleteWord(it) },
            onTranslateClick = { }
        )
        binding.fragmentRecyclerView.adapter = adapter
    }

    private fun showEditDialog(item: Word?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)
        val wordEditText = dialogView.findViewById<EditText>(R.id.editTextWord)
        val translateEditText = dialogView.findViewById<EditText>(R.id.editTextTranslate)

        item?.let {
            wordEditText.setText(it.word)
            translateEditText.setText(it.translate)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            saveButton.setOnClickListener {
                val word = wordEditText.text.toString().trim()
                val translate = translateEditText.text.toString().trim()

                if (word.isEmpty() || translate.isEmpty()) {
                    if (word.isEmpty()) wordEditText.error = "Enter word"
                    if (translate.isEmpty()) translateEditText.error = "Enter translation"
                    return@setOnClickListener
                }

                // Блокируем кнопку на время сохранения
                saveButton.isEnabled = false
                saveButton.text = "Saving..."

                // Запускаем в корутине чтобы дождаться завершения
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (item == null) {
                            // СОЗДАЕМ НОВОЕ СЛОВО
                            val newWord = Word(
                                word = word,
                                translate = translate,
                                topic = currentTopic
                            )
                            viewModel.insertWord(newWord) // Ждем завершения
                        } else {
                            // ОБНОВЛЯЕМ СУЩЕСТВУЮЩЕЕ СЛОВО
                            val updatedWord = item.copy(word = word, translate = translate)
                            viewModel.updateWord(updatedWord) // Ждем завершения
                        }

                        // ДАЕМ НЕМНОГО ВРЕМЕНИ БАЗЕ ДАННЫХ НА ОБРАБОТКУ
                        delay(100)

                        // ТЕПЕРЬ ПЕРЕЗАГРУЖАЕМ ДАННЫЕ
                        reloadCurrentTopicData()

                        // Закрываем диалог после успешного сохранения
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                            showToast("Saved successfully!")
                        }

                    } catch (e: Exception) {
                        // В случае ошибки разблокируем кнопку
                        withContext(Dispatchers.Main) {
                            saveButton.isEnabled = true
                            saveButton.text = "Save"
                            showToast("Error: ${e.message}")
                        }
                    }
                }
            }
        }

        dialog.show()
    }


    private fun setupSpinner() {
        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, topics)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fragmentSpinnerDB.adapter = spinnerAdapter

        binding.fragmentSpinnerDB.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTopic = parent.getItemAtPosition(position) as String

                when {
                    selectedTopic == "Create topic..." -> showCreateTopicDialog()
                    selectedTopic != currentTopic -> {
                        currentTopic = selectedTopic
                        loadWordsForCurrentTopic()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupObservers() {
        viewModel.wordItems.observe(viewLifecycleOwner) { words ->
            adapter.updateData(words, true)
        }

        viewModel.allTopics.observe(viewLifecycleOwner) { topicsFromDb ->
            updateSpinnerTopics(topicsFromDb)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddWord.setOnClickListener {
            showEditDialog(null)
        }

        binding.fabDeleteTopic.setOnClickListener {
            deleteCurrentTopic()
        }
    }


    private fun updateSpinnerTopics(topicsFromDb: List<String>) {
        topics.clear()
        topics.add("Default")
        topics.addAll(topicsFromDb.filter { it != "Default" })
        topics.add("Create topic...")

        spinnerAdapter.notifyDataSetChanged()

        // Восстанавливаем текущий выбор
        val currentPosition = topics.indexOf(currentTopic).takeIf { it >= 0 } ?: 0
        binding.fragmentSpinnerDB.setSelection(currentPosition)
        //reloadCurrentTopicData()
    }

    private fun loadWordsForCurrentTopic() {
        if (currentTopic == "Default") {
            viewModel.loadItems()
        } else {
            viewModel.loadItemsByTopic(currentTopic)
        }
    }

    private fun reloadCurrentTopicData() {
        loadWordsForCurrentTopic()
        // Также обновляем список топиков на случай если добавился новый
        viewModel.loadAllTopics()
    }

    private fun showCreateTopicDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Enter topic name"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Create Topic")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val newTopic = input.text.toString().trim()

                if (newTopic.isNotEmpty() && newTopic != "Default" && newTopic != "Create topic...") {
                    // Добавляем новый топик в список
                    if (!topics.contains(newTopic)) {
                        topics.add(topics.size - 1, newTopic)
                        spinnerAdapter.notifyDataSetChanged()
                    }

                    // Выбираем новый топик
                    currentTopic = newTopic
                    binding.fragmentSpinnerDB.setSelection(topics.indexOf(newTopic))

                    // Загружаем слова для нового топика (будет пустой список)
                    viewModel.loadItemsByTopic(newTopic)
                } else {
                    // Возвращаемся к Default при невалидном вводе
                    binding.fragmentSpinnerDB.setSelection(0)
                    currentTopic = "Default"
                    viewModel.loadItems()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Возвращаемся к предыдущему выбору
                binding.fragmentSpinnerDB.setSelection(topics.indexOf(currentTopic).coerceAtLeast(0))
            }
            .show()
    }

    private fun deleteCurrentTopic() {
        if (currentTopic == "Default") {
            showErrorMessage("Cannot delete Default topic")
            return
        }

        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Topic")
                .setMessage("Are you sure you want to delete topic '$currentTopic' and all its words?")
                .setPositiveButton("Delete") { dialog, which ->
                    viewModel.deleteTopic(currentTopic)
                    // После удаления переключаемся на Default
                    currentTopic = "Default"
                    if (::spinnerAdapter.isInitialized && topics.contains("Default")) {
                        binding.fragmentSpinnerDB.setSelection(topics.indexOf("Default"))
                    }
                    viewModel.loadItems()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteWord(word: Word) {

        // Запускаем в корутине чтобы дождаться завершения
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deleteWord(word) // Ждем завершения удаления

                // Даем немного времени базе данных на обработку
                delay(100)

                // Перезагружаем данные
                reloadCurrentTopicData()

                // Показываем сообщение об успехе
                showToast("Word deleted successfully")

            } catch (e: Exception) {
                showToast("Error deleting word: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        if (isAdded) {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}