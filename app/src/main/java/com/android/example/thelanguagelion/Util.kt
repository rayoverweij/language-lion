package com.android.example.thelanguagelion

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException

/**
        General methods for use across the app
 */

// Return a file from the assets folder
@Throws(IOException::class)
fun getFileFromAssets(fileName: String, context: Context): File = File(context.cacheDir, fileName)
    .also {
        it.outputStream().use { cache ->
            context.assets.open(fileName).use {
                it.copyTo(cache)
            }
        }
    }


// Create a ViewHolder for the Recycler View in Notebook
class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)