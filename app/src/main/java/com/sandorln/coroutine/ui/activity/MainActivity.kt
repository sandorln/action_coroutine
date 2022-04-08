package com.sandorln.coroutine.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.sandorln.coroutine.R
import com.sandorln.coroutine.databinding.ActivityMainBinding
import com.sandorln.coroutine.model.Article
import com.sandorln.coroutine.ui.adapter.ArticleAdapter
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    /* Adapters */
    lateinit var articleAdapter: ArticleAdapter

    lateinit var customDispatcher: ExecutorCoroutineDispatcher
    lateinit var documentBuilderFactory: DocumentBuilderFactory

    companion object {
//        val feedUrls = listOf(
//            "https://www.npr.org/rss/rss.php?id=1001",
//            "http://rss.cnn.com/rss/cnn_topstories.rss",
//            "http://feeds.foxnews.com/foxnews/politics?format=xml",
//            "http://rss.sandorln.com"
//        )

        val feedUrls: List<Pair<String, String>> = mutableListOf(
            Pair("npr", "https://www.npr.org/rss/rss.php?id=1001"),
            Pair("rss", "http://rss.cnn.com/rss/cnn_topstories.rss"),
            Pair("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
            Pair("sandorln", "http://rss.sandorln.com")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        customDispatcher = newSingleThreadContext("ServiceCall")
        documentBuilderFactory = DocumentBuilderFactory.newInstance()
        articleAdapter = ArticleAdapter()

        binding.rvArticle.adapter = articleAdapter

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

    /* 하나의 RSS 만 가져오는 형태 */
//    private fun fetchRssHeadlines(): List<String> {
//        val builder = documentBuilderFactory.newDocumentBuilder()
//        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
//        val news = xml.getElementsByTagName("channel").item(0)
//        return (0 until news.childNodes.length)
//            .asSequence()
//            .map { news.childNodes.item(it) }
//            .filter { Node.ELEMENT_NODE == it.nodeType }
//            .map { it as Element }
//            .filter { "item" == it.tagName }
//            .map { it.getElementsByTagName("title").item(0).textContent }
//            .toList()
//    }

    /* 여러개의 RSS 뉴스를 가져오는 형태 */
//    private fun asyncFetchRssHeadlines(feedUrl: String, dispatcher: CoroutineDispatcher): Deferred<List<String>> =
//        GlobalScope.async(dispatcher) {
//            val builder = documentBuilderFactory.newDocumentBuilder()
//            val xml = builder.parse(feedUrl)
//            val news = xml.getElementsByTagName("channel").item(0)
//            return@async (0 until news.childNodes.length)
//                .asSequence()
//                .map { news.childNodes.item(it) }
//                .filter { Node.ELEMENT_NODE == it.nodeType }
//                .map { it as Element }
//                .filter { "item" == it.tagName }
//                .map { it.getElementsByTagName("title").item(0).textContent }
//                .toList()
//        }

    /* 여러개의 RSS 뉴스를 구분지어 가져오는 형태 */
    private fun asyncFetchRssArticles(feedPair: Pair<String, String>, dispatcher: CoroutineDispatcher): Deferred<List<Article>> =
        GlobalScope.async(dispatcher) {
            val builder = documentBuilderFactory.newDocumentBuilder()
            val xml = builder.parse(feedPair.second)
            val news = xml.getElementsByTagName("channel").item(0)
            return@async (0 until news.childNodes.length)
                .asSequence()
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    var summary = it.getElementsByTagName("description").item(0).textContent

                    /* HTML 삭제 */
                    while (summary.contains("<div"))
                        summary = summary.substring(0, summary.indexOf("<div"))

                    Article(feedPair.first, title, summary)
                }
                .toList()
        }

    /* 데이터를 동시에 가져오는 형태 */
    private fun asyncLoadNews() = GlobalScope.launch {
        /* Loading Visible */
        withContext(Dispatchers.Main) { binding.pbLoading.isVisible = true }

        val dispatcher = newFixedThreadPoolContext(2, "IO")
//        val requests = mutableListOf<Deferred<List<String>>>()
        val requests = mutableListOf<Deferred<List<Article>>>()
        feedUrls.mapTo(requests) {
//            asyncFetchRssHeadlines(it, dispatcher)
            asyncFetchRssArticles(it, dispatcher)
        }

        /* 각 코드가 완료될 때까지 대기 */
        val time = measureTimeMillis {
            requests.forEach {
//                it.await() /* 예외 상태가 발생 시 곧바로 출력 */
                it.join() /* 예외 상태가 발생 하여도 곧바로 출력하지 않음 */
            }
        }

        /* 값 뽑아오기 */
        val articles = requests
            .filter { !it.isCancelled } /* 취소 된 작업에서는 결과를 받아오지 않도록 함 */
            .flatMap { it.getCompleted() }

        withContext(Dispatchers.Main) {
            delay(1000)
            binding.pbLoading.isVisible = false

            /* 지연시간 출력 */
            Toast.makeText(this@MainActivity, "지연시간 : $time ms", Toast.LENGTH_SHORT).show()
            articleAdapter.articles = articles
            articleAdapter.notifyDataSetChanged()
        }
    }
}