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
        .fallbackToDestructiveMigration(dropAllTables = true)
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