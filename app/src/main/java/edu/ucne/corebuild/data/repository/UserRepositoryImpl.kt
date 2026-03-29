package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.UserDao
import edu.ucne.corebuild.data.local.mapper.toUser
import edu.ucne.corebuild.domain.model.User
import edu.ucne.corebuild.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getLoggedUser(): Flow<User?> {
        return userDao.getLoggedUser().map { entity ->
            entity?.toUser()
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            userDao.logoutAll()
            val entity = userDao.login(email, password)
            if (entity != null) {
                val updated = entity.copy(isLogged = true)
                userDao.updateUser(updated)
                Result.success(updated.toUser())
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val entity = edu.ucne.corebuild.data.local.entity.UserEntity(
                name = user.name,
                email = user.email,
                password = password,
                isLogged = true
            )
            userDao.register(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePicture(userId: Int, imageUrl: String): Result<Unit> {
        return try {
            val currentEntity = userDao.getLoggedUser().firstOrNull()
            if (currentEntity != null) {
                userDao.updateUser(currentEntity.copy(profilePicture = imageUrl))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userDao.logoutAll()
    }
}
