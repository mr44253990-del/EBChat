package com.ebchat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.messaging.FirebaseMessaging

class EBChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)

        // Subscribe to global topic
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Subscribed to all_users topic")
                }
            }

        // Create notification channels
        createNotificationChannels()
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
