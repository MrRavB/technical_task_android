package com.example.lastbutnotleast

import io.reactivex.rxjava3.core.Single

class FetchUsersUseCase(val api: UserApi) {
    fun execute(): Single<List<User>> {
        return api.getUsers(1).flatMap { response -> Single.just(response.data) }
    }
}
