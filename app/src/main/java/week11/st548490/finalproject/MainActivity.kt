package week11.st548490.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import week11.st548490.finalproject.navigation.Screen
import week11.st548490.finalproject.ui.auth.AuthStateHandler
import week11.st548490.finalproject.ui.auth.ForgotPasswordScreen
import week11.st548490.finalproject.ui.auth.LoginScreen
import week11.st548490.finalproject.ui.auth.SignUpScreen
import week11.st548490.finalproject.ui.events.CreateEventScreen
import week11.st548490.finalproject.ui.events.EditEventScreen
import week11.st548490.finalproject.ui.events.EventDetailsScreen
import week11.st548490.finalproject.ui.expenses.AddExpenseScreen
import week11.st548490.finalproject.ui.expenses.ExpenseListScreen
import week11.st548490.finalproject.ui.location.SetLocationScreen
import week11.st548490.finalproject.ui.main.MainScreen
import week11.st548490.finalproject.ui.notifications.NotificationsScreen
import week11.st548490.finalproject.ui.profile.ProfileScreen
import week11.st548490.finalproject.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                typography = Typography
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventSyncApp()
                }
            }
        }
    }
}




@Composable
fun EventSyncApp() {
    val navController = rememberNavController()
    val authStateHandler: AuthStateHandler = viewModel()
    val currentUser by authStateHandler.currentUser.collectAsState()

    // Handle authentication state
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            // User not signed in, navigate to login
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        } else {
            // User signed in, navigate to main if not already there
            if (navController.currentDestination?.route != Screen.Main.route) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Screen.Main.route else Screen.Login.route
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // Main App Screens
        composable(Screen.Main.route) {
            MainScreen(navController = navController, authStateHandler = authStateHandler)
        }
        composable(Screen.CreateEvent.route) {
            CreateEventScreen(navController = navController)
        }
        composable(
            route = "event_details/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventDetailsScreen(navController = navController, eventId = eventId)
        }
        //expense screen
        composable(Screen.Expenses.route) {
            ExpenseListScreen(navController)
        }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController)
        }


        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, authStateHandler = authStateHandler)
        }
        // Add this composable to your NavHost
        composable(
            route = "edit_event/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EditEventScreen(navController = navController, eventId = eventId ?: "")
        }

        // Set Location screen
        composable(Screen.SetLocation.route) {
            SetLocationScreen(navController = navController)
        }



    }
}
