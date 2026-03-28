package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.UserDao
import edu.ucne.corebuild.data.local.entity.UserEntity
import edu.ucne.corebuild.domain.model.User
import edu.ucne.corebuild.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getLoggedUser(): Flow<User?> {
        return userDao.getLoggedUser().map { entity ->
            entity?.let {
                User(it.id, it.name, it.email, it.isLogged)
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            userDao.logoutAll() // Limpiar sesiones previas
            val entity = userDao.login(email, password)
            if (entity != null) {
                val updated = entity.copy(isLogged = true)
                userDao.updateUser(updated)
                Result.success(User(updated.id, updated.name, updated.email, true))
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val entity = UserEntity(
                name = user.name,
                email = user.email,
                password = password
            )
            userDao.register(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userDao.logoutAll()
    }
}
