package com.ebchat.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://srfztgcdejfaesrvkarg.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_BcH2xwywnUCVG48LYjPOLQ_8-y2InGA"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(GoTrue)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }

    // Auth
    val auth get() = client.gotrue

    // Database
    val database get() = client.postgrest

    // Storage
    val storage get() = client.storage

    // Realtime
    val realtime get() = client.realtime

    // Storage Buckets
    object Buckets {
        const val PROFILE_IMAGES = "profile-images"
        const val MESSAGE_MEDIA = "message-media"
        const val STORY_MEDIA = "story-media"
        const val POST_MEDIA = "post-media"
        const val GROUP_IMAGES = "group-images"
        const val AUDIO_FILES = "audio-files"
    }

    // Table Names
    object Tables {
        const val USERS = "users"
        const val MESSAGES = "messages"
        const val CHATS = "chats"
        const val STORIES = "stories"
        const val GROUPS = "groups"
        const val POSTS = "posts"
        const val COMMENTS = "comments"
        const val NOTIFICATIONS = "notifications"
        const val BLOCKED_USERS = "blocked_users"
    }
}
