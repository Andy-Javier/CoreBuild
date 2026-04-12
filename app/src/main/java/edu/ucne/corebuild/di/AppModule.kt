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
import edu.ucne.corebuild.core.network.NetworkManager
import edu.ucne.corebuild.data.local.dao.*
import edu.ucne.corebuild.data.local.database.CoreBuildDatabase
import edu.ucne.corebuild.data.remote.api.CoreBuildApi
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.repository.*
import edu.ucne.corebuild.domain.repository.*
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.buildscore.BuildScoreCalculator
import edu.ucne.corebuild.domain.recommendation.BuildRecommender
import edu.ucne.corebuild.domain.smartbuilder.SmartBuildGenerator
import edu.ucne.corebuild.domain.recommendation.RecommendationEngine
import edu.ucne.corebuild.domain.auth.AuthManager
import edu.ucne.corebuild.presentation.notifications.NotificationHelper
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
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideComponentDao(db: CoreBuildDatabase): ComponentDao = db.componentDao()

    @Provides
    fun provideOrderDao(db: CoreBuildDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideUserDao(db: CoreBuildDatabase): UserDao = db.userDao()

    @Provides
    fun provideFavoriteDao(db: CoreBuildDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideCartDao(db: CoreBuildDatabase): CartDao = db.cartDao()

    @Provides
    fun provideStatsDao(db: CoreBuildDatabase): StatsDao = db.statsDao()

    @Provides
    @Singleton
    fun provideComponentRepository(
        dao: ComponentDao,
        remoteDataSource: RemoteDataSource
    ): ComponentRepository = ComponentRepositoryImpl(dao, remoteDataSource)

    @Provides
    @Singleton
    fun provideOrderRepository(orderDao: OrderDao): OrderRepository = OrderRepositoryImpl(orderDao)

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao)

    @Provides
    @Singleton
    fun provideFavoriteRepository(favoriteDao: FavoriteDao): FavoriteRepository = FavoriteRepositoryImpl(favoriteDao)

    @Provides
    @Singleton
    fun provideCompatibilityEngine(): CompatibilityEngine = CompatibilityEngine()

    @Provides
    @Singleton
    fun provideRecommendationEngine(): RecommendationEngine = RecommendationEngine()

    @Provides
    @Singleton
    fun provideBuildScoreCalculator(compatibilityEngine: CompatibilityEngine): BuildScoreCalculator =
        BuildScoreCalculator(compatibilityEngine)

    @Provides
    @Singleton
    fun provideBuildRecommender(compatibilityEngine: CompatibilityEngine): BuildRecommender =
        BuildRecommender(compatibilityEngine)

    @Provides
    @Singleton
    fun provideSmartBuildGenerator(compatibilityEngine: CompatibilityEngine): SmartBuildGenerator =
        SmartBuildGenerator(compatibilityEngine)

    @Provides
    @Singleton
    fun provideCartRepository(
        cartDao: CartDao,
        compatibilityEngine: CompatibilityEngine
    ): CartRepository = CartRepositoryImpl(cartDao, compatibilityEngine)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
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

    @Provides
    @Singleton
    fun provideCoreBuildApi(retrofit: Retrofit): CoreBuildApi {
        return retrofit.create(CoreBuildApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(api: CoreBuildApi): RemoteDataSource {
        return RemoteDataSource(api)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(statsDao: StatsDao, componentDao: ComponentDao): StatsRepository =
        StatsRepositoryImpl(statsDao, componentDao)

    @Provides
    @Singleton
    fun provideAuthManager(): AuthManager = AuthManager()

    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context
    ): NetworkManager = NetworkManager(context)
}
