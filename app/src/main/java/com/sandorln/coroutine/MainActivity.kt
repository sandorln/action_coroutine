package com.sandorln.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sandorln.coroutine.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    lateinit var customDispatcher: ExecutorCoroutineDispatcher
    lateinit var documentBuilderFactory: DocumentBuilderFactory

    companion object {
        val feedUrls = listOf(
            "https://www.npr.org/rss/rss.php?id=1001",
            "http://rss.cnn.com/rss/cnn_topstories.rss",
            "http://feeds.foxnews.com/foxnews/politics?format=xml"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        customDispatcher = newSingleThreadContext("ServiceCall")
        documentBuilderFactory = DocumentBuilderFactory.newInstance()

        /* 1. 단일의 뉴스만 가져오는 형태 */
//        GlobalScope.launch(customDispatcher) {
//            val headlineList = fetchRssHeadlines()
//            withContext(Dispatchers.Main) {
//                binding.tvContent.text = "${headlineList.size} 개의 뉴스를 찾았습니다"
//            }
//        }

        /* 2. 여러개의 뉴스를 가져오는 형태 */
        asyncLoadNews()
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

    private fun asyncFetchRssHeadlines(feedUrl: String, dispatcher: CoroutineDispatcher): Deferred<List<String>> =
        GlobalScope.async(dispatcher) {
            val builder = documentBuilderFactory.newDocumentBuilder()
            val xml = builder.parse(feedUrl)
            val news = xml.getElementsByTagName("channel").item(0)
            return@async (0 until news.childNodes.length)
                .asSequence()
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map { it.getElementsByTagName("title").item(0).textContent }
                .toList()
        }

    /* 데이터를 동시에 가져오는 형태 */
    private fun asyncLoadNews() = GlobalScope.launch {
        val dispatcher = newFixedThreadPoolContext(2, "IO")
        val requests = mutableListOf<Deferred<List<String>>>()

        feedUrls.mapTo(requests) {
            asyncFetchRssHeadlines(it, dispatcher)
        }

        /* 각 코드가 완료될 때까지 대기 */
        val time = measureTimeMillis {
            requests.forEach { it.await() }
        }

        /* 값 뽑아오기 */
        val headlines = requests.flatMap {
            it.getCompleted()
        }

        withContext(Dispatchers.Main) {
            binding.tvContent.text = "지연시간 : $time ms\n총 ${headlines.size} 개의 뉴스를 ${requests.size} 피드에서 찾았다"
        }
    }
}