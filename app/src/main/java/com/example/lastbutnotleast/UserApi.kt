package com.example.lastbutnotleast

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface UserApi {
    @GET("users")
    fun getUsers(@Query("page") page: Int = 1): Single<UserResponse>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: Long): Completable

    @POST("users")
    fun createUser(@Body body: CreateUserRequest): Completable
}
