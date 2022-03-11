package com.sandorln.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test
import kotlin.system.measureTimeMillis

class 원자성 {

    var counter = 0

    /**
     * 원자성이 위반될 경우
     * - 각기 다른 코루틴에서 동일한 데이터를 동시에 수정될 수 있을 시 발생
     */
    @Test
    fun 원자성위반_카운터() {
        runBlocking {
            println("시작 전 카운터 $counter")

            val workerA = asyncIncrement(2000)
            val workerB = asyncIncrement(100)
            workerA.await()
            workerB.await()

            println("종료 후 카운터 $counter")
        }
    }

    fun asyncIncrement(by: Int) = GlobalScope.async {
        for (i in 0 until by) {
            counter++
        }
    }

    @Test
    fun 원자성_카운터() {
        runBlocking {
            val time = measureTimeMillis {
                println("시작 전 카운터 $counter")

                val workerA = mutexIncrement(2000)
                val workerB = mutexIncrement(100)
                workerA.await()
                workerB.await()

                println("종료 후 카운터 $counter")
            }

            println("현재 걸린 속도 $time ms")
        }
    }

    private val mutex = Mutex()
    private fun mutexIncrement(by: Int) = GlobalScope.async {
        for (i in 0 until by) {
            mutex.withLock {
                counter++
            }
        }
    }
}