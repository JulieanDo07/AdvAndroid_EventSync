package week11.st548490.finalproject.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import week11.st548490.finalproject.ui.expenses.EventExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    eventId: String,
    expenseViewModel: EventExpenseViewModel = viewModel()
)

{
    val expenseData by expenseViewModel.expenseData.collectAsState()

    // Load existing expense for this event
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

    LaunchedEffect(showSaveSuccess) {
        if (showSaveSuccess) {
            delay(1500)
            navController.popBackStack()
        }
    }

    //Main UI screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        //to scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Expense title
            OutlinedTextField(
                value = expenseData.title,
                onValueChange = { expenseViewModel.updateTitle(it) },
                label = { Text("Expense Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Divide by how many people field
            OutlinedTextField(
                value = expenseViewModel.divideByString(),
                onValueChange = { expenseViewModel.updateDivideBy(it) },
                label = { Text("Divided By (People)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Items list
            Text("Expense Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.LightGray)
            ) {
                items(expenseData.items) { item ->
                    val index = expenseData.items.indexOf(item)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = {
                                expenseViewModel.updateItemName(index, it)
                            },
                            placeholder = { Text("Item name") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = item.price.toString(),
                            onValueChange = {
                                expenseViewModel.updateItemPrice(index, it)
                            },
                            placeholder = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // button to add new item
            Button(
                onClick = { expenseViewModel.addItem() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Item")
            }

            Spacer(Modifier.height(16.dp))

            // Total cost
            OutlinedTextField(
                value = String.format(Locale.US, "$%.2f", expenseData.totalCost),
                onValueChange = {},
                readOnly = true,
                label = { Text("Total Cost") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Select attendees
            Text("Split With", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDFFFE5))
                    .clickable { showAttendeeDialog = true }
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Text("Select Attendees", fontWeight = FontWeight.Medium)
            }
                //show list of attendees
            if (expenseData.attendees.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column {
                    expenseData.attendees.forEach { attendee ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(attendee)
                            IconButton(
                                onClick = {
                                    val updated = expenseData.attendees.toMutableList()
                                    updated.remove(attendee)
                                    expenseViewModel.updateAttendees(updated)
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cost per person
            OutlinedTextField(
                value = String.format(Locale.US, "$%.2f", expenseData.costPerPerson),
                onValueChange = {},
                readOnly = true,
                label = { Text("Cost Per Person") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            if (showSaveSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    //save successfully pop up
                    Text(
                        "✓ Expense saved successfully!",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            //save button
            Button(
                onClick = {
                    expenseViewModel.saveExpense(eventId) { success ->
                        showSaveSuccess = success
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    //dialog pop for attendees
    @Composable
    fun AttendeeSelectionDialog(
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EFFA))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Select Attendees", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(attendees) { person ->
                            val isSelected = tempSelected.contains(person)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .clickable {
                                        if (isSelected) tempSelected.remove(person)
                                        else tempSelected.add(person)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFBAE6FD)
                                    else Color(0xFFF5F5F5)
                                )
                            ) {
                                // cancel, confirm  buttons
                                Row(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(person)
                                    Spacer(Modifier.weight(1f))
                                    if (isSelected) Text("✓", color = Color.Green)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = onDismiss) { Text("Cancel") }
                        Button(onClick = { onConfirm(tempSelected.toList()) }) {
                            Text("Confirm (${tempSelected.size})")
                        }
                    }
                }
            }
        }
    }

    if (showAttendeeDialog) {
        AttendeeSelectionDialog(
            attendees = allPossibleAttendees,
            selected = expenseData.attendees,
            onConfirm = { selectedList ->
                expenseViewModel.updateAttendees(selectedList)
                showAttendeeDialog = false
            },
            onDismiss = { showAttendeeDialog = false }
        )
    }
}
