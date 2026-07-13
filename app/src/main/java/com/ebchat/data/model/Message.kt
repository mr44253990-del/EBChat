package com.ebchat.data.model

import kotlinx.serialization.Serializable

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, LOCATION, CONTACT, STICKER
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

@Serializable
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String = MessageType.TEXT.name,
    val mediaUrl: String = "",
    val mediaName: String = "",
    val mediaSize: Long = 0,
    val mediaDuration: Long = 0, // for audio/video in seconds
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = MessageStatus.SENDING.name,
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0,
    val replyTo: String = "", // messageId being replied to
    val replyToContent: String = "",
    val replyToType: String = "",
    val forwardFrom: String = "", // original message id
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val edited: Boolean = false,
    val editedAt: Long = 0,
    val mentions: List<String> = emptyList(), // userIds mentioned
    val isPinned: Boolean = false,
    val isStarred: Boolean = false,
    val metadata: String = "" // extra JSON data
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "chatId" to chatId,
        "senderId" to senderId,
        "receiverId" to receiverId,
        "content" to content,
        "type" to type,
        "mediaUrl" to mediaUrl,
        "mediaName" to mediaName,
        "mediaSize" to mediaSize,
        "mediaDuration" to mediaDuration,
        "timestamp" to timestamp,
        "status" to status,
        "isDeleted" to isDeleted,
        "deletedAt" to deletedAt,
        "replyTo" to replyTo,
        "replyToContent" to replyToContent,
        "replyToType" to replyToType,
        "forwardFrom" to forwardFrom,
        "reactions" to reactions,
        "edited" to edited,
        "editedAt" to editedAt,
        "mentions" to mentions,
        "isPinned" to isPinned,
        "isStarred" to isStarred,
        "metadata" to metadata
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Message = Message(
            id = map["id"] as? String ?: "",
            chatId = map["chatId"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            receiverId = map["receiverId"] as? String ?: "",
            content = map["content"] as? String ?: "",
            type = map["type"] as? String ?: MessageType.TEXT.name,
            mediaUrl = map["mediaUrl"] as? String ?: "",
            mediaName = map["mediaName"] as? String ?: "",
            mediaSize = (map["mediaSize"] as? Number)?.toLong() ?: 0,
            mediaDuration = (map["mediaDuration"] as? Number)?.toLong() ?: 0,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            status = map["status"] as? String ?: MessageStatus.SENDING.name,
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            deletedAt = (map["deletedAt"] as? Number)?.toLong() ?: 0,
            replyTo = map["replyTo"] as? String ?: "",
            replyToContent = map["replyToContent"] as? String ?: "",
            replyToType = map["replyToType"] as? String ?: "",
            forwardFrom = map["forwardFrom"] as? String ?: "",
            reactions = (map["reactions"] as? Map<*, *>)?.mapKeys { it.key.toString() }
                ?.mapValues { it.value.toString() } ?: emptyMap(),
            edited = map["edited"] as? Boolean ?: false,
            editedAt = (map["editedAt"] as? Number)?.toLong() ?: 0,
            mentions = (map["mentions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isPinned = map["isPinned"] as? Boolean ?: false,
            isStarred = map["isStarred"] as? Boolean ?: false,
            metadata = map["metadata"] as? String ?: ""
        )
    }
}

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val type: String = "direct", // direct, group
    val lastMessage: Message? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> count
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val pinnedAt: Long = 0,
    val typingUsers: List<String> = emptyList()
) {
    fun getChatName(currentUserId: String): String {
        return if (type == "direct") {
            participants.find { it != currentUserId } ?: "Unknown"
        } else {
            "Group Chat"
        }
    }
}

data class TypingEvent(
    val chatId: String = "",
    val userId: String = "",
    val isTyping: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
