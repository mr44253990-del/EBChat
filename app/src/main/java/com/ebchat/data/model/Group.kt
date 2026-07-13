package com.ebchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<GroupMember> = emptyList(),
    val admins: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val inviteLink: String = "",
    val settings: GroupSettings = GroupSettings()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "photoUrl" to photoUrl,
        "createdBy" to createdBy,
        "createdAt" to createdAt,
        "members" to members.map { it.toMap() },
        "admins" to admins,
        "isPublic" to isPublic,
        "inviteLink" to inviteLink
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Group = Group(
            id = map["id"] as? String ?: "",
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: "",
            createdBy = map["createdBy"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            members = (map["members"] as? List<*>)?.mapNotNull {
                if (it is Map<*, *>) GroupMember.fromMap(it.mapKeys { k -> k.key.toString() }
                    .mapValues { v -> v.value }) else null
            } ?: emptyList(),
            admins = (map["admins"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isPublic = map["isPublic"] as? Boolean ?: true,
            inviteLink = map["inviteLink"] as? String ?: ""
        )
    }
}

@Serializable
data class GroupMember(
    val userId: String = "",
    val role: String = "member", // admin, member
    val joinedAt: Long = System.currentTimeMillis(),
    val canSendMessages: Boolean = true,
    val canAddMembers: Boolean = false,
    val nickname: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "role" to role,
        "joinedAt" to joinedAt,
        "canSendMessages" to canSendMessages,
        "canAddMembers" to canAddMembers,
        "nickname" to nickname
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): GroupMember = GroupMember(
            userId = map["userId"] as? String ?: "",
            role = map["role"] as? String ?: "member",
            joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            canSendMessages = map["canSendMessages"] as? Boolean ?: true,
            canAddMembers = map["canAddMembers"] as? Boolean ?: false,
            nickname = map["nickname"] as? String ?: ""
        )
    }
}

@Serializable
data class GroupSettings(
    val onlyAdminsCanAddMembers: Boolean = false,
    val onlyAdminsCanSendMessages: Boolean = false,
    val allowMemberNickname: Boolean = true,
    val showMemberList: Boolean = true,
    val autoDeleteMessages: Boolean = false,
    val autoDeleteHours: Int = 0
)
