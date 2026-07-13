package com.ebchat.ui.screens.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.Story
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.theme.PinkAccent
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

@Composable
fun StoryViewerScreen(navController: NavHostController, initialUserId: String? = null) {
    val userId = FirebaseConfig.getCurrentUserId()
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Load stories
    LaunchedEffect(Unit) {
        FirebaseConfig.storiesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storyList = mutableListOf<Story>()
                snapshot.children.forEach { userStories ->
                    userStories.children.forEach { storySnap ->
                        val map = storySnap.value as? Map<String, Any?>
                        if (map != null) {
                            val story = Story.fromMap(map.mapKeys { it.key.toString() })
                            if (story.isActive && !story.isDeleted && story.userId != userId) {
                                storyList.add(story)
                            }
                        }
                    }
                }
                stories = storyList.sortedByDescending { it.createdAt }
                // Find initial index
                if (initialUserId != null) {
                    val idx = storyList.indexOfFirst { it.userId == initialUserId }
                    if (idx >= 0) currentIndex = idx
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Progress timer
    LaunchedEffect(currentIndex, isPaused) {
        if (stories.isEmpty()) return@LaunchedEffect
        progress = 0f
        while (progress < 1f && !isPaused) {
            delay(50)
            progress += 0.005f
        }
        if (progress >= 1f && !isPaused) {
            if (currentIndex < stories.size - 1) {
                currentIndex++
            } else {
                navController.popBackStack()
            }
        }
    }

    if (stories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("No stories available", color = Color.White)
        }
        return
    }

    val currentStory = stories[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width
                        when {
                            offset.x < width * 0.3f -> {
                                if (currentIndex > 0) currentIndex--
                            }
                            offset.x > width * 0.7f -> {
                                if (currentIndex < stories.size - 1) {
                                    currentIndex++
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    }
                )
            }
    ) {
        // Story content
        if (currentStory.mediaUrl.isNotBlank()) {
            AsyncImage(
                model = currentStory.mediaUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(android.graphics.Color.parseColor(currentStory.backgroundColor))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentStory.caption,
                    color = Color(android.graphics.Color.parseColor(currentStory.textColor)),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay
        Column(modifier = Modifier.fillMaxSize()) {
            // Progress bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stories.forEachIndexed { index, _ ->
                    val p = when {
                        index < currentIndex -> 1f
                        index == currentIndex -> progress
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(p)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                    }
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        currentStory.userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentStory.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(formatStoryTime(currentStory.createdAt), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isReacted = currentStory.reactions.containsKey(userId)
                IconButton(onClick = {
                    val newReactions = currentStory.reactions.toMutableMap()
                    if (isReacted) {
                        newReactions.remove(userId)
                    } else {
                        newReactions[userId] = "\u2764"
                    }
                    FirebaseConfig.storiesRef().child(currentStory.userId).child(currentStory.id).child("reactions").setValue(newReactions)
                }) {
                    Icon(
                        if (isReacted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "React",
                        tint = if (isReacted) Color.Red else Color.White
                    )
                }

                if (currentStory.userId == userId) {
                    IconButton(onClick = {
                        FirebaseConfig.storiesRef().child(currentStory.userId).child(currentStory.id).child("isDeleted").setValue(true)
                    }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                    }
                }
            }
        }
    }
}

fun formatStoryTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 7200000 -> "1h ago"
        else -> "${diff / 3600000}h ago"
    }
}
