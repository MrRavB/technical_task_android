package com.example.lastbutnotleast

class CreateUserRequest(val name: String,
                        val email: String,
                        val gender: String = "Female",
                        val status: String = "Active")
