package edu.ucne.corebuild.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.entity.ComponentEntity

@Database(entities = [ComponentEntity::class], version = 1, exportSchema = false)
abstract class CoreBuildDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao
}
