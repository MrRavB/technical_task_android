package com.example.lastbutnotleast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import io.reactivex.rxjava3.core.Single
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class UsersViewModelTest {

    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()
    @get:Rule
    val schedulerRule = RxImmediateSchedulerRule()

    @Test
    fun fetchUsersSuccessTest() {
        val api = mockk<UserApi>()
        val viewModel = UsersViewModel(api)
        val usersObserver = spyk<Observer<List<User>>>()
        val networkObserver = spyk<Observer<NetworkStatus>>()
        val users = listOf(USER_MOCK)

        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = users))

        viewModel.users.observeForever(usersObserver)
        viewModel.networkStatus.observeForever(networkObserver)

        viewModel.fetchUsers()

        verifyOrder {
            networkObserver.onChanged(NetworkStatus.LOADING)
            networkObserver.onChanged(NetworkStatus.SUCCESS)
            usersObserver.onChanged(users)
        }
    }
}
