package com.ebchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val gender: String = "", // "male", "female", "other"
    val birthDate: String = "",
    val status: String = "Hey there! I'm using EBChat",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    val isBlocked: Boolean = false,
    val themePreference: String = "pink_glass", // pink_glass, dark, blue, purple, green
    val isMuted: Boolean = false,
    val mutedUntil: Long = 0,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val postCount: Int = 0,
    val storyCount: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "photoUrl" to photoUrl,
        "bio" to bio,
        "gender" to gender,
        "birthDate" to birthDate,
        "status" to status,
        "isOnline" to isOnline,
        "lastSeen" to lastSeen,
        "createdAt" to createdAt,
        "fcmToken" to fcmToken,
        "isBlocked" to isBlocked,
        "themePreference" to themePreference,
        "isMuted" to isMuted,
        "mutedUntil" to mutedUntil,
        "followers" to followers,
        "following" to following,
        "postCount" to postCount,
        "storyCount" to storyCount
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            id = map["id"] as? String ?: "",
            uid = map["uid"] as? String ?: "",
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: "",
            bio = map["bio"] as? String ?: "",
            gender = map["gender"] as? String ?: "",
            birthDate = map["birthDate"] as? String ?: "",
            status = map["status"] as? String ?: "",
            isOnline = map["isOnline"] as? Boolean ?: false,
            lastSeen = map["lastSeen"] as? Long ?: System.currentTimeMillis(),
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            fcmToken = map["fcmToken"] as? String ?: "",
            isBlocked = map["isBlocked"] as? Boolean ?: false,
            themePreference = map["themePreference"] as? String ?: "pink_glass",
            isMuted = map["isMuted"] as? Boolean ?: false,
            mutedUntil = map["mutedUntil"] as? Long ?: 0,
            followers = (map["followers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            following = (map["following"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            postCount = (map["postCount"] as? Number)?.toInt() ?: 0,
            storyCount = (map["storyCount"] as? Number)?.toInt() ?: 0
        )
    }
}

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val readReceipts: Boolean = true,
    val theme: String = "pink_glass",
    val language: String = "bn",
    val fontSize: String = "medium"
)
