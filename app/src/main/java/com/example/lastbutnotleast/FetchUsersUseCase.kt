package com.example.lastbutnotleast

import io.reactivex.rxjava3.core.Single

class FetchUsersUseCase(private val api: UserApi) {
    fun execute(): Single<List<User>> = fetchUsers(1)

    private fun fetchUsers(page: Int): Single<List<User>> =
        api.getUsers(page)
            .flatMap { response ->
                response.meta.pagination.pages.run {
                    if (this != page) fetchUsers(this) else Single.just(response.data)
                }}
}
