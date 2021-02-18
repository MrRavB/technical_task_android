package com.example.lastbutnotleast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = UserApiClient.getUserApi()
        val viewModel = createViewModel(api).apply { fetchUsers() }

        setContent {
            Users(usersViewModel = viewModel)
        }
    }

    private fun createViewModel(api: UserApi) =
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return UsersViewModel(api) as T
            }
        }).get(UsersViewModel::class.java)
}

@Composable
fun Users(usersViewModel: UsersViewModel) {
    val users by usersViewModel.users.observeAsState()
    val networkStatus by usersViewModel.networkStatus.observeAsState()

    when (networkStatus) {
        NetworkStatus.LOADING -> Loading()
        NetworkStatus.ERROR -> TODO()
        NetworkStatus.SUCCESS -> {
            LazyColumn {
                items(items = users ?: emptyList()) {
                    Text(text = it.name)
                }
            }
        }
    }
}

@Composable
fun Loading() {
    Column(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

