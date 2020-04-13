package com.android.example.thelanguagelion.ui.notebook

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.TextItemViewHolder
import com.android.example.thelanguagelion.database.Sememe

class SememeAdapter: RecyclerView.Adapter<TextItemViewHolder>() {
    var data = listOf<Sememe>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = item.sememeId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.text_item_view, parent, false) as TextView
        return TextItemViewHolder(view)
    }
}