package edu.ucne.corebuild.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.ucne.corebuild.data.local.datasource.ComponentLocalDataSource
import edu.ucne.corebuild.data.repository.ComponentRepositoryImpl
import edu.ucne.corebuild.domain.repository.ComponentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideComponentRepository(
        localDataSource: ComponentLocalDataSource
    ): ComponentRepository {
        return ComponentRepositoryImpl(localDataSource)
    }
}
