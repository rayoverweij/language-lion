package com.android.example.thelanguagelion.ui.notebook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.database.StudentDatabase
import com.android.example.thelanguagelion.databinding.FragmentNotebookBinding

class NotebookFragment : Fragment() {
    private lateinit var binding: FragmentNotebookBinding
    private lateinit var viewModelFactory: NotebookViewModelFactory
    private lateinit var viewModel: NotebookViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notebook, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = StudentDatabase.getInstance(application).studentDatabaseDao
        viewModelFactory = NotebookViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NotebookViewModel::class.java)

        binding.notebookViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SememeAdapter()
        binding.sememeList.adapter = adapter

        viewModel.sememes.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        return binding.root
    }
}
