package com.ebchat.ui.screens.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.Chat
import com.ebchat.data.model.MessageStatus
import com.ebchat.data.model.User
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.OnlineGreen
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavHostController) {
    val userId = FirebaseConfig.getCurrentUserId()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load chats
    LaunchedEffect(userId) {
        FirebaseConfig.chatsRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<Chat>()
                snapshot.children.forEach { child ->
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        val participants = (map["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        if (participants.contains(userId)) {
                            val unreadMap = mutableMapOf<String, Int>()
                            (map["unreadCount"] as? Map<*, *>)?.forEach { (k, v) ->
                                if (k is String && v is Number) unreadMap[k] = v.toInt()
                            }
                            chatList.add(Chat(
                                id = map["id"] as? String ?: child.key ?: "",
                                participants = participants,
                                type = map["type"] as? String ?: "direct",
                                unreadCount = unreadMap,
                                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0,
                                isArchived = map["isArchived"] as? Boolean ?: false,
                                isMuted = map["isMuted"] as? Boolean ?: false,
                                pinnedAt = (map["pinnedAt"] as? Number)?.toLong() ?: 0,
                                typingUsers = (map["typingUsers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                            ))
                        }
                    }
                }
                chats = chatList.sortedByDescending { it.pinnedAt > 0 || it.unreadCount.values.sum() > 0 }
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    // Load users
    LaunchedEffect(Unit) {
        FirebaseConfig.usersRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userMap = mutableMapOf<String, User>()
                snapshot.children.forEach { child ->
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        userMap[child.key ?: ""] = User.fromMap(map.mapKeys { it.key.toString() })
                    }
                }
                users = userMap
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filteredChats = if (searchQuery.isBlank()) chats else chats.filter { chat ->
        val otherUserId = chat.participants.find { it != userId }
        val otherUser = users[otherUserId]
        otherUser?.name?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Messages", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassSurface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("search") },
                containerColor = PinkPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "New Chat", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PinkPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = PinkLight
                ),
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinkPrimary)
                }
            } else if (filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(64.dp), tint = PinkLight)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No messages yet", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredChats) { chat ->
                        val otherUserId = chat.participants.find { it != userId }
                        val otherUser = users[otherUserId]
                        val unread = chat.unreadCount[userId] ?: 0

                        ChatListItem(
                            chat = chat,
                            user = otherUser,
                            unreadCount = unread,
                            onClick = {
                                if (chat.type == "group") {
                                    navController.navigate("group_chat/${chat.id}")
                                } else {
                                    navController.navigate("chat_detail/${chat.id}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    user: User?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unreadCount > 0) PinkLight.copy(alpha = 0.2f) else GlassSurface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unreadCount > 0) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PinkPrimary, PinkAccent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.photoUrl?.isNotBlank() == true) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = (user?.name ?: "U").take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
                // Online indicator
                if (user?.isOnline == true) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Chat Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.name ?: "Unknown",
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.pinnedAt > 0) {
                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(16.dp), tint = PinkPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // Typing indicator
                if (chat.typingUsers.isNotEmpty()) {
                    Text(
                        "typing...",
                        fontSize = 14.sp,
                        color = PinkPrimary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Tap to start chatting",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Unread Badge
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
