// ============================================================
// File: di/DatabaseModule.kt  [FIXED]
// Purpose: Hilt module — SOLE constructor of Room DB and DAO.
//
// FIX: Now uses Room.databaseBuilder() directly instead of
// delegating to SignLinkDatabase.getInstance(). This guarantees
// Hilt manages exactly one DB instance for the app's lifetime.
//
// Also provides AppSettingsDataStore here so all data-layer
// singletons come from one module.
// ============================================================

package com.signlink.app.di

import android.content.Context
import androidx.room.Room
import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.data.local.ChatDao
import com.signlink.app.data.local.SignLinkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSignLinkDatabase(
        @ApplicationContext context: Context
    ): SignLinkDatabase = Room.databaseBuilder(
        context.applicationContext,
        SignLinkDatabase::class.java,
        "signlink_database.db"
    )
        // In development: wipe and rebuild if schema changes.
        // Before release: replace with proper Migration objects.
        .fallbackToDestructiveMigration(true)
        .build()

    @Provides
    @Singleton
    fun provideChatDao(db: SignLinkDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideAppSettingsDataStore(
        @ApplicationContext context: Context
    ): AppSettingsDataStore = AppSettingsDataStore(context)
}