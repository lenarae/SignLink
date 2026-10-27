// ============================================================
// File: SignLinkApp.kt  [UPDATED]
// Purpose: Application class — runs before anything else.
//
// NEW: Injects ChatRepository and calls applyStartupRetention()
// so stale messages are pruned on every app launch according
// to the user's chosen retention policy.
//
// This is the correct place for this — not an Activity, because
// Activities can be destroyed and recreated, but Application
// lives for the full process lifetime.
// ============================================================

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

        // Prune messages older than the user's retention setting.
        // We use runCatching to ensure app startup is NEVER blocked by a database issue.
        runCatching {
            chatRepository.applyStartupRetention()
        }
    }
}