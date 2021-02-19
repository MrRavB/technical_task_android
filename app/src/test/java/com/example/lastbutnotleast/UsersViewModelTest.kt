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
    private lateinit var removeUserStatusObserver: Observer<SingleLiveEvent<NetworkStatus>>
    private lateinit var userIdToRemoveObserver: Observer<Long>
    private lateinit var userToCreateObserver: Observer<UserDraft>
    private lateinit var createUserStatusObserver: Observer<SingleLiveEvent<NetworkStatus>>

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
        userIdToRemoveObserver = spyk()
        userToCreateObserver = spyk()
        createUserStatusObserver = spyk()
        viewModel = UsersViewModel(api)
        viewModel.users.observeForever(usersObserver)
        viewModel.networkStatus.observeForever(networkObserver)
        viewModel.removeUserStatus.observeForever(removeUserStatusObserver)
        viewModel.userIdToRemove.observeForever(userIdToRemoveObserver)
        viewModel.userToCreate.observeForever(userToCreateObserver)
        viewModel.createUserStatus.observeForever(createUserStatusObserver)
    }

    @Test
    fun fetchUsersSuccessTest() {
        val users = listOf(USER_MOCK)
        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = users))

        viewModel.fetchUsers()

        verifyOrder {
            api.getUsers(1)
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
            api.deleteUser(10)
            removeUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.LOADING))
            removeUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.SUCCESS))
            api.getUsers(any())
        }
    }

    @Test
    fun deleteUserFailureTest() {
        every { api.deleteUser(13) } returns Completable.error(Throwable())

        viewModel.removeUser(13)

        verifyOrder {
            api.deleteUser(13)
            removeUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.LOADING))
            removeUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.ERROR))
        }
    }

    @Test
    fun setUserIdToRemoveTest() {
        viewModel.setUserIdToRemove(10)

        verify { userIdToRemoveObserver.onChanged(10) }
    }

    @Test
    fun confirmUserRemoveTest() {
        every { api.deleteUser(15) } returns Completable.complete()
        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(1)), data = listOf()))

        viewModel.setUserIdToRemove(15)

        viewModel.confirmUserRemove()

        verify { api.deleteUser(15) }
        verify { userIdToRemoveObserver.onChanged(null) }
    }

    @Test
    fun cancelUserRemoveTest() {
        viewModel.setUserIdToRemove(30)

        viewModel.cancelUserRemove()

        verifyOrder {
            userIdToRemoveObserver.onChanged(30)
            userIdToRemoveObserver.onChanged(null)
        }
    }

    @Test
    fun updateUserToCreateNameTest() {
        viewModel.updateUserToCreateName("John")

        verify { userToCreateObserver.onChanged(UserDraft(name = "John")) }
    }

    @Test
    fun updateUserToCreateEmailTest() {
        viewModel.updateUserToCreateEmail("john@gmail.com")

        verify { userToCreateObserver.onChanged(UserDraft(email = "john@gmail.com")) }
    }

    @Test
    fun updateUserToCreateTest() {
        viewModel.updateUserToCreateName("John")
        viewModel.updateUserToCreateEmail("john@gmail.com")
        viewModel.updateUserToCreateName("Sylvester")

        verifyOrder {
            userToCreateObserver.onChanged(UserDraft(name = "John"))
            userToCreateObserver.onChanged(UserDraft(name = "John", email = "john@gmail.com"))
            userToCreateObserver.onChanged(UserDraft(name = "Sylvester", email = "john@gmail.com"))
        }
    }

    @Test
    fun clearUserToCreateTest() {
        viewModel.updateUserToCreateName("John")

        viewModel.clearUserToCreate()

        verifyOrder {
            userToCreateObserver.onChanged(UserDraft(name = "John"))
            userToCreateObserver.onChanged(null)
        }
    }

    @Test
    fun confirmUserCreateTest() {
        every { api.createUser(any()) } returns Completable.complete()
        viewModel.updateUserToCreateName("Name")
        viewModel.updateUserToCreateEmail("Email")

        viewModel.confirmCreateUser()

        verify { api.createUser(CreateUserRequest(name = "Name", email = "Email")) }
    }

    @Test
    fun createUserSuccessTest() {
        every { api.createUser(any()) } returns Completable.complete()
        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(3)), data = listOf()))

        viewModel.updateUserToCreateName("Name")
        viewModel.updateUserToCreateEmail("Email")

        viewModel.confirmCreateUser()

        verifyOrder {
            createUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.LOADING))
            createUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.SUCCESS))
            userToCreateObserver.onChanged(null)
            api.getUsers(3)
        }
    }

    @Test
    fun createUserFailureTest() {
        every { api.createUser(any()) } returns Completable.error(Throwable())
        every { api.getUsers(any()) } returns Single.just(UserResponse(meta = Meta(pagination = Pagination(3)), data = listOf()))

        viewModel.updateUserToCreateName("Name")
        viewModel.updateUserToCreateEmail("Email")

        viewModel.confirmCreateUser()

        verifyOrder {
            createUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.LOADING))
            createUserStatusObserver.onChanged(SingleLiveEvent(NetworkStatus.ERROR))
        }
        verify(exactly = 0) { api.getUsers(any()) }
    }
}
