package edu.ucne.corebuild.presentation.auth

sealed interface AuthEvent {
    data class OnLogin(val email: String, val pass: String) : AuthEvent
    data class OnRegister(val name: String, val email: String, val pass: String) : AuthEvent
    data class OnUpdateProfilePicture(val imageUrl: String) : AuthEvent
    data object OnLogout : AuthEvent
    data object DismissError : AuthEvent
}
