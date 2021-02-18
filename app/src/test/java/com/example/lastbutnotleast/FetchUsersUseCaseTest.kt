package com.example.lastbutnotleast

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Test

class FetchUsersUseCaseTest {

    @Test
    fun simpleFetchUsersTest() {
        val api = mockk<UserApi>()
        val user1 = User(name = "name1", email = "email1", createdAt = "createdAt1")
        val user2 = User(name = "name2", email = "email2", createdAt = "createdAt2")
        val useCase = FetchUsersUseCase(api)
        every { api.getUsers(1) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = listOf(user1, user2)))

        val observer = TestObserver<List<User>>().apply {
            useCase.execute().subscribe(this)
        }

        observer.assertValue(listOf(user1, user2))
    }
}
