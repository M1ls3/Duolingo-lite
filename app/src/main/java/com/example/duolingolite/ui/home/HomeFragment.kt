package com.example.duolingolite.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duolingolite.adapter.WordAdapter
import com.example.duolingolite.data.Word
import com.example.duolingolite.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: WordAdapter

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
        // showButtons = false – на головній сторінці немає кнопок редагування/видалення,
        // showTranslate = false – переклад спочатку не показується.
        adapter = WordAdapter(
            items = emptyList(),
            showButtons = false,
            showTranslate = false,
            onEditClick = { /* Не використовується */ },
            onDeleteClick = { /* Не використовується */ },
            onTranslateClick = { word -> toggleTranslation() },
            topics = emptyList()
        )
        recyclerView.adapter = adapter

        // Спостерігаємо за LiveData з HomeViewModel
        homeViewModel.currentWord.observe(viewLifecycleOwner) { word ->
            currentWord = word
            // Завжди при отриманні нового слова скидаємо показ перекладу
            translationVisible = false
            adapter.updateData(listOf(word), showTranslate = false)
        }

        // Налаштовуємо кнопку, що отримує випадкове слово
        binding.button.setOnClickListener {
            homeViewModel.fetchRandomWord()
        }

        return root
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
