package com.example.lastbutnotleast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
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
    val userToCreate by usersViewModel.userToCreate.observeAsState()

    when (networkStatus) {
        NetworkStatus.LOADING -> Loading()
        NetworkStatus.ERROR -> Text("Something went wrong") //todo: move o the center
        NetworkStatus.SUCCESS -> {
            LazyColumn {
                items(items = users ?: emptyList()) {
                    Card(modifier = Modifier.padding(4.dp).fillMaxWidth().clickable { usersViewModel.setUserIdToRemove(it.id) }) {
                        Text(text = it.name, style = TextStyle(fontSize = 16.sp), modifier = Modifier.padding(16.dp))
                    }
                }
            }
            AddUserButton(usersViewModel)
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
            onDismissRequest = { usersViewModel.cancelUserRemove() },
            confirmButton = { Button(onClick = { usersViewModel.confirmUserRemove() }) { Text("Confirm") } },
            dismissButton = { Button(onClick = { usersViewModel.cancelUserRemove() }) { Text("Cancel") } },
            text = { Text("Are you sure you want to remove the user?") }
        )
    }
    userToCreate?.let { user ->
        Dialog(onDismissRequest = { usersViewModel.clearUserToCreate() }) {
            Box(modifier = Modifier.background(MaterialTheme.colors.background).padding(16.dp)) {
                Column {
                    TextField(value = user.name, onValueChange = { usersViewModel.updateUserToCreateName(it) }, label = { Text("Name") })
                    Spacer(modifier = Modifier.padding(8.dp))
                    TextField(value = user.email, onValueChange = { usersViewModel.updateUserToCreateEmail(it) }, label = { Text("Email") })
                }
            }
        }
    }
}

@Composable
private fun ShowMessage(message: String) = Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()

@Composable
fun Loading() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val ref = createRef()
        CircularProgressIndicator(modifier = Modifier.constrainAs(ref) {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        })
    }
}

@Composable
private fun AddUserButton(viewModel: UsersViewModel) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val ref = createRef()
        FloatingActionButton(modifier = Modifier.padding(16.dp).constrainAs(ref) {
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
        }, onClick = {viewModel.updateUserToCreateEmail("")}) { }
    }
}
