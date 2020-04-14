package com.android.example.thelanguagelion.ui.notebook

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.TextItemViewHolder
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.getFileFromAssets
import simplenlg.semantics.Semanticon

class SememeAdapter: RecyclerView.Adapter<TextItemViewHolder>() {
    var data = listOf<Sememe>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        //val semanticon = Semanticon(getFileFromAssets("semanticon.xml", holder.textView.context).absolutePath)
        val item = data[position]
        //Log.i("Adapter", item.sememeId)
        //Log.i("Adapter", semanticon.toString())
        //val sem = semanticon.getSem(item.sememeId)
        //Log.i("Adapter", sem.id)
        //holder.textView.text = sem.dutch[0]
        holder.textView.text = item.sememeId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.text_item_view, parent, false) as TextView
        return TextItemViewHolder(view)
    }
}