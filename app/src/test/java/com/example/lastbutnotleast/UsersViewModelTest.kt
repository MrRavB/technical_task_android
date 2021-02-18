package com.example.lastbutnotleast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsersViewModelTest {

    private lateinit var api: UserApi
    private lateinit var usersObserver: Observer<List<User>>
    private lateinit var networkObserver: Observer<NetworkStatus>
    private lateinit var viewModel: UsersViewModel

    @get:Rule
    val executorRule = InstantTaskExecutorRule()
    @get:Rule
    val schedulerRule = RxImmediateSchedulerRule()

    @Before
    fun setUp() {
        api = mockk()
        usersObserver = spyk()
        networkObserver = spyk()
        viewModel = UsersViewModel(api)
        viewModel.users.observeForever(usersObserver)
        viewModel.networkStatus.observeForever(networkObserver)
    }

    @Test
    fun fetchUsersSuccessTest() {
        val users = listOf(USER_MOCK)
        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = users))

        viewModel.fetchUsers()

        verifyOrder {
            networkObserver.onChanged(NetworkStatus.LOADING)
            networkObserver.onChanged(NetworkStatus.SUCCESS)
            usersObserver.onChanged(users)
        }
    }

    @Test
    fun fetchUsersFailureTest() {
        every { api.getUsers(any()) } returns Single.error(Throwable())

        viewModel.fetchUsers()

        verify { networkObserver.onChanged(NetworkStatus.ERROR) }
    }
}
