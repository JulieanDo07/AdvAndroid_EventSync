package week11.st548490.finalproject.ui.location

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLocationScreen(navController: NavController) {

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedLocation by remember { mutableStateOf("No location selected") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search location...") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (searchQuery.text.isNotEmpty()) {
                    selectedLocation = searchQuery.text
                    Toast.makeText(context, "Location selected: ${selectedLocation}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Location")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Map Placeholder Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text("Map will appear here", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selectedLocation", selectedLocation)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Location")
        }
    }
}
