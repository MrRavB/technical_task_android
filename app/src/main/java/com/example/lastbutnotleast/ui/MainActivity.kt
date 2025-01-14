package com.example.lastbutnotleast.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import com.example.lastbutnotleast.api.UserApi
import com.example.lastbutnotleast.api.UserApiClient
import com.example.lastbutnotleast.model.NetworkStatus

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        val viewModel = createViewModel(UserApiClient.getUserApi()).apply {
            if (savedInstanceState == null) {
                fetchUsers()
            }
        }

        setContent {
            MaterialTheme {
                Users(usersViewModel = viewModel)
            }
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
    val createUserStatus by usersViewModel.createUserStatus.observeAsState()

    when (networkStatus) {
        NetworkStatus.LOADING -> Loading()
        NetworkStatus.ERROR -> RefreshError(usersViewModel)
        NetworkStatus.SUCCESS -> {
            LazyColumn {
                items(items = users ?: emptyList()) { user ->
                    Card(modifier = Modifier.padding(4.dp).fillMaxWidth().pointerInput { detectTapGestures(onLongPress = { usersViewModel.setUserIdToRemove(user.id) }) } ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val duration = Duration.between(LocalDateTime.now(), user.createdAt).toMillis().toString()
                            Text(text = user.name, style = TextStyle(fontSize = 16.sp))
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = user.email, style = TextStyle(fontSize = 12.sp))
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = "Created $duration ms. ago", style = TextStyle(fontSize = 10.sp))
                        }
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

    userIdToRemove?.let {
        AlertDialog(
            onDismissRequest = { usersViewModel.cancelUserRemove() },
            confirmButton = { Button(onClick = { usersViewModel.confirmUserRemove() }) { Text("Confirm") } },
            dismissButton = { Button(onClick = { usersViewModel.cancelUserRemove() }) { Text("Cancel") } },
            text = { Text("Are you sure you want to remove the user?") }
        )
    }

    createUserStatus?.getContentIfNotHandled()?.let {
        when (it) {
            NetworkStatus.SUCCESS -> ShowMessage("User created")
            NetworkStatus.LOADING -> ShowMessage("Creating user")
            NetworkStatus.ERROR -> ShowMessage("Create user error")
        }
    }

    userToCreate?.let { user ->
        Dialog(onDismissRequest = { usersViewModel.clearUserToCreate() }) {
            Box(modifier = Modifier.background(MaterialTheme.colors.background).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    TextField(value = user.name,
                        onValueChange = { usersViewModel.updateUserToCreateName(it) },
                        label = { Text("Name") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    TextField(value = user.email,
                        onValueChange = { usersViewModel.updateUserToCreateEmail(it) },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(horizontalArrangement = Arrangement.End) {
                        Button(onClick = { usersViewModel.clearUserToCreate() }) { Text("Cancel") }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Button(onClick = { usersViewModel.confirmCreateUser() }) { Text("Confirm") }
                    }
                }
            }
        }
    }
}

@Composable
fun RefreshError(viewModel: UsersViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().clickable { viewModel.fetchUsers() },
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Something went wrong")
        Text(text = "Click to refresh")
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
        }, onClick = { viewModel.updateUserToCreateEmail("")} ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "add user")
        }
    }
}
