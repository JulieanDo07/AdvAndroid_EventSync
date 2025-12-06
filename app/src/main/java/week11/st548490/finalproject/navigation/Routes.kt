package week11.st548490.finalproject.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")

    // Main App Screens
    object Main : Screen("main")
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String = "") = "event_details/${eventId}"
    }

    // Expenses Section - Juliean
    object Expenses : Screen("expenses")
    object AddExpense : Screen("add_expense")

    // Notifications and Profile
    object Notifications : Screen("notifications")
    object Invitations : Screen("invitations") // New screen for event invitations
    object Profile : Screen("profile")

    // Location Screens
    object SetLocation : Screen("set_location")
    object MapScreen : Screen("map_screen/{eventLocation}") {
        fun createRoute(eventLocation: String) = "map_screen/${eventLocation}"
    }
}

// Route helpers
fun String?.createEventDetailsRoute(): String {
    return "event_details/$this"
}

fun String?.createEditEventRoute(): String {
    return "edit_event/$this"
}

fun String?.createMapScreenRoute(): String {
    return "map_screen/$this"
}