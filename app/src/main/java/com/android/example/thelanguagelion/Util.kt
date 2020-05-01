package com.android.example.thelanguagelion

import android.content.Context
import android.util.Log
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.Sentence
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Node
import java.io.File
import java.io.IOException
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

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


// Reset the database and add the basic profile
suspend fun resetDatabase(database: StudentDatabaseDao, context: Context) {
    withContext(Dispatchers.IO) {
        database.clearProfiles()
        database.clearSememes()
        database.clearSentences()

        val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
        val semanticonUri = File(semanticonPath).toURI()

        val grammarPath = getFileFromAssets("grammar.xml", context).absolutePath
        val grammarUri = File(grammarPath).toURI()

        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()

            var doc = builder.parse(semanticonUri.toString())
            if(doc != null) {
                val lexRoot = doc.documentElement
                val semNodes = lexRoot.childNodes
                for(i in 0 .. semNodes.length) {
                    val semNode = semNodes.item(i)
                    if(semNode?.nodeType == Node.ELEMENT_NODE) {
                        if(semNode.nodeName != "sem") continue
                        val semNodeAttrs = semNode.childNodes
                        for(j in 0 .. semNodeAttrs.length) {
                            val semNodeAttr = semNodeAttrs.item(j)
                            if(semNodeAttr?.nodeType == Node.ELEMENT_NODE) {
                                val attrName = semNodeAttr.nodeName.trim()
                                if(attrName == "id") {
                                    val value = semNodeAttr.textContent
                                    database.insert(Sememe(value))
                                }
                            }
                        }
                    }
                }
            }

            doc = builder.parse(grammarUri.toString())
            if(doc != null) {
                val lexRoot = doc.documentElement
                val sentenceNodes = lexRoot.childNodes
                for(i in 0 .. sentenceNodes.length) {
                    val sentenceNode = sentenceNodes.item(i)
                    if(sentenceNode?.nodeType == Node.ELEMENT_NODE) {
                        if(sentenceNode.nodeName != "sentence") continue
                        val sentenceNodeAttrs = sentenceNode.childNodes
                        for(j in 0 .. sentenceNodeAttrs.length) {
                            val sentenceNodeAttr = sentenceNodeAttrs.item(j)
                            if(sentenceNodeAttr?.nodeType == Node.ELEMENT_NODE) {
                                val attrName = sentenceNodeAttr.nodeName.trim()
                                if(attrName == "id") {
                                    val value = sentenceNodeAttr.textContent
                                    database.insert(Sentence(value))
                                }
                            }
                        }
                    }
                }
            }

        } catch (ex: Exception) {
            Log.e("Data", ex.toString())
        }

        database.update(Sememe("S0020000", 1))
        database.update(Sememe("S0020001", 1))
        database.update(Sememe("S0021000", 1))
        database.update(Sentence("G001", 1))

        val primQueue = LinkedList(
            listOf(
                "S0030000", "S0031001", "S0031002", "S0041000", "S0041001", "S0041015", "S0021002", "S0041002",
                "S0041003", "S0050000", "S0041005", "S0050001", "S0041006", "S0021004", "S0021006", "S0041007",
                "S0041008", "S0041016", "S0041009", "S0041017", "S0041010", "S0031003", "S0041011", "S0041018",
                "S0021009", "S0041012", "S0041019", "S0021012", "S0041013", "S0041020", "S0050002", "S0041014",
                "S0041021", "S0050002", "S0030001", "S0051000", "S0051001", "S0031004", "S0040000", "S0031005",
                "S0040001", "S0031006", "S0040002", "S0031007", "S0040003", "S0031008", "S0040004", "S0031012",
                "S0021001", "S0021003", "S0031013", "S0021005", "S0010000", "S0021007", "S0010001", "S0021008",
                "S0010002", "S0021010", "S0010003", "S0021013", "S0010004", "S0010005", "S0010006", "S0010007",
                "S0010008", "S0031009", "S0010009", "S0010010", "S0042000", "S0010011", "S0010012", "S0042001",
                "S0010013", "S0010014", "S0042002", "S0010015"
            )
        )
        database.insert(
            Profile(
            username = "Rayo",
            primaryQueue = primQueue
        )
        )
    }
}