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
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private var currentTopic: String = "Default"
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
            onDeleteClick = { viewModel.deleteWord(it) },
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val word = wordEditText.text.toString().trim()
                val translate = translateEditText.text.toString().trim()

                if (word.isEmpty() || translate.isEmpty()) {
                    if (word.isEmpty()) wordEditText.error = "Enter word"
                    if (translate.isEmpty()) translateEditText.error = "Enter translation"
                    return@setOnClickListener
                }

                if (item == null) {
                    // СОЗДАЕМ НОВОЕ СЛОВО С ВЫБРАННЫМ ТОПИКОМ
                    val newWord = Word(
                        word = word,
                        translate = translate,
                        topic = currentTopic // Записываем текущий топик из спиннера
                    )
                    viewModel.insertWord(newWord)
                    reloadCurrentTopicData()
                } else {
                    // Обновляем существующее слово (топик остается прежним)
                    val updatedWord = item.copy(word = word, translate = translate)
                    viewModel.updateWord(updatedWord)
                    reloadCurrentTopicData()
                }

                // ПОСЛЕ СОХРАНЕНИЯ ПЕРЕЗАГРУЖАЕМ СЛОВА ДЛЯ ТЕКУЩЕГО ТОПИКА
                //reloadCurrentTopicData()

                dialog.dismiss()
            }
        }
        //reloadCurrentTopicData()
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