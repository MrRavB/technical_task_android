package com.example.lastbutnotleast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val removeUserStatus by usersViewModel.removeUserStatus.observeAsState()
    val userIdToRemove by usersViewModel.userIdToRemove.observeAsState()

    when (networkStatus) {
        NetworkStatus.LOADING -> Loading() //todo: move o the center
        NetworkStatus.ERROR -> Text("Something went wrong") //todo: move o the center
        NetworkStatus.SUCCESS -> {
            LazyColumn {
                items(items = users ?: emptyList()) {
                    Card(modifier = Modifier.padding(4.dp).fillMaxWidth().clickable { usersViewModel.setUserIdToRemove(it.id) }) {
                        Text(text = it.name, style = TextStyle(fontSize = 16.sp), modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        else -> {}
    }

    removeUserStatus?.getContentIfNotHandled()?.let {
        when (it) {
            NetworkStatus.SUCCESS -> ShowMessage("Removed")
            NetworkStatus.LOADING -> ShowMessage("Removing")
            NetworkStatus.ERROR -> ShowMessage("Remove user error")
        }
    }

    if (userIdToRemove != null) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { Button(onClick = { usersViewModel.confirmUserRemove() }) { Text("Confirm") } },
            dismissButton = { Button(onClick = { usersViewModel.cancelUserRemove() }) { Text("Cancel") } },
            text = { Text("Are you sure you want to remove the user?") }
        )
    }
}

@Composable
private fun ShowMessage(message: String) = Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()

@Composable
fun Loading() {
    Column(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

