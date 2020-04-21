package com.android.example.thelanguagelion.ui.notebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.database.Sememe
import simplenlg.framework.SemElement

class SememeAdapter: RecyclerView.Adapter<SememeAdapter.ViewHolder>() {

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val wordDutch: TextView = itemView.findViewById(R.id.word_dutch)
        private val wordEnglish: TextView = itemView.findViewById(R.id.word_english)

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item_notebook_word, parent, false)
                return ViewHolder(view)
            }
        }

        fun bind(item: SemElement) {
            wordDutch.text = item.dutch
                .toString()
                .replace("[", "")
                .replace("]", "")

            wordEnglish.text = item.english
                .toString()
                .replace("[", "")
                .replace("]", "")
        }
    }


    var data = listOf<SemElement>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }
}