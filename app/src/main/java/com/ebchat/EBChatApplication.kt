package com.ebchat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import com.ebchat.data.remote.FirebaseConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.messaging.FirebaseMessaging

class EBChatApplication : Application() {

    private val tag = "EBChatApplication"

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase safely — wrap in try/catch so any failure
        // does not crash the app on launch.
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e(tag, "FirebaseApp.initializeApp failed", e)
        }

        // Configure Realtime Database persistence exactly once, before
        // any UI code touches FirebaseConfig.database.
        FirebaseConfig.configurePersistence()

        // Configure Firestore persistent cache on the MAIN thread, BEFORE
        // any coroutine or UI code accesses Firestore. This prevents crashes
        // when Firestore is lazily initialized from background threads.
        FirebaseConfig.configureFirestore()

        try {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set log level", e)
        }

        // Subscribe to global topic — best-effort, must not crash app
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "Subscribed to all_users topic")
                    } else {
                        Log.w(tag, "Failed to subscribe to all_users", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "subscribeToTopic failed", e)
        }

        // Create notification channels
        try {
            createNotificationChannels()
        } catch (e: Exception) {
            Log.e(tag, "createNotificationChannels failed", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DEFAULT,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Default notification channel for EBChat"
                    setSound(
                        Uri.parse("android.resource://${packageName}/raw/notification_sound"),
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                },
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New message notifications"
                    setSound(
                        Uri.parse("android.resource://${packageName}/raw/message_sound"),
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                },
                NotificationChannel(
                    CHANNEL_CALLS,
                    "Calls",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Incoming call notifications"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                },
                NotificationChannel(
                    CHANNEL_STORIES,
                    "Stories",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New story notifications"
                },
                NotificationChannel(
                    CHANNEL_POSTS,
                    "Posts & Reactions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Post reactions and comments"
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    companion object {
        const val CHANNEL_DEFAULT = "ebchat_default_channel"
        const val CHANNEL_MESSAGES = "ebchat_messages_channel"
        const val CHANNEL_CALLS = "ebchat_calls_channel"
        const val CHANNEL_STORIES = "ebchat_stories_channel"
        const val CHANNEL_POSTS = "ebchat_posts_channel"
    }
}
