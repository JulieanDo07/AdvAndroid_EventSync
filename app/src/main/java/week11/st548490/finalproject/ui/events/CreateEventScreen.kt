package week11.st548490.finalproject.ui.events

import androidx.compose.ui.window.Dialog
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import week11.st548490.finalproject.R
import week11.st548490.finalproject.data.models.User
import week11.st548490.finalproject.navigation.Screen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    val viewModel: CreateEventViewModel = viewModel()
    val users by viewModel.users.collectAsState()
    val createEventState by viewModel.createEventState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("#4FC3F7") } // Pastel blue default
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showUserSelection by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    val themeColors = listOf(
        "#4FC3F7", // Pastel Blue
        "#FFF176", // Pastel Yellow
        "#81C784", // Pastel Green
        "#F48FB1"  // Pastel Pink
    )

// Replace your LaunchedEffect with this
    val savedLocation by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("selectedLocation", "")
        ?.collectAsState() ?: remember { mutableStateOf("") }

    LaunchedEffect(savedLocation) {
        if (savedLocation.isNotEmpty()) {
            location = savedLocation
        }
    }


    val context = LocalContext.current

    val calendar = Calendar.getInstance()

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_sync_24),
                            contentDescription = "Sync Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Event", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Event Image Upload Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.event),
                        contentDescription = "Event Image",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "Tap to upload image",
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Event Name
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Event Name") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Description") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date and Time Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() },
                    placeholder = { Text("Select Date") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { datePicker.show() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = "Pick Date"
                            )
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Theme Selection
            Text(
                text = "Theme Color",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                themeColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(color)))
                            .border(
                                width = if (selectedTheme == color) 3.dp else 0.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                            .clickable { selectedTheme = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Invitees Section
            Text(
                text = "Invite People",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(android.graphics.Color.parseColor(selectedTheme)).copy(alpha = 0.2f))
                    .clickable { showUserSelection = true }
                    .padding(16.dp)
            ) {
                Text(
                    text = if (selectedUsers.isNotEmpty()) {
                        "Selected: ${selectedUsers.joinToString { it.displayName }}"
                    } else {
                        "Select from users"
                    },
                    color = Color.Black
                )
            }

            // Selected Users List
            if (selectedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedUsers) { user ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(android.graphics.Color.parseColor(selectedTheme)).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.displayName,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Edit/add expenses
            Button(
                onClick = {
                    navController.navigate(Screen.Expenses.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCFFFD5),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Edit Expenses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

// Set Location Button - Navigates to separate screen
            Button(
                onClick = {
                    navController.navigate(Screen.SetLocation.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme)),
                    contentColor = Color.White
                )
            ) {
                Text("Set Location")
            }




            Spacer(modifier = Modifier.height(32.dp))

            // Create Event Button
            Button(
                onClick = {
                    if (currentUser != null) {
                        viewModel.createEvent(
                            title = eventName,
                            description = description,
                            date = selectedDate,
                            time = selectedTime,
                            themeColor = selectedTheme,
                            budget = budget.toDoubleOrNull() ?: 0.0,
                            location = location,
                            creatorId = currentUser.uid,
                            invitedUserIds = selectedUsers.map { it.id }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                enabled = eventName.isNotEmpty() && createEventState !is CreateEventState.Loading
            ) {
                Text(
                    text = if (createEventState is CreateEventState.Loading) "Creating..." else "Create Event",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Error Message
            if (createEventState is CreateEventState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createEventState as CreateEventState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // User Selection Dialog
        if (showUserSelection) {
            UserSelectionDialog(
                users = users,
                selectedUsers = selectedUsers,
                onUsersSelected = {
                    selectedUsers = it
                    showUserSelection = false
                },
                onDismiss = {
                    showUserSelection = false
                }
            )
        }

    }
}

@Composable
fun UserSelectionDialog(
    users: List<User>,
    selectedUsers: List<User>,
    onUsersSelected: (List<User>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedUsers.toMutableList()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp), // Increased height
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Users to Invite",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Use LazyColumn instead of LazyRow for better mobile experience
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        val isSelected = tempSelected.any { it.id == user.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    if (isSelected) {
                                        tempSelected.removeAll { it.id == user.id }
                                    } else {
                                        tempSelected.add(user)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User",
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = user.email,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Green
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onUsersSelected(tempSelected.toList()) }
                    ) {
                        Text("Confirm (${tempSelected.size} selected)")
                    }
                }
            }
        }
    }
}