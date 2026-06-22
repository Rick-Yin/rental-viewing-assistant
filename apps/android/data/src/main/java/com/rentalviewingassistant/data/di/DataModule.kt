package com.rentalviewingassistant.data.di

import android.content.Context
import androidx.room.Room
import com.rentalviewingassistant.data.local.RentalDao
import com.rentalviewingassistant.data.local.RentalDatabase
import com.rentalviewingassistant.data.repository.AssetChecklistRepository
import com.rentalviewingassistant.data.repository.RoomRentalRepository
import com.rentalviewingassistant.domain.repository.ChecklistRepository
import com.rentalviewingassistant.domain.repository.RentalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RentalDatabase =
        Room.databaseBuilder(
            context,
            RentalDatabase::class.java,
            "rental-viewing-assistant.db",
        ).build()

    @Provides
    fun provideRentalDao(database: RentalDatabase): RentalDao = database.rentalDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRentalRepository(repository: RoomRentalRepository): RentalRepository

    @Binds
    @Singleton
    abstract fun bindChecklistRepository(repository: AssetChecklistRepository): ChecklistRepository
}
