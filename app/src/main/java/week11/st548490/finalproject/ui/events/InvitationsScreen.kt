// InvitationsScreen.kt
package week11.st548490.finalproject.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import week11.st548490.finalproject.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(navController: NavController) {
    val invitationViewModel: InvitationViewModel = viewModel()
    val pendingInvitations by invitationViewModel.pendingInvitations.collectAsState()
    val actionState by invitationViewModel.invitationActionState.collectAsState()

    LaunchedEffect(Unit) {
        invitationViewModel.loadPendingInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Invitations") },
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
                .padding(16.dp)
        ) {
            // Show action state messages
            when (actionState) {
                is InvitationActionState.Success -> {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Success") },
                        text = { Text((actionState as InvitationActionState.Success).message) },
                        confirmButton = {
                            TextButton(onClick = { }) {
                                Text("OK")
                            }
                        }
                    )
                }
                is InvitationActionState.Error -> {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Error") },
                        text = { Text((actionState as InvitationActionState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { }) {
                                Text("OK")
                            }
                        }
                    )
                }
                else -> {}
            }

            if (pendingInvitations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = "No invitations",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No pending invitations",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingInvitations) { event ->
                        InvitationCard(
                            event = event,
                            onAccept = { invitationViewModel.acceptInvitation(event.id) },
                            onDecline = { invitationViewModel.declineInvitation(event.id) },
                            isLoading = actionState is InvitationActionState.Loading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationCard(
    event: week11.st548490.finalproject.data.models.Event,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(event.themeColor)).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(android.graphics.Color.parseColor(event.themeColor)),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = "Event",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date and time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${event.date.toDate().toString().substring(0, 10)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Time: ${event.time}",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accept/Decline buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ),
                    enabled = !isLoading
                ) {
                    Text("Decline")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(android.graphics.Color.parseColor(event.themeColor)),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    Text("Accept")
                }
            }
        }
    }
}