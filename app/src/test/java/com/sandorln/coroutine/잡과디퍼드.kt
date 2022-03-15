package com.sandorln.coroutine

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

@InternalCoroutinesApi
class 잡과디퍼드 {
    @Test
    fun 잡_예외처리() = runBlocking {
        /* 1. 작업이 생성과 동시에 시작되는 상태 : 에러 발생 */
//        GlobalScope.launch {
//            TODO("NOT Implemented!")
//        }

        /* 2. 작업이 생성되었지만 시작한적이 없는 상태 : 에러 발생하지 않음 */
        GlobalScope.launch(start = CoroutineStart.LAZY) {
            TODO("NOT Implemented!")
        }
        delay(500)
    }

    @Test
    fun 잡_활성화상태() {
        /* 1. 완료되는 것을 기다리지 않고 실행을 끝냄 */
//        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
//            delay(3000)
//            println("기다리지 않고 해당 함수를 끝냄")
//        }
//        job.start()

        /* 2. 완료되는 것을 기다려서 실행을 끝냄 */
        runBlocking {
            val job = GlobalScope.launch {
                delay(3000)
                println("JOB 실행을 기다리고 함수를 끝냄")
            }
            job.join()
        }
        println("함수 실행 종료")
    }

    @Test
    fun 잡_취소상태() = runBlocking {
        /* 1. 취소 상태에 취소 이유 주기 */
//        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
//            delay(5000)
//        }
//
//        delay(2000)
//
//        /* 취소 */
//        job.cancel(cause = CancellationException("Tired of waiting"))
//        val cancellation = job.getCancellationException()
//        println(cancellation.message)

        /* 2. 핸들러 사용해서 이유 받아오기 */
//        val exceptionHandler = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
//            println("Job Cancel due to ${throwable.message}")
//        }
//        GlobalScope.launch(context = exceptionHandler) {
//            TODO("TODO 실행")
//        }
//        delay(2000)

        /* 3. invokeOnCompletion 로 이유 받아오기, 실패 */
        GlobalScope.launch {
            TODO("TODO 실행")
        }.invokeOnCompletion { cause ->
            cause?.let { throwable ->
                println("Job Cancel due to ${throwable.message}")
            }
        }
        delay(2000)
    }

    @Test
    fun 디퍼드_예외처리() {
        runBlocking {
            /* 1. 실행되지 않는 상태 */
//        val deferred = GlobalScope.async {
//            TODO("TODO 실행")
//        }
//        delay(2000)

            /* 2. 기다리며 실패하는 상태 */
//            val deferred = GlobalScope.async {
//                TODO("TODO 실행")
//            }
//            deferred.await()

            /* 3. try/catch 를 통해 throwable 처리 */
            try {
                val deferred = GlobalScope.async {
                    TODO("TODO 실행")
                }
                deferred.await()
            } catch (throwable: Throwable) {
                println("deferred throwable to ${throwable.message}")
            }
        }
    }

    @Test
    fun 상태는_한방향으로만() {
        runBlocking {
            val time = measureTimeMillis {
                val job = GlobalScope.launch {
                    delay(2000)
                }
                /* 2초 대기 */
                job.join()

                /* 다시한번 2초 대기 할 것 같지만 이미 완료 되었기 때문에 다시 돌아가지 않음 */
                job.start()
                job.join()
            }
            println("시간 흐름 : $time ms")
        }
    }
}