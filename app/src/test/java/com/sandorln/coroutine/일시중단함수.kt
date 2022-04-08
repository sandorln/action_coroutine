package com.sandorln.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Test

class 일시중단함수 {
    data class Profile(
        val id: Long,
        val name: String,
        val age: Long
    )

    interface ProfileServiceRepository {
        fun asyncFetchByName(name: String): Deferred<Profile>
        fun asyncFetchById(id: Long): Deferred<Profile>

        suspend fun suspendFetchByName(name: String): Profile
        suspend fun suspendFetchById(id: Long): Profile
    }

    class ProfileServiceClient : ProfileServiceRepository {
        override fun asyncFetchByName(name: String): Deferred<Profile> = GlobalScope.async { Profile(1, name, 28) }
        override fun asyncFetchById(id: Long): Deferred<Profile> = GlobalScope.async { Profile(id, "Susan", 28) }

        override suspend fun suspendFetchByName(name: String): Profile = Profile(99, name, 34)
        override suspend fun suspendFetchById(id: Long): Profile = Profile(id, "sandorln", 34)
    }

    @Test
    fun Async로_처리시() {
        runBlocking {
            val client = ProfileServiceClient()

            val profileById = client.asyncFetchById(12)
            val profileByName = client.asyncFetchByName("sandorln")

            /* 동시성으로 처리 */
            println("profileById = ${profileById.await()} , profileByName = ${profileByName.await()}")
        }
    }

    @Test
    fun Suspend로_처리시() {
        runBlocking {
            val client = ProfileServiceClient()

            val profileById = client.suspendFetchById(12)
            val profileByName = client.suspendFetchByName("sandorln")

            /* 일시 중지 함수로 처리 */
            println("profileById = $profileById , profileByName = $profileByName")
        }
    }
}