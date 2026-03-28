package edu.ucne.corebuild.di

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.dao.OrderDao
import edu.ucne.corebuild.data.local.database.CoreBuildDatabase
import edu.ucne.corebuild.data.local.datasource.ComponentLocalDataSource
import edu.ucne.corebuild.data.repository.CartRepositoryImpl
import edu.ucne.corebuild.data.repository.ComponentRepositoryImpl
import edu.ucne.corebuild.data.repository.OrderRepositoryImpl
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.domain.repository.OrderRepository
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoreBuildDatabase {
        return Room.databaseBuilder(
            context,
            CoreBuildDatabase::class.java,
            "corebuild.db"
        ).build()
    }

    @Provides
    fun provideComponentDao(db: CoreBuildDatabase): ComponentDao {
        return db.componentDao()
    }

    @Provides
    fun provideOrderDao(db: CoreBuildDatabase): OrderDao {
        return db.orderDao()
    }

    @Provides
    @Singleton
    fun provideComponentRepository(
        dao: ComponentDao,
        localDataSource: ComponentLocalDataSource
    ): ComponentRepository {
        return ComponentRepositoryImpl(dao, localDataSource)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao
    ): OrderRepository {
        return OrderRepositoryImpl(orderDao)
    }

    @Provides
    @Singleton
    fun provideCompatibilityEngine(): CompatibilityEngine {
        return CompatibilityEngine()
    }

    @Provides
    @Singleton
    fun provideCartRepository(compatibilityEngine: CompatibilityEngine): CartRepository {
        return CartRepositoryImpl(compatibilityEngine)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://corebuildapi-production.up.railway.app/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
