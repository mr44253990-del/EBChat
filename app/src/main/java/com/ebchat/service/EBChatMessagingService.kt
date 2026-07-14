package com.ebchat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.ebchat.MainActivity
import com.ebchat.R
import com.ebchat.data.remote.FirebaseConfig
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URL

class EBChatMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_DEFAULT = "ebchat_default_channel"
        const val CHANNEL_MESSAGES = "ebchat_messages_channel"
        const val CHANNEL_CALLS = "ebchat_calls_channel"
        const val CHANNEL_STORIES = "ebchat_stories_channel"
        const val CHANNEL_POSTS = "ebchat_posts_channel"
        const val KEY_REPLY = "key_reply"
        const val NOTIFICATION_GROUP = "ebchat_messages"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        val userId = FirebaseConfig.getCurrentUserId()
        if (userId.isNotBlank()) {
            FirebaseConfig.usersRef().child(userId).child("fcmToken").setValue(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "EBChat"
        val body = notification?.body ?: data["body"] ?: "New notification"
        val type = data["type"] ?: "message"
        val senderId = data["senderId"] ?: ""
        val senderName = data["senderName"] ?: "Someone"
        val chatId = data["chatId"] ?: ""

        when (type) {
            "message" -> showMessageNotification(title, body, senderId, senderName, chatId)
            "like" -> showNotification(title, body, CHANNEL_POSTS, type.hashCode())
            "comment" -> showNotification(title, body, CHANNEL_POSTS, type.hashCode())
            "story" -> showNotification(title, body, CHANNEL_STORIES, type.hashCode())
            "group_invite" -> showNotification(title, body, CHANNEL_DEFAULT, type.hashCode())
            else -> showNotification(title, body, CHANNEL_DEFAULT, remoteMessage.messageId?.hashCode() ?: 0)
        }
    }

    private fun showMessageNotification(title: String, body: String, senderId: String, senderName: String, chatId: String) {
        val notificationId = chatId.hashCode()

        // Intent to open chat
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("type", "message")
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reply action
        val replyLabel = "Reply"
        val remoteInput = RemoteInput.Builder(KEY_REPLY).setLabel(replyLabel).build()
        val replyIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "com.ebchat.REPLY_ACTION"
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
            putExtra("notificationId", notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            this, notificationId, replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, "Reply", replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Mark as read action
        val readIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "com.ebchat.MARK_READ_ACTION"
            putExtra("chatId", chatId)
            putExtra("notificationId", notificationId)
        }
        val readPendingIntent = PendingIntent.getBroadcast(
            this, notificationId + 1, readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val readAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, "Mark Read", readPendingIntent
        ).build()

        // Dismiss action
        val dismissIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "com.ebchat.DISMISS_ACTION"
            putExtra("notificationId", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, notificationId + 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, "Dismiss", dismissPendingIntent
        ).build()

        // Build notification
        val person = Person.Builder().setName(senderName).build()
        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(body, System.currentTimeMillis(), person)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setStyle(messagingStyle)
            .addAction(replyAction)
            .addAction(readAction)
            .addAction(dismissAction)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setLights(Color.parseColor("#FF69B4"), 300, 2000)
            .setGroup(NOTIFICATION_GROUP)

        // Try to load sender image
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val photoUrl = FirebaseConfig.usersRef().child(senderId).child("photoUrl").get().await()
                val url = photoUrl.getValue(String::class.java)
                if (!url.isNullOrBlank()) {
                    val bitmap = loadImageFromUrl(url)
                    if (bitmap != null) {
                        notificationBuilder.setLargeIcon(bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error loading sender image: ${e.message}")
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun showNotification(title: String, body: String, channelId: String, notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setLights(Color.parseColor("#FF69B4"), 300, 2000)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun loadImageFromUrl(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            null
        }
    }
}
