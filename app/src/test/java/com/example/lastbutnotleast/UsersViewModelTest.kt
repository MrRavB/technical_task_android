package com.example.lastbutnotleast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsersViewModelTest {

    private lateinit var api: UserApi
    private lateinit var usersObserver: Observer<List<User>>
    private lateinit var networkObserver: Observer<NetworkStatus>
    private lateinit var removeUserStatusObserver: Observer<NetworkStatus>

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
        removeUserStatusObserver = spyk()
        viewModel = UsersViewModel(api)
        viewModel.users.observeForever(usersObserver)
        viewModel.networkStatus.observeForever(networkObserver)
        viewModel.removeUserStatus.observeForever(removeUserStatusObserver)
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

    @Test
    fun deleteUserSuccessTest() {
        every { api.deleteUser(10) } returns Completable.complete()

        viewModel.removeUser(10)

        verifyOrder {
            removeUserStatusObserver.onChanged(NetworkStatus.LOADING)
            removeUserStatusObserver.onChanged(NetworkStatus.SUCCESS)
            removeUserStatusObserver.onChanged(null)
            api.getUsers(any())
        }
    }

    @Test
    fun deleteUserFailureTest() {
        every { api.deleteUser(13) } returns Completable.error(Throwable())

        viewModel.removeUser(13)

        verifyOrder {
            removeUserStatusObserver.onChanged(NetworkStatus.LOADING)
            removeUserStatusObserver.onChanged(NetworkStatus.ERROR)
            removeUserStatusObserver.onChanged(null)
        }
    }
}
