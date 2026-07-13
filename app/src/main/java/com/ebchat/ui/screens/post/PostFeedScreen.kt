package com.ebchat.ui.screens.post

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ebchat.data.model.Post
import com.ebchat.data.model.Story
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkAccent
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userId = FirebaseConfig.getCurrentUserId()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Load posts
    LaunchedEffect(Unit) {
        FirebaseConfig.postsRef().orderByChild("createdAt").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Post>()
                snapshot.children.reversed().forEach { child ->
                    val value = child.value
                    if (value is Map<*, *>) {
                        val map = value.mapKeys { it.key.toString() }
                        postList.add(Post.fromMap(map))
                    }
                }
                posts = postList
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    // Load active stories
    LaunchedEffect(Unit) {
        FirebaseConfig.storiesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storyMap = mutableMapOf<String, Story>()
                snapshot.children.forEach { userStories ->
                    userStories.children.forEach { storySnap ->
                        val value = storySnap.value
                        if (value is Map<*, *>) {
                            val map = value.mapKeys { it.key.toString() }
                            val story = Story.fromMap(map)
                            if (story.isActive && !story.isDeleted) {
                                storyMap[story.userId] = story
                            }
                        }
                    }
                }
                stories = storyMap.values.toList().sortedByDescending { it.createdAt }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filteredPosts = if (searchQuery.isBlank()) posts else posts.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.content.contains(searchQuery, ignoreCase = true) ||
        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EBChat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = PinkPrimaryDark
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.SEARCH) }) {
                        Icon(Icons.Default.Search, "Search", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassSurface.copy(alpha = 0.95f)
                )
            )
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
                placeholder = { Text("Search posts, tags...") },
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

            // Stories Row
            if (stories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Add Story Button
                    item {
                        AddStoryButton(
                            onClick = { navController.navigate("create_story") }
                        )
                    }
                    // Story items
                    items(stories) { story ->
                        StoryBubble(
                            story = story,
                            onClick = {
                                navController.navigate("story_viewer/${story.userId}")
                            }
                        )
                    }
                }
            }

            // Posts Feed
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PinkPrimary)
                }
            } else if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PinkLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No posts yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "Be the first to share something!",
                            fontSize = 14.sp,
                            color = PinkPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPosts) { post ->
                        PostCard(
                            post = post,
                            currentUserId = userId,
                            onPostClick = {
                                navController.navigate("post_detail/${post.id}")
                            },
                            onUserClick = {
                                if (post.userId != userId) {
                                    navController.navigate("user_profile/${post.userId}")
                                }
                            },
                            onLike = { toggleLike(post, userId) },
                            onBookmark = { toggleBookmark(post, userId) },
                            onShare = { /* TODO: Implement share */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddStoryButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Story",
                    tint = PinkPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Your Story", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
fun StoryBubble(story: Story, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PinkPrimary, PinkAccent)),
                    CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (story.userPhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = story.userPhotoUrl,
                    contentDescription = story.userName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = story.userName.take(1).uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PinkPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.userName.split(" ").firstOrNull() ?: story.userName,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    currentUserId: String,
    onPostClick: () -> Unit,
    onUserClick: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit
) {
    val isLiked = post.likedBy.contains(currentUserId)
    val isBookmarked = post.bookmarkedBy.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onPostClick)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PinkPrimary, PinkAccent)),
                            CircleShape
                        )
                        .clickable(onClick = onUserClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.userPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = post.userPhotoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = post.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(post.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = { /* Show options menu */ }) {
                    Icon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.tags.take(3).forEach { tag ->
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Media
            if (post.mediaUrls.isNotEmpty()) {
                val firstMedia = post.mediaUrls.first()
                val firstType = post.mediaTypes.firstOrNull() ?: "image"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (firstType == "video") {
                        VideoThumbnail(url = firstMedia)
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(firstMedia)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Multiple media indicator
                    if (post.mediaUrls.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "+${post.mediaUrls.size - 1}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveRedEye, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.viewCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(16.dp), tint = if (isLiked) PinkPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likeCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Comment, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Like",
                        tint = if (isLiked) PinkPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = onPostClick) {
                    Icon(Icons.Default.ChatBubbleOutline, "Comment", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        "Bookmark",
                        tint = if (isBookmarked) PinkPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(url: String) {
    // Simple thumbnail placeholder for video
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Visibility,
            contentDescription = "Video",
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

fun toggleLike(post: Post, userId: String) {
    val likedBy = post.likedBy.toMutableList()
    val newLikeCount = if (likedBy.contains(userId)) {
        likedBy.remove(userId)
        post.likeCount - 1
    } else {
        likedBy.add(userId)
        post.likeCount + 1
    }
    FirebaseConfig.postsRef().child(post.id).updateChildren(mapOf(
        "likedBy" to likedBy,
        "likeCount" to newLikeCount
    ))
}

fun toggleBookmark(post: Post, userId: String) {
    val bookmarkedBy = post.bookmarkedBy.toMutableList()
    if (bookmarkedBy.contains(userId)) {
        bookmarkedBy.remove(userId)
    } else {
        bookmarkedBy.add(userId)
    }
    FirebaseConfig.postsRef().child(post.id).child("bookmarkedBy").setValue(bookmarkedBy)
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
