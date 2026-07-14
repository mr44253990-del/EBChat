package com.ebchat.ui.screens.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.Message
import com.ebchat.data.model.MessageStatus
import com.ebchat.data.model.MessageType
import com.ebchat.data.model.User
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.OnlineGreen
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(navController: NavHostController, chatId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseConfig.getCurrentUserId()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var otherUser by remember { mutableStateOf<User?>(null) }
    var inputText by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<Message?>(null) }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    // Load messages
    LaunchedEffect(chatId) {
        FirebaseConfig.messagesRef().child(chatId).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val msgList = mutableListOf<Message>()
                    snapshot.children.forEach { child ->
                        val map = child.value as? Map<String, Any?>
                        if (map != null) {
                            msgList.add(Message.fromMap(map.mapKeys { it.key.toString() }))
                        }
                    }
                    messages = msgList.sortedBy { it.timestamp }
                    // Mark messages as read
                    msgList.filter { it.receiverId == userId && it.status != MessageStatus.READ.name }
                        .forEach { msg ->
                            FirebaseConfig.messagesRef().child(chatId).child(msg.id).child("status").setValue(MessageStatus.READ.name)
                        }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Load other user info
    LaunchedEffect(chatId) {
        FirebaseConfig.chatsRef().child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val participants = (snapshot.child("participants").value as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val otherId = participants.find { it != userId }
                if (otherId != null) {
                    FirebaseConfig.usersRef().child(otherId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val map = userSnapshot.value as? Map<String, Any?>
                            if (map != null) {
                                otherUser = User.fromMap(map.mapKeys { it.key.toString() })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Typing indicator
    LaunchedEffect(inputText) {
        if (inputText.isNotBlank()) {
            FirebaseConfig.typingRef().child(chatId).child(userId).setValue(true)
            delay(3000)
            FirebaseConfig.typingRef().child(chatId).child(userId).removeValue()
        }
    }

    // Listen for other user typing
    LaunchedEffect(chatId) {
        FirebaseConfig.typingRef().child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = snapshot.children.mapNotNull { it.key }
                isTyping = typingUsers.any { it != userId }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Upload to Supabase and send
            sendMediaMessage(chatId, userId, otherUser?.id ?: "", it.toString(), MessageType.IMAGE)
        }
    }

    // Video picker
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            sendMediaMessage(chatId, userId, otherUser?.id ?: "", it.toString(), MessageType.VIDEO)
        }
    }

    // Scroll to bottom when new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (otherUser?.photoUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = otherUser?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    (otherUser?.name ?: "U").take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                otherUser?.name ?: "Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (otherUser?.isOnline == true) {
                                Text(
                                    "Online",
                                    fontSize = 12.sp,
                                    color = OnlineGreen
                                )
                            } else if (isTyping) {
                                Text(
                                    "typing...",
                                    fontSize = 12.sp,
                                    color = PinkPrimary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Video call */ }) {
                        Icon(Icons.Default.VideoCall, "Video Call", tint = PinkPrimary)
                    }
                    IconButton(onClick = { /* TODO: Options */ }) {
                        Icon(Icons.Default.MoreVert, "More", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassSurface.copy(alpha = 0.98f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == userId,
                        onLongClick = { selectedMessage = message },
                        onReply = { replyTo = message },
                        onReact = { emoji -> addReaction(chatId, message.id, userId, emoji) }
                    )
                }
            }

            // Reply Preview
            if (replyTo != null) {
                ReplyPreview(
                    message = replyTo!!,
                    onDismiss = { replyTo = null }
                )
            }

            // Selected Message Actions
            if (selectedMessage != null) {
                MessageActionsBar(
                    message = selectedMessage!!,
                    isOwnMessage = selectedMessage!!.senderId == userId,
                    onDismiss = { selectedMessage = null },
                    onReply = { replyTo = selectedMessage; selectedMessage = null },
                    onDelete = {
                        deleteMessage(chatId, selectedMessage!!.id)
                        selectedMessage = null
                    },
                    onForward = { /* TODO */ },
                    onReact = { emoji ->
                        addReaction(chatId, selectedMessage!!.id, userId, emoji)
                        selectedMessage = null
                    }
                )
            }

            // Emoji Picker (simplified)
            if (showEmojiPicker) {
                EmojiPicker(onEmojiSelected = { emoji ->
                    inputText += emoji
                    showEmojiPicker = false
                })
            }

            // Input Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassSurface.copy(alpha = 0.98f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                        Icon(Icons.Default.EmojiEmotions, "Emoji", tint = PinkPrimary)
                    }

                    IconButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.Photo, "Photo", tint = PinkPrimary)
                    }

                    IconButton(onClick = { videoPicker.launch("video/*") }) {
                        Icon(Icons.Default.VideoCall, "Video", tint = PinkPrimary)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = PinkLight.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    sendMessage(chatId, userId, otherUser?.id ?: "", inputText, replyTo)
                                    inputText = ""
                                    replyTo = null
                                }
                            }
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (inputText.isBlank()) {
                        // Voice message button
                        IconButton(
                            onClick = { isRecording = !isRecording }
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                "Voice",
                                tint = if (isRecording) Color.Red else PinkPrimary
                            )
                        }
                    } else {
                        // Send button
                        IconButton(
                            onClick = {
                                sendMessage(chatId, userId, otherUser?.id ?: "", inputText, replyTo)
                                inputText = ""
                                replyTo = null
                            }
                        ) {
                            Icon(Icons.Default.Send, "Send", tint = PinkPrimary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    onLongClick: () -> Unit,
    onReply: () -> Unit,
    onReact: (String) -> Unit
) {
    if (message.isDeleted) {
        DeletedMessageBubble(isOwnMessage = isOwnMessage)
        return
    }

    val bubbleColor = if (isOwnMessage) {
        Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark))
    } else {
        Brush.linearGradient(listOf(GlassSurface, PinkLight.copy(alpha = 0.3f)))
    }

    val textColor = if (isOwnMessage) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // Reply preview
                if (message.replyTo.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text(
                                "Replying to",
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                message.replyToContent,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Media content
                if (message.mediaUrl.isNotBlank()) {
                    when (message.type) {
                        MessageType.IMAGE.name -> {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        MessageType.VIDEO.name -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Video", color = Color.White)
                            }
                        }
                        MessageType.AUDIO.name -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(200.dp)
                            ) {
                                Icon(Icons.Default.Mic, "Audio", tint = textColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${message.mediaDuration}s", color = textColor)
                            }
                        }
                    }
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Text content
                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }

                // Reactions
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.reactions.values.distinct().forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(emoji, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Time & Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val statusIcon = when (message.status) {
                            MessageStatus.SENDING.name -> "..."
                            MessageStatus.SENT.name -> "\u2713"
                            MessageStatus.DELIVERED.name -> "\u2713\u2713"
                            MessageStatus.READ.name -> "\u2713\u2713"
                            else -> ""
                        }
                        val statusColor = if (message.status == MessageStatus.READ.name) Color.Cyan else textColor.copy(alpha = 0.7f)
                        Text(statusIcon, fontSize = 10.sp, color = statusColor)
                    }
                }
            }
        }
    }
}

@Composable
fun DeletedMessageBubble(isOwnMessage: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            "This message was deleted",
            fontSize = 13.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ReplyPreview(message: Message, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(containerColor = PinkLight.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(PinkPrimary, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Replying to", fontSize = 12.sp, color = PinkPrimary, fontWeight = FontWeight.Medium)
                Text(
                    message.content,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Cancel", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageActionsBar(
    message: Message,
    isOwnMessage: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onReact: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Quick reactions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("\u2764", "\uD83D\uDC4D", "\uD83D\uDE02", "\uD83D\uDE22", "\uD83D\uDE32", "\uD83D\uDD25").forEach { emoji ->
                    Text(
                        emoji,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onReact(emoji) }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(Icons.Default.Reply, "Reply", onReply)
                ActionButton(Icons.Default.Forward, "Forward", onForward)
                if (isOwnMessage) {
                    ActionButton(Icons.Default.Delete, "Delete", onDelete)
                }
                ActionButton(Icons.Default.Close, "Close", onDismiss)
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, label, tint = PinkPrimary, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 11.sp, color = PinkPrimary)
    }
}

@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf(
        "\uD83D\uDE00", "\uD83D\uDE02", "\uD83D\uDE0D", "\uD83D\uDE0E", "\uD83D\uDE14",
        "\uD83D\uDE21", "\uD83D\uDE2D", "\uD83D\uDE08", "\u2764", "\uD83D\uDC4D",
        "\uD83D\uDC4E", "\uD83D\uDD25", "\uD83C\uDF89", "\uD83D\uDE4C", "\uD83D\uDC4F",
        "\uD83E\udd14", "\uD83D\ude33", "\uD83D\ude31", "\uD83E\udd70", "\uD83E\udd2d"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Reactions", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize()
            ) {
                items(emojis.size) { index ->
                    Text(
                        emojis[index],
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onEmojiSelected(emojis[index]) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

fun sendMessage(chatId: String, senderId: String, receiverId: String, content: String, replyTo: Message?) {
    val messageId = UUID.randomUUID().toString()
    val message = Message(
        id = messageId,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        type = MessageType.TEXT.name,
        replyTo = replyTo?.id ?: "",
        replyToContent = replyTo?.content ?: "",
        replyToType = replyTo?.type ?: "",
        status = MessageStatus.SENT.name,
        timestamp = System.currentTimeMillis()
    )

    FirebaseConfig.messagesRef().child(chatId).child(messageId).setValue(message.toMap())
    FirebaseConfig.chatsRef().child(chatId).child("lastMessage").setValue(message.toMap())
    FirebaseConfig.chatsRef().child(chatId).child("updatedAt").setValue(ServerValue.TIMESTAMP)

    // Increment unread count for receiver
    val unreadRef = FirebaseConfig.chatsRef().child(chatId).child("unreadCount").child(receiverId)
    unreadRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val current = snapshot.getValue(Int::class.java) ?: 0
            unreadRef.setValue(current + 1)
        }
        override fun onCancelled(error: DatabaseError) {}
    })
}

fun sendMediaMessage(chatId: String, senderId: String, receiverId: String, mediaUrl: String, type: MessageType) {
    val messageId = UUID.randomUUID().toString()
    val message = Message(
        id = messageId,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        content = "",
        type = type.name,
        mediaUrl = mediaUrl,
        status = MessageStatus.SENT.name,
        timestamp = System.currentTimeMillis()
    )
    FirebaseConfig.messagesRef().child(chatId).child(messageId).setValue(message.toMap())
}

fun addReaction(chatId: String, messageId: String, userId: String, emoji: String) {
    FirebaseConfig.messagesRef().child(chatId).child(messageId).child("reactions").child(userId).setValue(emoji)
}

fun deleteMessage(chatId: String, messageId: String) {
    FirebaseConfig.messagesRef().child(chatId).child(messageId).updateChildren(mapOf(
        "isDeleted" to true,
        "deletedAt" to System.currentTimeMillis(),
        "content" to "",
        "mediaUrl" to ""
    ))
}

fun formatMessageTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
