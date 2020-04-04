package com.android.example.thelanguagelion.ui.notebook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.example.thelanguagelion.R

class NotebookFragment : Fragment() {

    private lateinit var notebookViewModel: NotebookViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notebookViewModel =
                ViewModelProviders.of(this).get(NotebookViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notebook, container, false)
        val textView: TextView = root.findViewById(R.id.text_notebook)
        notebookViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
