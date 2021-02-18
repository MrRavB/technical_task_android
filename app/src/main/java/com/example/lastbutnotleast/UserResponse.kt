package com.example.lastbutnotleast

import com.google.gson.annotations.SerializedName

class UserResponse(val meta: Meta, val data: List<User>)

class Meta(val pagination: Pagination)

class Pagination(val pages: Int)

data class User(val name: String,
                val email: String,
                @SerializedName("created_at") val createdAt: String)
