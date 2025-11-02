package com.example.duolingolite.ui.home

import android.R
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
import com.example.duolingolite.adapter.WordAdapter
import com.example.duolingolite.data.Word
import com.example.duolingolite.databinding.FragmentHomeBinding
import com.example.duolingolite.viewholder.WordViewHolder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: WordAdapter

    private var currentTopic: String = "Default"
    private val topics = mutableListOf("Default")

    // Поточне слово, яке відображається
    private var currentWord: Word? = null
    // Флаг, що вказує, чи показувати переклад
    private var translationVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Використовуємо фабрику або стандартний ViewModelProvider, якщо HomeViewModel конструктор приймає Application
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Налаштовуємо RecyclerView
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Ініціалізуємо адаптер.
        adapter = WordAdapter(
            items = emptyList(),
            showButtons = false,
            showTranslate = false,
            onEditClick = { /* Не використовується */ },
            onDeleteClick = { /* Не використовується */ },
            onTranslateClick = { word -> toggleTranslation() },
        )
        recyclerView.adapter = adapter

        // Ініціалізуємо Spinner
        setupSpinner()

        // Спостерігаємо за LiveData з HomeViewModel
        homeViewModel.currentWord.observe(viewLifecycleOwner) { word ->
            currentWord = word
            // Завжди при отриманні нового слова скидаємо показ перекладу
            translationVisible = false
            if (word != null) {
                adapter.updateData(listOf(word), showTranslate = false)
            } else {
                // Если слово не найдено, показываем сообщение
                adapter.updateData(emptyList())
                showErrorMessage("No words found for topic: $currentTopic")
            }
        }

        // Спостерігаємо за списком топиков
        homeViewModel.allTopics.observe(viewLifecycleOwner) { topicList ->
            updateTopicsList(topicList)
        }

        // Налаштовуємо кнопку, що отримує випадкове слово
        binding.button.setOnClickListener {
            if(currentTopic == "Default" || currentTopic == "" || currentTopic == null || currentTopic.isEmpty())
            homeViewModel.fetchRandomWord()
            else
                homeViewModel.fetchRandomWord(currentTopic)
        }

        return root
    }

    private fun setupSpinner() {
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            topics
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fragmentSpinner.adapter = spinnerAdapter

        binding.fragmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTopic = parent.getItemAtPosition(position) as String
                if (selectedTopic == "Create topic...") {
                    showCreateTopicDialog()
                } else {
                    currentTopic = selectedTopic
//                    // При изменении топика сразу загружаем новое слово
//                    fetchRandomWord()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не выбрано
            }
        }
    }

    private fun updateTopicsList(topicList: List<String>) {
        topics.clear()
        topics.add("Default")
        topics.addAll(topicList.filter { it != "Default" })
        topics.add("Create topic...") // Добавляем опцию создания нового топика

        // Обновляем адаптер Spinner
        (binding.fragmentSpinner.adapter as? ArrayAdapter<String>)?.clear()
        (binding.fragmentSpinner.adapter as? ArrayAdapter<String>)?.addAll(topics)

        // Устанавливаем текущий топик (если он есть в списке)
        val currentPosition = topics.indexOf(currentTopic)
        if (currentPosition >= 0) {
            binding.fragmentSpinner.setSelection(currentPosition)
        } else {
            binding.fragmentSpinner.setSelection(0) // Default
            currentTopic = "Default"
        }
    }

    private fun showCreateTopicDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Create New Topic")

        val input = EditText(requireContext())
        input.hint = "Enter topic name"
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Create") { dialog, which ->
            val newTopic = input.text.toString().trim()
            if (newTopic.isNotEmpty() && newTopic != "Default" && newTopic != "Create topic...") {
                // Добавляем новый топик в список и выбираем его
                if (!topics.contains(newTopic)) {
                    topics.add(topics.size - 1, newTopic) // Добавляем перед "Create topic..."
                    (binding.fragmentSpinner.adapter as? ArrayAdapter<String>)?.notifyDataSetChanged()
                }
                currentTopic = newTopic
                binding.fragmentSpinner.setSelection(topics.indexOf(newTopic))
            } else {
                // Если введен невалидный топик, возвращаемся к Default
                binding.fragmentSpinner.setSelection(0)
                currentTopic = "Default"
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            // При отмене возвращаемся к предыдущему топику
            val previousPosition = topics.indexOf(currentTopic).coerceAtLeast(0)
            binding.fragmentSpinner.setSelection(previousPosition)
        }

        dialogBuilder.show()
    }

    private fun showErrorMessage(message: String) {
        // Можно показать Toast или Snackbar
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Функція для перемикання відображення перекладу
    private fun toggleTranslation() {
        currentWord?.let { word ->
            translationVisible = !translationVisible
            adapter.updateData(listOf(word), showTranslate = translationVisible)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
