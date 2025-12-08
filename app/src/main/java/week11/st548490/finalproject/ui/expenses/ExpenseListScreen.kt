package week11.st548490.finalproject.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import week11.st548490.finalproject.ui.expenses.EventExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    eventId: String,
    expenseViewModel: EventExpenseViewModel = viewModel()
) {
    //load expense data when id event changes
    val expenseData by expenseViewModel.expenseData.collectAsState()

    LaunchedEffect(eventId) {
        expenseViewModel.loadExpenseForEvent(eventId)
    }

    val allPossibleAttendees = listOf(
        "Lando Norris",
        "Oscar Piastri",
        "Max Verstappen",
        "Juliean Do",
        "Atin Atin",
        "Simran Simran"
    )

    var showAttendeeDialog by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    //closes after saving
    LaunchedEffect(showSaveSuccess) {
        if (showSaveSuccess) {
            delay(1500)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Page", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Title
            item {
                OutlinedTextField(
                    value = expenseData.title,
                    onValueChange = { expenseViewModel.updateTitle(it) },
                    label = { Text("Title of Cost Sheet") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Divided by
            item {
                OutlinedTextField(
                    value = expenseData.divideBy.toString(),
                    onValueChange = { expenseViewModel.updateDivideBy(it) },
                    label = { Text("Divided by How Many People") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Section title
            item {
                Text("Cost of Each Item", fontWeight = FontWeight.Bold)
            }

            // Items list
            itemsIndexed(expenseData.items) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { expenseViewModel.updateItemName(index, it) },
                        placeholder = { Text("Enter Name of Item") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = item.price.toString(),
                        onValueChange = { expenseViewModel.updateItemPrice(index, it) },
                        placeholder = { Text("Price ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Add item button
            item {
                IconButton(
                    onClick = { expenseViewModel.addItem() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }

            // Total cost
            item {
                OutlinedTextField(
                    value = String.format(Locale.US, "%.2f", expenseData.totalCost),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total Cost of Items") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Attendee section
            item {
                Text("Add Attendees to Split", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDFFFE5))
                        .clickable { showAttendeeDialog = true }
                        .padding(16.dp)
                ) {
                    Text("Add Attendees")
                }
            }

            // Cost per person
            item {
                OutlinedTextField(
                    value = String.format(Locale.US, "%.2f", expenseData.costPerPerson),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cost Per Person") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save button
            item {
                Button(
                    onClick = { showSaveSuccess = true },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Expenses", fontWeight = FontWeight.Bold)
                }
            }

            // Success message
            if (showSaveSuccess) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Text(
                            "✓ Expenses saved successfully!",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
    // Pop up when saved
    if (showAttendeeDialog) {
        ExpenseAttendeeDialog(
            attendees = allPossibleAttendees,
            selected = expenseData.attendees,
            onConfirm = { newList ->
                expenseViewModel.updateAttendees(newList)
                showAttendeeDialog = false
            },
            onDismiss = { showAttendeeDialog = false }
        )
    }
}

@Composable
fun ExpenseAttendeeDialog(
    attendees: List<String>,
    selected: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected.toMutableList()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF4EFFA)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Select Users to Split",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attendees) { person ->
                        val isSelected = tempSelected.contains(person)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .clickable {
                                    if (isSelected) {
                                        tempSelected.remove(person)
                                    } else {
                                        tempSelected.add(person)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    Color(0xFFBAE6FD)
                                else
                                    Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(person, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Text("✓", color = Color.Green, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
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
                        onClick = { onConfirm(tempSelected.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        Text("Confirm (${tempSelected.size} selected)")
                    }
                }
            }
        }
    }
}