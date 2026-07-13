package com.ebchat.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.ebchat.R
import com.ebchat.data.model.Message
import com.ebchat.data.model.MessageStatus
import com.ebchat.data.model.MessageType
import com.ebchat.data.remote.FirebaseConfig
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.ebchat.REPLY_ACTION" -> handleReply(context, intent)
            "com.ebchat.MARK_READ_ACTION" -> handleMarkRead(context, intent)
            "com.ebchat.DISMISS_ACTION" -> handleDismiss(context, intent)
        }
    }

    private fun handleReply(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra("chatId") ?: return
        val senderId = intent.getStringExtra("senderId") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val reply = remoteInput?.getCharSequence(EBChatMessagingService.KEY_REPLY)?.toString() ?: return

        val userId = FirebaseConfig.getCurrentUserId()
        if (userId.isBlank()) return

        // Send the reply message
        val messageId = UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            chatId = chatId,
            senderId = userId,
            receiverId = senderId,
            content = reply,
            type = MessageType.TEXT.name,
            status = MessageStatus.SENT.name,
            timestamp = System.currentTimeMillis()
        )

        FirebaseConfig.messagesRef().child(chatId).child(messageId).setValue(message.toMap())
        FirebaseConfig.chatsRef().child(chatId).child("lastMessage").setValue(message.toMap())
        FirebaseConfig.chatsRef().child(chatId).child("updatedAt").setValue(ServerValue.TIMESTAMP)

        // Clear the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // Show a "sent" confirmation
        val sentNotification = NotificationCompat.Builder(context, EBChatMessagingService.CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reply Sent")
            .setContentText("Your reply has been sent")
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .build()
        notificationManager.notify(notificationId + 1000, sentNotification)
    }

    private fun handleMarkRead(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra("chatId") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)

        // Mark all messages in this chat as read
        FirebaseConfig.messagesRef().child(chatId).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val userId = FirebaseConfig.getCurrentUserId()
                snapshot.children.forEach { child ->
                    val senderId = child.child("senderId").getValue(String::class.java)
                    if (senderId != userId) {
                        child.ref.child("status").setValue(MessageStatus.READ.name)
                    }
                }
                // Reset unread count
                FirebaseConfig.chatsRef().child(chatId).child("unreadCount").child(userId).setValue(0)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Clear notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private fun handleDismiss(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
