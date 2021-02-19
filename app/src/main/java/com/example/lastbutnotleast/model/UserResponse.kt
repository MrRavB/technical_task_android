package com.example.lastbutnotleast.model

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

class UserResponse(val meta: Meta, val data: List<User>)

class Meta(val pagination: Pagination)

class Pagination(val pages: Int)

data class User(val id: Long,
                val name: String,
                val email: String,
                @SerializedName("created_at") val createdAt: LocalDateTime)
