package com.sandorln.coroutine

import kotlinx.coroutines.*
import org.junit.Test
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
}