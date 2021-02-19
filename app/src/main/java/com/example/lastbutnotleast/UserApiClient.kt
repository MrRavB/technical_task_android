package com.example.lastbutnotleast

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

private const val HEADER_AUTHORIZATION = "Authorization"
private const val HEADER_AUTHORIZATION_PREFIX = "Bearer "
private const val ACCESS_TOKEN = "8d77b09716fb7bd15aca6ec40f96fb083834a522de857f2dbeea97e5b0c88f37"
private const val BASE_URL = "https://gorest.co.in/public-api/"

object UserApiClient {
    fun getUserApi(): UserApi {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(Interceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader(HEADER_AUTHORIZATION, HEADER_AUTHORIZATION_PREFIX + ACCESS_TOKEN)
                    .build())
            }).build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    private fun createConverter(): Converter.Factory =
        GsonConverterFactory.create(
            GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, DateTimeMarshaller()).create()
        )
}
