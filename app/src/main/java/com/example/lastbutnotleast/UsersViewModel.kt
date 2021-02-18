package com.example.lastbutnotleast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class UsersViewModel(private val api: UserApi): ViewModel() {

    private val _networkStatus = MutableLiveData<NetworkStatus>()
    private val _removeUserStatus = MutableLiveData<NetworkStatus>()
    private val _users = MutableLiveData<List<User>>()

    val networkStatus: LiveData<NetworkStatus> = _networkStatus
    val removeUserStatus: LiveData<NetworkStatus> = _removeUserStatus
    val users: LiveData<List<User>> = _users

    fun fetchUsers() {
        FetchUsersUseCase(api = api).execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _networkStatus.value = NetworkStatus.LOADING }
            .doOnSuccess { _networkStatus.value = NetworkStatus.SUCCESS }
            .doOnError { _networkStatus.value = NetworkStatus.ERROR }
            .subscribe(_users::setValue)
    }

    fun removeUser(id: Int) {
        api.deleteUser(id = id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _removeUserStatus.value = NetworkStatus.LOADING }
            .doOnComplete {
                _removeUserStatus.value = NetworkStatus.SUCCESS
                _removeUserStatus.value = null
            }
            .doOnError {
                _removeUserStatus.value = NetworkStatus.ERROR
                _removeUserStatus.value = null
            }
            .subscribe({ fetchUsers() }, { })
    }
}
