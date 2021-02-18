package com.example.lastbutnotleast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class UsersViewModel(private val api: UserApi): ViewModel() {

    private var _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users


    fun fetchUsers() {
        FetchUsersUseCase(api = api).execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _networkStatus.value = NetworkStatus.LOADING }
            .doOnSuccess { _networkStatus.value = NetworkStatus.SUCCESS }
            .subscribe(_users::setValue)
    }
}
