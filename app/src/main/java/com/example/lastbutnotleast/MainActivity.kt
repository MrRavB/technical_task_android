package com.example.lastbutnotleast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = UserApiClient.getUserApi()
        val viewModel = createViewModel(api).apply {
            if (savedInstanceState == null) {
                fetchUsers()
            }
        }

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
                    Card(modifier = Modifier.padding(4.dp).fillMaxWidth().clickable {  }) {
                        Text(text = it.name, style = TextStyle(fontSize = 16.sp), modifier = Modifier.padding(16.dp))
                    }
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

