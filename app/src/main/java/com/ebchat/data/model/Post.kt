package com.ebchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val mediaTypes: List<String> = emptyList(), // "image", "video"
    val thumbnailUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true,
    val isDeleted: Boolean = false,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val bookmarkedBy: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val location: String = "",
    val isPinned: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "userName" to userName,
        "userPhotoUrl" to userPhotoUrl,
        "title" to title,
        "content" to content,
        "tags" to tags,
        "mediaUrls" to mediaUrls,
        "mediaTypes" to mediaTypes,
        "thumbnailUrls" to thumbnailUrls,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "isPublic" to isPublic,
        "isDeleted" to isDeleted,
        "viewCount" to viewCount,
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "shareCount" to shareCount,
        "likedBy" to likedBy,
        "bookmarkedBy" to bookmarkedBy,
        "mentions" to mentions,
        "location" to location,
        "isPinned" to isPinned
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Post = Post(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            userName = map["userName"] as? String ?: "",
            userPhotoUrl = map["userPhotoUrl"] as? String ?: "",
            title = map["title"] as? String ?: "",
            content = map["content"] as? String ?: "",
            tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            mediaUrls = (map["mediaUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            mediaTypes = (map["mediaTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            thumbnailUrls = (map["thumbnailUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isPublic = map["isPublic"] as? Boolean ?: true,
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            viewCount = (map["viewCount"] as? Number)?.toInt() ?: 0,
            likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0,
            commentCount = (map["commentCount"] as? Number)?.toInt() ?: 0,
            shareCount = (map["shareCount"] as? Number)?.toInt() ?: 0,
            likedBy = (map["likedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            bookmarkedBy = (map["bookmarkedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            mentions = (map["mentions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            location = map["location"] as? String ?: "",
            isPinned = map["isPinned"] as? Boolean ?: false
        )
    }
}

@Serializable
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val replies: List<Comment> = emptyList()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "postId" to postId,
        "userId" to userId,
        "userName" to userName,
        "userPhotoUrl" to userPhotoUrl,
        "content" to content,
        "createdAt" to createdAt,
        "likeCount" to likeCount,
        "likedBy" to likedBy
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Comment = Comment(
            id = map["id"] as? String ?: "",
            postId = map["postId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            userName = map["userName"] as? String ?: "",
            userPhotoUrl = map["userPhotoUrl"] as? String ?: "",
            content = map["content"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0,
            likedBy = (map["likedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }
}
