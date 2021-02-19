package com.example.lastbutnotleast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class UsersViewModel(private val api: UserApi): ViewModel() {

    private val _networkStatus = MutableLiveData<NetworkStatus>()
    private val _removeUserStatus = MutableLiveData<SingleLiveEvent<NetworkStatus>>()
    private val _users = MutableLiveData<List<User>>()
    private val _userIdToRemove = MutableLiveData<Long>()
    private val _userToCreate= MutableLiveData<UserDraft>()

    val networkStatus: LiveData<NetworkStatus> = _networkStatus
    val removeUserStatus: LiveData<SingleLiveEvent<NetworkStatus>> = _removeUserStatus
    val users: LiveData<List<User>> = _users
    val userIdToRemove: LiveData<Long> = _userIdToRemove
    val userToCreate: LiveData<UserDraft> = _userToCreate

    fun fetchUsers() {
        FetchUsersUseCase(api = api).execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _networkStatus.value = NetworkStatus.LOADING }
            .doOnSuccess { _networkStatus.value = NetworkStatus.SUCCESS }
            .doOnError { _networkStatus.value = NetworkStatus.ERROR }
            .subscribe(_users::setValue) { }
    }

    fun removeUser(id: Long) {
        api.deleteUser(id = id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.LOADING) }
            .doOnComplete { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.SUCCESS) }
            .doOnError { _removeUserStatus.value = SingleLiveEvent(NetworkStatus.ERROR) }
            .subscribe({ fetchUsers() }, { })
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
}
