package com.sandorln.coroutine

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.concurrent.CountDownLatch
import javax.xml.parsers.DocumentBuilderFactory

/**
 * documentBuilderFactory 가 TEST CODE 에서 작동하지 않아서 TEST 를 거치지 못했음
 */
class RSS {
    var countDownLatch = CountDownLatch(1)
    private lateinit var customDispatcher: ExecutorCoroutineDispatcher
    private lateinit var documentBuilderFactory: DocumentBuilderFactory

    @Test
    fun RSS_값_가져오기() {
        customDispatcher = newSingleThreadContext("ServiceCall")
        documentBuilderFactory = DocumentBuilderFactory.newInstance()
        var headlines: List<String> = mutableListOf()

        GlobalScope.launch(customDispatcher) {
            headlines = fetchRssHeadlines()
            countDownLatch.countDown()
        }

        countDownLatch.await()
        println("뉴스 헤드 라인 : $headlines")
    }

    private fun fetchRssHeadlines(): List<String> {
        val builder = documentBuilderFactory.newDocumentBuilder()
        println("RSS 값 가져오기 시작")
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        println("RSS 값 가져오기 성공")

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