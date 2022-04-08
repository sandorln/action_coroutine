package com.sandorln.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis

class 가벼운코루틴 {

    @Test
    fun 코루틴의속도() {
        runBlocking {
            println("시작 전 스레드개수 ${Thread.activeCount()}")
            val time = measureTimeMillis {
//                createCoroutines(1)
//                createCoroutines(10000)
                createCoroutines(100000)
            }
            println("끝난 후 스레드개수 ${Thread.activeCount()}")

            println("현재 걸린 속도 $time ms")
        }
    }

    private suspend fun createCoroutines(amount: Int) {
        val jobs = mutableListOf<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
                delay(1000)
            }
        }
        jobs.forEach {
            it.join()
        }
    }

    @Test
    fun 스레드에묶이지않는_코루틴() {
        runBlocking {
            val time = measureTimeMillis {
                createCoroutinesAndCallTreadName(3)
            }

            println("현재 걸린 속도 $time ms")
        }
    }

    private suspend fun createCoroutinesAndCallTreadName(amount: Int) {
        val jobs = mutableListOf<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
                println("시작한 코루틴 인덱스 : $i , 속해있는 스레드 ${Thread.currentThread().name}")
                delay(1000)
                println("끝이난 코루틴 인덱스 : $i , 속해있는 스레드 ${Thread.currentThread().name}")
            }
        }
        jobs.forEach {
            it.join()
        }
    }

    @Test
    fun 부모가_완료_되었을_시_자식_은_어떻게_되는가() {
        val countDownLatch = CountDownLatch(1)
        var child1: Job? = null
        var child2: Job? = null
        val parent = GlobalScope.launch {
            supervisorScope {
                println("현재 부모 시작")
                child1 = launch {
                    println("자식1 시작")
                    delay(3000)
                    println("자식1 끝")
                }
                child2 = launch(CoroutineExceptionHandler { _, exception ->
                    println(exception.message)
                }) {
                    println("자식2 시작")
                    throw Exception("에러 발생")
                    println("자식2 끝")
                }
                println("현재 부모 끝")
            }
        }

        GlobalScope.launch {
            delay(1000)
            println("기다리기")
            printlnJobStatus("parent", parent)
            printlnJobStatus("child1", child1)
            printlnJobStatus("child2", child2)
            delay(5000)
            println("기다리기 끝")
            printlnJobStatus("parent", parent)
            printlnJobStatus("child1", child1)
            printlnJobStatus("child2", child2)
            countDownLatch.countDown()
        }

        countDownLatch.await()
    }

    fun printlnJobStatus(jobName: String, job: Job?) {
        println("$jobName.isActive = ${job?.isActive}, $jobName.isCancelled = ${job?.isCancelled}, $jobName.isCompleted = ${job?.isCompleted}")
    }

    @Test
    fun 콜드_핫_플로우() {
        val countDownLatch = CountDownLatch(4)

        val coldFlow = flow {
            for (index in 0..10) {
                delay(1000)
                emit(index)
            }
        }

        val hotFlow = MutableStateFlow<Int>(0)

        GlobalScope.launch {
            launch {
                for (index in 0..10) {
                    delay(1000)
                    hotFlow.emit(index)
                }
            }

            launch {
                coldFlow.collect {
                    println("Cold Flow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
            launch {
                hotFlow.collect {
                    println("Hot Flow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
            delay(20000)
            launch {
                coldFlow.collect {
                    println("Re Cold Flow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
            launch {
                hotFlow.collect {
                    println("Re Hot Flow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
        }
        countDownLatch.await()
    }

    @Test
    fun stateFlow와sharedFlow차이() {
        val countDownLatch = CountDownLatch(4)
        val stateFlow = MutableStateFlow(0)
        val sharedFlow = MutableSharedFlow<Int>(replay = 5, onBufferOverflow = BufferOverflow.SUSPEND)

        GlobalScope.launch {
            launch {
                repeat(11) {
                    delay(1000)
                    stateFlow.emit(it)
                    sharedFlow.emit(it)
                }
            }

            launch {
                stateFlow.collect {
                    println("stateFlow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
            launch {
                sharedFlow.collect {
                    println("sharedFlow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }

            delay(15000)
            println("15초가 지난 후 다시")

            launch {
                stateFlow.collect {
                    println("RE stateFlow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
            launch {
                sharedFlow.collect {
                    println("RE sharedFlow 값 : $it")
                    if (it >= 10)
                        countDownLatch.countDown()
                }
            }
        }
        countDownLatch.await()
    }

    @Test
    fun collect관련처리() {
        val countDownLatch = CountDownLatch(1)
        val flow = flow<Int> {
            repeat(10) {
                println("emit 값 : $it")
                emit(it)
                delay(300)
            }
        }.shareIn(GlobalScope, SharingStarted.Eagerly)
        val stateFlow = flow<Int> {
            repeat(10) {
                println("emit 값 : $it")
                emit(it)
                delay(300)
            }
        }.stateIn(GlobalScope, SharingStarted.WhileSubscribed(), -1)

        GlobalScope.launch {
            /* Collect 상태 */
//            launch {
//                flow
//                    .collect {
//                        println("collect 현재 값 : $it")
//                        delay(1000)
//                        if (it >= 9)
//                            countDownLatch.countDown()
//                    }
//            }
            /* CollectLatest 상태 */
//            launch {
//                flow
//                    .collectLatest {
//                        println("collectLatest 현재 값 : $it")
//                        delay(1000)
//                        println("collectLatest delay 끝")
//                        if (it >= 9)
//                            countDownLatch.countDown()
//                    }
//            }

            /* CollectLatest & conflate 상태 */
//            launch {
//                flow
//                    .onEach {
//                        println("onEach conflate 현재 값 : $it")
//                    }
//                    .conflate()
//                    .collectLatest {
//                        println("collectLatest conflate 현재 값 : $it")
//                        delay(3000)
//                        println("collectLatest conflate delay 끝")
//                        if (it >= 9)
//                            countDownLatch.countDown()
//                    }
//            }
            /* Collect & Conflate 상태 */
//            launch {
//                flow
//                    .onEach {
//                        println("onEach 현재 값 : $it")
//                    }
//                    .conflate()
//                    .collect {
//                        println("collect 현재 값 : $it")
//                        delay(1000)
//                        println("collect 딜레이 끝")
//                        if (it >= 9)
//                            countDownLatch.countDown()
//                    }
//            }
            /* State Flow 는 자동으로 conflate 가 되어 있다고 함 (맞음) */
//            launch {
//                stateFlow
//                    .collect {
//                        println("state collect 현재 값 : $it")
//                        delay(1000)
//                        println("state collect 딜레이 끝")
//                        if (it >= 9)
//                            countDownLatch.countDown()
//                    }
//            }
        }
        countDownLatch.await()
    }
}