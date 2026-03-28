package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getLoggedUser(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(user: User, password: String): Result<Unit>
    suspend fun logout()
}
