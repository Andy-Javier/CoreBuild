package edu.ucne.corebuild.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.corebuild.data.local.dao.*
import edu.ucne.corebuild.data.local.entity.*

@Database(
    entities = [
        ComponentEntity::class,
        OrderEntity::class,
        UserEntity::class,
        FavoriteEntity::class,
        CartEntity::class,
        StatsEntity::class,
        AdminLogEntity::class
    ],
    version = 226,
    exportSchema = false
)
abstract class CoreBuildDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun statsDao(): StatsDao
    abstract fun adminLogDao(): AdminLogDao
}
