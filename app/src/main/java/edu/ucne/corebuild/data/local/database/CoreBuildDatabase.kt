package edu.ucne.corebuild.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.dao.OrderDao
import edu.ucne.corebuild.data.local.dao.UserDao
import edu.ucne.corebuild.data.local.entity.ComponentEntity
import edu.ucne.corebuild.data.local.entity.OrderEntity
import edu.ucne.corebuild.data.local.entity.UserEntity

@Database(entities = [ComponentEntity::class, OrderEntity::class, UserEntity::class], version = 1, exportSchema = false)
abstract class CoreBuildDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
}
