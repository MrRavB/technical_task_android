package com.example.lastbutnotleast.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lastbutnotleast.api.FetchUsersUseCase
import com.example.lastbutnotleast.api.UserApi
import com.example.lastbutnotleast.model.CreateUserRequest
import com.example.lastbutnotleast.model.NetworkStatus
import com.example.lastbutnotleast.model.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class UsersViewModel(private val api: UserApi, private val compositeDisposable: CompositeDisposable = CompositeDisposable()): ViewModel() {

    private val _networkStatus = MutableLiveData<NetworkStatus>()
    private val _removeUserStatus = MutableLiveData<SingleLiveEvent<NetworkStatus>>()
    private val _users = MutableLiveData<List<User>>()
    private val _userIdToRemove = MutableLiveData<Long>()
    private val _userToCreate = MutableLiveData<UserDraft>()
    private val _createUserStatus = MutableLiveData<SingleLiveEvent<NetworkStatus>>()

    val networkStatus: LiveData<NetworkStatus> = _networkStatus
    val removeUserStatus: LiveData<SingleLiveEvent<NetworkStatus>> = _removeUserStatus
    val users: LiveData<List<User>> = _users
    val userIdToRemove: LiveData<Long> = _userIdToRemove
    val userToCreate: LiveData<UserDraft> = _userToCreate
    val createUserStatus: LiveData<SingleLiveEvent<NetworkStatus>> = _createUserStatus

    fun fetchUsers() {
        compositeDisposable.add(
            FetchUsersUseCase(api = api).execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _networkStatus.value = NetworkStatus.LOADING }
            .doOnSuccess { _networkStatus.value = NetworkStatus.SUCCESS }
            .doOnError { _networkStatus.value = NetworkStatus.ERROR }
            .subscribe(_users::setValue) { })
    }

    fun removeUser(id: Long) {
        compositeDisposable.add(api.deleteUser(id = id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.LOADING) }
            .doOnComplete { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.SUCCESS) }
            .doOnError { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.ERROR) }
            .subscribe({ fetchUsers() }, { }))
    }

    fun setUserIdToRemove(id: Long) {
        _userIdToRemove.value = id
    }

    fun confirmUserRemove() {
        userIdToRemove.value?.let {
            removeUser(it)
            _userIdToRemove.value = null
        }
    }

    fun cancelUserRemove() {
        _userIdToRemove.value = null
    }

    fun updateUserToCreateName(name: String) {
        _userToCreate.value = (_userToCreate.value ?: UserDraft()).copy(name = name)
    }

    fun updateUserToCreateEmail(email: String) {
        _userToCreate.value = (_userToCreate.value ?: UserDraft()).copy(email = email)
    }

    fun clearUserToCreate() {
        _userToCreate.value = null
    }

    fun confirmCreateUser() {
        _userToCreate.value?.let {
            compositeDisposable.add(api.createUser(CreateUserRequest(name = it.name, email = it.email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { _createUserStatus.value = SingleLiveEvent(NetworkStatus.LOADING) }
                .doOnComplete {
                    _createUserStatus.value = SingleLiveEvent(NetworkStatus.SUCCESS)
                    _userToCreate.value = null
                }
                .doOnError { _createUserStatus.value = SingleLiveEvent(NetworkStatus.ERROR) }
                .subscribe({ fetchUsers() }, { }))
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
