package com.example.lastbutnotleast

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test

class FetchUsersUseCaseTest {

    @MockK
    private lateinit var api: UserApi
    private lateinit var useCase: FetchUsersUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = FetchUsersUseCase(api)
    }

    @After
    fun after() {
        FetchUsersUseCase.lastPage = 1
    }

    @Test
    fun simpleUsersFetchTest() {
        val user1 = USER_MOCK.copy(name = "name1")
        val user2 = USER_MOCK.copy(name = "name2")
        every { api.getUsers(1) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = listOf(user1, user2)))

        val observer = TestObserver<List<User>>().apply {
            useCase.execute().subscribe(this)
        }

        observer.assertValue(listOf(user1, user2))
    }

    @Test
    fun lastUsersFetchTest() {
        val user1 = USER_MOCK.copy(name = "name1")
        val user2 = USER_MOCK.copy(name = "name2")
        val user3 = USER_MOCK.copy(name = "name3")
        every { api.getUsers(1) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(4)), data = listOf(user1, user2)))
        every { api.getUsers(4) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(4)), data = listOf(user3)))

        val observer = TestObserver<List<User>>().apply {
            useCase.execute().subscribe(this)
        }

        observer.assertValue(listOf(user3))
    }

    @Test
    fun cacheLastPageTest() {
        val user1 = USER_MOCK.copy(name = "user1")
        val user2 = USER_MOCK.copy(name = "user2")
        val user3 = USER_MOCK.copy(name = "user3")
        every { api.getUsers(1) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(pages = 3)), data = listOf(user1, user2)))
        every { api.getUsers(3) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(pages = 3)), data = listOf(user3)))

        TestObserver<List<User>>().apply {
            useCase.execute().subscribe(this)
        }

        TestObserver<List<User>>().apply {
            useCase.execute().subscribe(this)
        }
        verify(exactly = 3) { api.getUsers(any()) }
    }
}