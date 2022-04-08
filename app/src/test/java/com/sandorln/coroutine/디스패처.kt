package com.sandorln.coroutine

import kotlinx.coroutines.*
import org.junit.Test

class 디스패처 {

    /**
     * 특정 Thread 에서 suspend function 과 같은 일시 중단 함수를 부를시
     * 일시 중단 후 해당 일시 중단 함수 가 일어난 Thread 로 이동
     */
    @Test
    fun Unconfined_디스패처() {
        runBlocking {
            GlobalScope.launch(Dispatchers.Unconfined) {
                println("시작 쓰레드 이름 : ${Thread.currentThread().name}")
                delay(500)
                println("진행 중 쓰레드 이름 : ${Thread.currentThread().name}")

                /*
                * 처음에는 Main(Test worker) Thread 에서 시작했지만
                * 일시 중단 연산이 실행된 DefaultExecutor Thread 로 이동
                */
            }.join()
        }
    }
}