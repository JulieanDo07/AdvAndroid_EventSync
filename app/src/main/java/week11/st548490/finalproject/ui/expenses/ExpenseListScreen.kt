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
import week11.st548490.finalproject.ui.events.EventExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
// Add this import at the top of your file
import androidx.compose.foundation.lazy.items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    expenseViewModel: EventExpenseViewModel = viewModel()
) {
    val expenseData by expenseViewModel.expenseData.collectAsState()
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
    val context = LocalContext.current

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Title of Cost Sheet
            OutlinedTextField(
                value = expenseData.title,
                onValueChange = { expenseViewModel.updateTitle(it) },
                label = { Text("Title of Cost Sheet") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divided by how many attendees
            OutlinedTextField(
                value = expenseData.divideBy,
                onValueChange = { expenseViewModel.updateDivideBy(it) },
                label = { Text("Divided by How Many People") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section title
            Text("Cost of Each Item", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Items table
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, Color.LightGray)
            ) {
                itemsIndexed(expenseData.items) { index, item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { expenseViewModel.updateItemName(index, it) },
                            placeholder = { Text("Enter Name of Item") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = item.price,
                            onValueChange = { expenseViewModel.updateItemPrice(index, it) },
                            placeholder = { Text("Price ($)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }
            }

            // Add cost item row button
            IconButton(
                onClick = { expenseViewModel.addItem() },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total cost
            OutlinedTextField(
                value = expenseData.totalCost,
                onValueChange = {},
                readOnly = true,
                label = { Text("Total Cost of Items") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add attendees section
            Text("Add Attendees to Split", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDFFFE5))
                    .clickable { showAttendeeDialog = true }
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Text("Add Attendees", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cost per person
            OutlinedTextField(
                value = expenseData.costPerPerson,
                onValueChange = {},
                readOnly = true,
                label = { Text("Cost Per Person") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save success message
            if (showSaveSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ Expenses saved successfully!",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    showSaveSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Expenses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // SHOW POPUP DIALOG
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
                    // FIX: Use items() function with the list as parameter
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