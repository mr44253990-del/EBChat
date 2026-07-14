package com.ebchat.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata

object FirebaseConfig {

    // Firebase Instances
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {})
            }
        }
    }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    // Database References
    fun usersRef() = database.getReference("users")
    fun messagesRef() = database.getReference("messages")
    fun chatsRef() = database.getReference("chats")
    fun storiesRef() = database.getReference("stories")
    fun groupsRef() = database.getReference("groups")
    fun notificationsRef() = database.getReference("notifications")
    fun typingRef() = database.getReference("typing")
    fun presenceRef() = database.getReference("presence")
    fun postsRef() = database.getReference("posts")
    fun commentsRef(postId: String) = database.getReference("comments/$postId")

    // Firestore Collections
    fun usersCollection() = firestore.collection("users")
    fun messagesCollection() = firestore.collection("messages")
    fun chatsCollection() = firestore.collection("chats")
    fun storiesCollection() = firestore.collection("stories")
    fun groupsCollection() = firestore.collection("groups")
    fun postsCollection() = firestore.collection("posts")
    fun notificationsCollection(userId: String) = firestore.collection("users/$userId/notifications")

    // Storage References
    fun profileImagesRef() = storage.reference.child("profile_images")
    fun messageImagesRef() = storage.reference.child("message_images")
    fun messageVideosRef() = storage.reference.child("message_videos")
    fun messageAudioRef() = storage.reference.child("message_audio")
    fun storyMediaRef() = storage.reference.child("story_media")
    fun postMediaRef() = storage.reference.child("post_media")

    fun getStorageMetadata(contentType: String) = storageMetadata {
        setContentType(contentType)
    }

    // Current User
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }
}
