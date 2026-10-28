package com.signlink.app

import android.app.Application
import com.signlink.app.data.repository.ChatRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SignLinkApp : Application() {

    // Hilt injects this after super.onCreate() completes
    @Inject
    lateinit var chatRepository: ChatRepository

    override fun onCreate() {
        super.onCreate()

        // Use runCatching to ensure app startup is NEVER blocked by a database issue.
        runCatching {
            chatRepository.applyStartupRetention()
        }
    }
}