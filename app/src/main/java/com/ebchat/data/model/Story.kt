package com.ebchat.data.model

import kotlinx.serialization.Serializable

enum class StoryType {
    IMAGE, VIDEO, TEXT
}

@Serializable
data class Story(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val type: String = StoryType.IMAGE.name,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (12 * 60 * 60 * 1000), // 12 hours
    val isExpired: Boolean = false,
    val viewers: List<String> = emptyList(),
    val reactions: Map<String, String> = emptyMap(),
    val isDeleted: Boolean = false,
    val backgroundColor: String = "#FF69B4",
    val textColor: String = "#FFFFFF",
    val fontStyle: String = "default"
) {
    val isActive: Boolean
        get() = !isExpired && !isDeleted && System.currentTimeMillis() < expiresAt

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "userName" to userName,
        "userPhotoUrl" to userPhotoUrl,
        "mediaUrl" to mediaUrl,
        "thumbnailUrl" to thumbnailUrl,
        "caption" to caption,
        "type" to type,
        "createdAt" to createdAt,
        "expiresAt" to expiresAt,
        "isExpired" to isExpired,
        "viewers" to viewers,
        "reactions" to reactions,
        "isDeleted" to isDeleted,
        "backgroundColor" to backgroundColor,
        "textColor" to textColor,
        "fontStyle" to fontStyle
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Story = Story(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            userName = map["userName"] as? String ?: "",
            userPhotoUrl = map["userPhotoUrl"] as? String ?: "",
            mediaUrl = map["mediaUrl"] as? String ?: "",
            thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
            caption = map["caption"] as? String ?: "",
            type = map["type"] as? String ?: StoryType.IMAGE.name,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            expiresAt = (map["expiresAt"] as? Number)?.toLong() ?: (System.currentTimeMillis() + (12 * 60 * 60 * 1000)),
            isExpired = map["isExpired"] as? Boolean ?: false,
            viewers = (map["viewers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            reactions = (map["reactions"] as? Map<*, *>)?.mapKeys { it.key.toString() }
                ?.mapValues { it.value.toString() } ?: emptyMap(),
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            backgroundColor = map["backgroundColor"] as? String ?: "#FF69B4",
            textColor = map["textColor"] as? String ?: "#FFFFFF",
            fontStyle = map["fontStyle"] as? String ?: "default"
        )
    }
}
