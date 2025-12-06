package week11.st548490.finalproject.ui.location

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import java.net.URL

// Data class for geocoding results
data class GeocodeResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, eventLocation: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Sheridan College Trafalgar Campus coordinates
    val sheridanCollege = LatLng(43.4689, -79.6993)

    // State for destination coordinates
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var destinationAddress by remember { mutableStateOf(eventLocation ?: "Location not specified") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Geocode the event location to get coordinates
    LaunchedEffect(eventLocation) {
        if (!eventLocation.isNullOrEmpty()) {
            coroutineScope.launch {
                isLoading = true
                try {
                    val result = geocodeLocation(eventLocation)
                    if (result != null) {
                        destination = LatLng(result.latitude, result.longitude)
                        destinationAddress = result.displayName
                        errorMessage = null
                    } else {
                        errorMessage = "Could not find location: $eventLocation"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error loading location: ${e.message}"
                }
                isLoading = false
            }
        } else {
            errorMessage = "Event location not specified"
            isLoading = false
        }
    }

    // Camera position - show both locations if available
    val cameraPositionState = rememberCameraPositionState()

    // Update camera when destination is found
    LaunchedEffect(destination) {
        if (destination != null) {
            val dest = destination!!
            // Calculate midpoint between Sheridan and destination
            val midLat = (sheridanCollege.latitude + dest.latitude) / 2
            val midLng = (sheridanCollege.longitude + dest.longitude) / 2

            // Calculate zoom level based on distance
            val distance = calculateDistance(sheridanCollege, dest)
            val zoom = when {
                distance > 1000 -> 8f  // Far away, zoom out
                distance > 100 -> 10f  // Medium distance
                else -> 12f            // Close by
            }

            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(midLat, midLng),
                zoom
            )
        } else {
            // If no destination, just show Sheridan College
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                sheridanCollege,
                15f
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Directions",
                        fontWeight = FontWeight.Bold
                    )
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
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading directions...")
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Unable to load location",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (eventLocation != null) {
                            Button(
                                onClick = {
                                    // Try to open Google Maps directly
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(eventLocation)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback to web
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(eventLocation)}")
                                        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Directions,
                                    contentDescription = "Directions",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open in Google Maps")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text("Back to Event")
                        }
                    }
                }
            } else {
                // Route Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Route,
                                contentDescription = "Route",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Route Summary",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Sheridan College → $destinationAddress",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        destination?.let { dest ->
                            val distance = calculateDistance(sheridanCollege, dest)
                            val distanceText = when {
                                distance < 1 -> "${(distance * 1000).toInt()} meters"
                                else -> String.format("%.1f km", distance)
                            }
                            val estimatedTime = (distance / 50.0 * 60).toInt() // Assuming 50 km/h average
                            val timeText = if (estimatedTime < 60) "${estimatedTime} min" else "${estimatedTime / 60}h ${estimatedTime % 60}min"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Distance",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = distanceText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Duration",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = timeText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2)
                                    )
                                }
                            }
                        }
                    }
                }

                // Map View
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                mapType = MapType.NORMAL
                            ),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                compassEnabled = true,
                                mapToolbarEnabled = true,
                                zoomGesturesEnabled = true,
                                scrollGesturesEnabled = true,
                                rotationGesturesEnabled = true,
                                tiltGesturesEnabled = true
                            )
                        ) {
                            // Marker for Sheridan College
                            Marker(
                                state = rememberMarkerState(position = sheridanCollege),
                                title = "Sheridan College Trafalgar Campus",
                                snippet = "Starting point",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )

                            // Marker for destination
                            destination?.let { dest ->
                                Marker(
                                    state = rememberMarkerState(position = dest),
                                    title = "Event Location",
                                    snippet = destinationAddress,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                )
                            }
                            // Polyline removed - only showing markers
                        }
                    }
                }

                // Quick Directions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Directions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        destination?.let { dest ->
                            val bearing = calculateBearing(sheridanCollege, dest)
                            val direction = getDirectionFromBearing(bearing)

                            Text(
                                text = "1. Start from Sheridan College Trafalgar Campus",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "2. Head $direction towards the destination",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "3. Follow main roads to reach your destination",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "4. Use the button below for detailed navigation",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Navigation Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    destination?.let { dest ->
                        Button(
                            onClick = {
                                // Open Google Maps with directions
                                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1" +
                                        "&origin=${sheridanCollege.latitude},${sheridanCollege.longitude}" +
                                        "&destination=${dest.latitude},${dest.longitude}" +
                                        "&travelmode=driving")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    // Fallback to web browser
                                    val webUri = Uri.parse("https://www.google.com/maps/dir/" +
                                            "${sheridanCollege.latitude},${sheridanCollege.longitude}/" +
                                            "${dest.latitude},${dest.longitude}")
                                    val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4285F4),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Directions,
                                contentDescription = "Directions",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate with Google Maps")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Additional options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Copy coordinates to clipboard
                                    val coordinates = "${dest.latitude}, ${dest.longitude}"
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                            as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Coordinates", coordinates)
                                    clipboard.setPrimaryClip(clip)
                                    // Show toast
                                    android.widget.Toast.makeText(
                                        context,
                                        "Coordinates copied to clipboard",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Copy Coordinates")
                            }

                            OutlinedButton(
                                onClick = {
                                    navController.popBackStack()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back to Event")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tips section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F7FF)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Route,
                                    contentDescription = "Tip",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pro Tip:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                            Text(
                                text = "For real-time traffic updates, lane guidance, and voice navigation, use the 'Navigate with Google Maps' button above.",
                                fontSize = 12.sp,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Geocode a location string to coordinates
private suspend fun geocodeLocation(location: String): GeocodeResult? {
    return withContext(Dispatchers.IO) {
        try {
            // Clean the query for URL
            val cleanQuery = location.trim().replace(" ", "+")

            // OpenStreetMap Nominatim API URL
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=$cleanQuery&limit=1"

            // Make HTTP request
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "EventApp/1.0")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Referer", "https://eventapp.example.com")

            val jsonString = connection.getInputStream().bufferedReader().use { it.readText() }

            if (jsonString.isEmpty()) return@withContext null

            val jsonArray = JSONArray(jsonString)

            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val displayName = jsonObject.getString("display_name")
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")

                return@withContext GeocodeResult(displayName, lat, lon)
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Calculate distance between two coordinates in kilometers
private fun calculateDistance(start: LatLng, end: LatLng): Double {
    val earthRadius = 6371 // Earth's radius in kilometers

    val latDistance = Math.toRadians(end.latitude - start.latitude)
    val lonDistance = Math.toRadians(end.longitude - start.longitude)

    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
            Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}

// Calculate bearing between two points
private fun calculateBearing(start: LatLng, end: LatLng): Double {
    val lat1 = Math.toRadians(start.latitude)
    val lat2 = Math.toRadians(end.latitude)
    val lonDiff = Math.toRadians(end.longitude - start.longitude)

    val y = Math.sin(lonDiff) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(lonDiff)

    val bearing = Math.toDegrees(Math.atan2(y, x))
    return (bearing + 360) % 360
}

// Get direction from bearing
private fun getDirectionFromBearing(bearing: Double): String {
    return when {
        bearing >= 337.5 || bearing < 22.5 -> "North"
        bearing >= 22.5 && bearing < 67.5 -> "Northeast"
        bearing >= 67.5 && bearing < 112.5 -> "East"
        bearing >= 112.5 && bearing < 157.5 -> "Southeast"
        bearing >= 157.5 && bearing < 202.5 -> "South"
        bearing >= 202.5 && bearing < 247.5 -> "Southwest"
        bearing >= 247.5 && bearing < 292.5 -> "West"
        else -> "Northwest"
    }
}