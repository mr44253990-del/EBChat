package com.ebchat.ui.screens.post

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.Comment
import com.ebchat.data.model.Post
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavHostController, postId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseConfig.getCurrentUserId()
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load post
    LaunchedEffect(postId) {
        FirebaseConfig.postsRef().child(postId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as? Map<String, Any?>
                if (map != null) {
                    post = Post.fromMap(map.mapKeys { it.key.toString() })
                }
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    // Load comments
    LaunchedEffect(postId) {
        FirebaseConfig.commentsRef(postId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()
                snapshot.children.forEach { child ->
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        commentList.add(Comment.fromMap(map.mapKeys { it.key.toString() }))
                    }
                }
                comments = commentList.sortedByDescending { it.createdAt }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Increment view count
    LaunchedEffect(postId) {
        FirebaseConfig.postsRef().child(postId).child("viewCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val current = snapshot.getValue(Int::class.java) ?: 0
                FirebaseConfig.postsRef().child(postId).child("viewCount").setValue(current + 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    if (isLoading || post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PinkPrimary)
        }
        return
    }

    val isLiked = post!!.likedBy.contains(userId)
    val isBookmarked = post!!.bookmarkedBy.contains(userId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        },
        bottomBar = {
            // Comment Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.98f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = PinkLight.copy(alpha = 0.5f)
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                val commentId = UUID.randomUUID().toString()
                                val comment = Comment(
                                    id = commentId,
                                    postId = postId,
                                    userId = userId,
                                    userName = post!!.userName,
                                    content = commentText,
                                    createdAt = System.currentTimeMillis()
                                )
                                FirebaseConfig.commentsRef(postId).child(commentId).setValue(comment.toMap())
                                FirebaseConfig.postsRef().child(postId).child("commentCount").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val current = snapshot.getValue(Int::class.java) ?: 0
                                        FirebaseConfig.postsRef().child(postId).child("commentCount").setValue(current + 1)
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                                commentText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = PinkPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Post Content
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    // User info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (post!!.userPhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = post!!.userPhotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    post!!.userName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(post!!.userName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(formatTimestamp(post!!.createdAt), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title & Content
                    if (post!!.title.isNotBlank()) {
                        Text(post!!.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (post!!.content.isNotBlank()) {
                        Text(post!!.content, fontSize = 15.sp, lineHeight = 22.sp)
                    }

                    // Tags
                    if (post!!.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            post!!.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PinkLight.copy(alpha = 0.3f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("#$tag", fontSize = 12.sp, color = PinkPrimaryDark)
                                }
                            }
                        }
                    }

                    // Media
                    if (post!!.mediaUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = post!!.mediaUrls.first(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RemoveRedEye, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${post!!.viewCount} views", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Text("${post!!.likeCount} likes \u00b7 ${post!!.commentCount} comments", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { toggleLike(post!!, userId) }) {
                            Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Like", tint = if (isLiked) PinkPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        IconButton(onClick = { /* Focus comment */ }) {
                            Icon(Icons.Default.ChatBubbleOutline, "Comment", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        IconButton(onClick = { toggleBookmark(post!!, userId) }) {
                            Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Bookmark", tint = if (isBookmarked) PinkPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comments Section Title
                    Text("Comments (${comments.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Comments List
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark))),
                contentAlignment = Alignment.Center
            ) {
                if (comment.userPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = comment.userPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        comment.userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.userName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatTimestamp(comment.createdAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(comment.content, fontSize = 14.sp, lineHeight = 18.sp)
            }
        }
    }
}
