package week11.st548490.finalproject.ui.events

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import week11.st548490.finalproject.R
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: String
) {
    val viewModel: CreateEventViewModel = viewModel()
    val users by viewModel.users.collectAsState()
    val createEventState by viewModel.createEventState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var event by remember { mutableStateOf<Event?>(null) }
    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("#4FC3F7") }
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

    // Load event data
    LaunchedEffect(eventId) {
        viewModel.loadUsers()
        // Load event data - you'll need to implement this in your repository
        // val loadedEvent = eventRepository.getEvent(eventId)
        // event = loadedEvent
        // Pre-fill form with event data
        // eventName = loadedEvent?.title ?: ""
        // description = loadedEvent?.description ?: ""
        // selectedTheme = loadedEvent?.themeColor ?: "#4FC3F7"
        // budget = loadedEvent?.budget?.toString() ?: ""
        // location = loadedEvent?.location ?: ""
    }

    // Handle successful update
    LaunchedEffect(createEventState) {
        if (createEventState is CreateEventState.Success) {
            navController.popBackStack()
        }
    }

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
                        Text("Edit Event", fontWeight = FontWeight.Bold)
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
                },
                actions = {
                    IconButton(
                        onClick = {
                            // TODO: Implement delete event
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color.Red)
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
            // Same form as CreateEventScreen but pre-filled
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Date") },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Time") },
                    shape = RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Budget and Per Person Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Budget ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                val perPersonShare = if (budget.isNotEmpty() && selectedUsers.isNotEmpty()) {
                    String.format("%.2f", budget.toDoubleOrNull() ?: 0.0 / (selectedUsers.size + 1))
                } else "0.00"

                OutlinedTextField(
                    value = "$$perPersonShare",
                    onValueChange = { /* Read-only */ },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Per Person") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Set Location Button
            Button(
                onClick = { /* TODO: Open Google Maps */ },
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

            // Location Display
            if (location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: $location",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Changes Button
            Button(
                onClick = {
                    if (currentUser != null) {
                        viewModel.updateEvent(
                            eventId = eventId,
                            title = eventName,
                            description = description,
                            date = selectedDate,
                            time = selectedTime,
                            themeColor = selectedTheme,
                            budget = budget.toDoubleOrNull() ?: 0.0,
                            location = location,
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
                    text = if (createEventState is CreateEventState.Loading) "Saving..." else "Save Changes",
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
                onDismiss = { showUserSelection = false }
            )
        }
    }
}