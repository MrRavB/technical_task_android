package com.example.lastbutnotleast

import io.reactivex.rxjava3.core.Single

interface UserApi {
    fun getUsers(page: Int): Single<UserResponse>
}
