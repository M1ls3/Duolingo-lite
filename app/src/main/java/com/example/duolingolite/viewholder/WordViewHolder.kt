package com.example.duolingolite.viewholder

import android.view.View
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.duolingolite.R

class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val wordTextView: TextView = itemView.findViewById(R.id.wordTextView)
    val translateTextView: TextView = itemView.findViewById(R.id.translateTextView)
    val editButton: View = itemView.findViewById(R.id.buttonEdit)
    val deleteButton: View = itemView.findViewById(R.id.buttonDelete)
    val translateButton: View = itemView.findViewById(R.id.buttonTranslate)
    val fragmentSpinner: Spinner = itemView.findViewById(R.id.fragmentSpinner)
}