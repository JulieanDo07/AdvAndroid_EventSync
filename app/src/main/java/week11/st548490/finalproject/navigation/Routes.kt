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
    object EventDetails : Screen("event_details")

    // Expenses Section - Juliean
    object Expenses : Screen("expenses")
    object AddExpense : Screen("add_expense")

    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    // Add argument for selected location

    // Location Screen - Add this
    object SetLocation : Screen("set_location")

}

// Route helpers
fun String?.createEventDetailsRoute(): String {
    return "event_details/$this"
}

fun String?.createEditEventRoute(): String {
    return "edit_event/$this"
}