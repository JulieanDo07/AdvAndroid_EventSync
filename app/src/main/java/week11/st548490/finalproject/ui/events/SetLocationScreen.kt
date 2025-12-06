package week11.st548490.finalproject.ui.location

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SetLocationScreen(navController: NavController) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("Click on map to select location") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Default camera position
    val defaultLocation = LatLng(40.7128, -74.0060) // New York

    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 4f) // Start zoomed out
    }

    // Marker state
    val markerState = rememberMarkerState(position = selectedLocation ?: defaultLocation)

    // Update marker when location changes
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            markerState.position = it
        }
    }

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
            placeholder = { Text("Search for any location...") },
            singleLine = true,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Button
        Button(
            onClick = {
                if (searchQuery.text.isNotEmpty()) {
                    coroutineScope.launch {
                        isLoading = true
                        searchResults = searchOpenStreetMap(searchQuery.text)
                        isLoading = false

                        if (searchResults.isEmpty()) {
                            Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && searchQuery.text.isNotEmpty()
        ) {
            Text(if (isLoading) "Searching..." else "Search Location")
        }

        // Search results dropdown
        if (searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn {
                    items(searchResults) { result ->
                        Text(
                            text = result.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLocation = LatLng(result.latitude, result.longitude)
                                    selectedAddress = result.displayName
                                    searchQuery = TextFieldValue(result.displayName)
                                    searchResults = emptyList()
                                    keyboardController?.hide()
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        LatLng(result.latitude, result.longitude),
                                        15f
                                    )
                                    Toast.makeText(context, "Selected: ${result.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp)
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map Display - This is the REAL Google Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    mapToolbarEnabled = true,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    tiltGesturesEnabled = true
                ),
                onMapClick = { latLng ->
                    // When user clicks anywhere on the map
                    selectedLocation = latLng
                    selectedAddress = "Custom Location: ${"%.6f".format(latLng.latitude)}, ${"%.6f".format(latLng.longitude)}"

                    // Try to get address for the clicked location
                    coroutineScope.launch {
                        val address = reverseGeocode(latLng)
                        if (address != null) {
                            selectedAddress = address
                        }
                    }
                }
            ) {
                // Add marker if location is selected
                if (selectedLocation != null) {
                    Marker(
                        state = markerState,
                        title = selectedAddress,
                        draggable = true,
                        onClick = {
                            // When marker is clicked
                            false // Return false to show info window
                        },
                        onInfoWindowClick = {
                            Toast.makeText(context, "Location info clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Text(
            text = "How to use:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "1. Type and search for any location\n" +
                    "2. OR Click anywhere on the map\n" +
                    "3. Drag marker to adjust position",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Drag marker button (since onDragEnd isn't working properly in compose-maps)
        Button(
            onClick = {
                // Update location from current marker position
                selectedLocation = markerState.position
                selectedAddress = "Marker Position: ${"%.6f".format(markerState.position.latitude)}, ${"%.6f".format(markerState.position.longitude)}"
                Toast.makeText(context, "Marker position updated", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedLocation != null
        ) {
            Text("Update Marker Position")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected location info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedLocation != null) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Selected Location:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                selectedLocation?.let { location ->
                    Text(
                        text = "Coordinates: ${"%.6f".format(location.latitude)}, ${"%.6f".format(location.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cancel Button
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }

            // Save Button
            Button(
                onClick = {
                    selectedLocation?.let { location ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedLocation", selectedAddress)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedLatLng", location)
                        navController.popBackStack()
                        Toast.makeText(context, "✓ Location saved!", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(context, "Please select a location first", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = selectedLocation != null && !isLoading
            ) {
                Text("Save Location")
            }
        }
    }
}

// Data class for search results
data class SearchResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

// Search using OpenStreetMap Nominatim API - 100% FREE, no API key
private suspend fun searchOpenStreetMap(query: String): List<SearchResult> {
    return withContext(Dispatchers.IO) {
        try {
            // Clean the query for URL
            val cleanQuery = query.trim().replace(" ", "+")

            // OpenStreetMap Nominatim API URL
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=$cleanQuery&limit=10&addressdetails=1"

            // Make HTTP request
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "EventApp/1.0") // Required by OpenStreetMap
            connection.setRequestProperty("Accept", "application/json")

            val jsonString = connection.getInputStream().bufferedReader().use { it.readText() }

            if (jsonString.isEmpty()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonString)
            val results = mutableListOf<SearchResult>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val displayName = jsonObject.getString("display_name")
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")

                results.add(SearchResult(displayName, lat, lon))
            }

            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

// Reverse geocode using OpenStreetMap - also FREE
private suspend fun reverseGeocode(latLng: LatLng): String? {
    return withContext(Dispatchers.IO) {
        try {
            val lat = latLng.latitude
            val lon = latLng.longitude

            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1"

            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "EventApp/1.0")
            connection.setRequestProperty("Accept", "application/json")

            val jsonString = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = org.json.JSONObject(jsonString)

            jsonObject.getString("display_name")
        } catch (e: Exception) {
            // If reverse geocode fails, return coordinates
            "Location: ${"%.6f".format(latLng.latitude)}, ${"%.6f".format(latLng.longitude)}"
        }
    }
}