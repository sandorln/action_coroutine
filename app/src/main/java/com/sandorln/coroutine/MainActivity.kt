package com.sandorln.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sandorln.coroutine.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    lateinit var customDispatcher: ExecutorCoroutineDispatcher
    lateinit var documentBuilderFactory: DocumentBuilderFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        customDispatcher = newSingleThreadContext("ServiceCall")
        documentBuilderFactory = DocumentBuilderFactory.newInstance()

        GlobalScope.launch(customDispatcher) {
            val headline = fetchRssHeadlines().toString()
            withContext(Dispatchers.Main) {
                binding.tvContent.text = headline
            }
        }
    }

    private fun fetchRssHeadlines(): List<String> {
        val builder = documentBuilderFactory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)
        return (0 until news.childNodes.length)
            .asSequence()
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { "item" == it.tagName }
            .map { it.getElementsByTagName("title").item(0).textContent }
            .toList()
    }
}