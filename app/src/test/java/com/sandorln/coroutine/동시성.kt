package com.sandorln.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * 동시성 관련 용어
 *
 * '동시성'
 * - 두 개이상의 알고리즘의 실행 시간이 겹쳐질 시 발생
 * - 병렬(두 개이상 스레드/코어 존재 필요)와는 다르게 단일 코어에서 다른 스레드의 인스트럭션을 교차 배치(겹쳐서)하여 실행
 *
 * 'suspend function'
 * - 함수가 실행 되는 동안 해당 코루틴을 일시 정지 시킨 후 함수가 완료 될 시 다시 실행
 * - 비동기 작업을 순차적으로 변경
 *
 * '레이스 컨디션'
 * - 동시성 코드가 제대로 작동하기 위해 일정한 순서로 완료 되어야할 시 오류 발생
 *
 */
class 동시성 {
    @Test
    fun 순차적_GetProfile() {
        runBlocking {
            val time = measureTimeMillis {
                val userInfo = 순차_getUserInfo()
                val contractInfo = 순차_getContractInfo()

                val profile = 순차_createProfile(userInfo, contractInfo)

                println("만들어진 프로필 : $profile")
            }
            println("최종 걸린 시간 : $time ms")
        }
    }

    private suspend fun 순차_getUserInfo(): String {
        println("시작 - 사용자 정보 가져오기")
        delay(1000)
        println("끝 - 사용자 정보 가져오기")
        return "사용자 1"
    }

    private suspend fun 순차_getContractInfo(): String {
        println("시작 - 연락처 가져오기")
        delay(1000)
        println("끝 - 연락처 가져오기")
        return "010-1234-5678"
    }

    private suspend fun 순차_createProfile(userInfo: String, contractInfo: String): String {
        println("시작 - 사용자 프로필 만들기")
        delay(1000)
        println("끝 - 사용자 프로필 만들기")
        return "$userInfo & $contractInfo"
    }

    @Test
    fun 동시성처리_GetProfile() {
        runBlocking {
            val time = measureTimeMillis {
                val userInfo = async { 순차_getUserInfo() }
                val contractInfo = async { 순차_getContractInfo() }

                val profile = 순차_createProfile(userInfo.await(), contractInfo.await())

                println("만들어진 프로필 : $profile")
            }
            println("최종 걸린 시간 : $time ms")
        }
    }

    @Test
    fun 흔한오류_레이스컨디션() {
        runBlocking {
            레이스컨디션_getUserData()
            delay(1000)
            println("사용자 정보 : ${userData.id}, ${userData.name}")
        }
    }

    data class UserData(val id: String, val name: String)

    lateinit var userData: UserData
    private fun 레이스컨디션_getUserData() = GlobalScope.async {
        delay(1100)
        userData = UserData("SanDorln", "김동순")
    }
}