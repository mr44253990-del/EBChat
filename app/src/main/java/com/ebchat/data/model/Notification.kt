package com.ebchat.data.model

import kotlinx.serialization.Serializable

enum class NotificationType {
    MESSAGE, LIKE, COMMENT, FOLLOW, MENTION, STORY_VIEW, GROUP_INVITE,
    SYSTEM, REPLY, FORWARD, POST_SHARE, FRIEND_REQUEST, TYPING
}

@Serializable
data class NotificationItem(
    val id: String = "",
    val type: String = NotificationType.MESSAGE.name,
    val title: String = "",
    val body: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val targetId: String = "", // postId, messageId, groupId, etc.
    val chatId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type,
        "title" to title,
        "body" to body,
        "senderId" to senderId,
        "senderName" to senderName,
        "senderPhotoUrl" to senderPhotoUrl,
        "targetId" to targetId,
        "chatId" to chatId,
        "isRead" to isRead,
        "createdAt" to createdAt,
        "metadata" to metadata
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): NotificationItem = NotificationItem(
            id = map["id"] as? String ?: "",
            type = map["type"] as? String ?: NotificationType.MESSAGE.name,
            title = map["title"] as? String ?: "",
            body = map["body"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            senderName = map["senderName"] as? String ?: "",
            senderPhotoUrl = map["senderPhotoUrl"] as? String ?: "",
            targetId = map["targetId"] as? String ?: "",
            chatId = map["chatId"] as? String ?: "",
            isRead = map["isRead"] as? Boolean ?: false,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            metadata = map["metadata"] as? String ?: ""
        )
    }
}

data class PushNotificationData(
    val to: String = "",
    val notification: NotificationPayload = NotificationPayload(),
    val data: Map<String, String> = emptyMap()
)

data class NotificationPayload(
    val title: String = "",
    val body: String = "",
    val sound: String = "notification",
    val android_channel_id: String = "ebchat_default_channel"
)
