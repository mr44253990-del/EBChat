package com.ebchat.ui.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.Post
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.data.remote.SupabaseClient
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseConfig.getCurrentUserId()
    var userName by remember { mutableStateOf("") }
    var userPhotoUrl by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPosting by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        FirebaseConfig.usersRef().child(userId).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val map = snapshot.value as? Map<String, Any?>
                if (map != null) {
                    userName = map["name"] as? String ?: ""
                    userPhotoUrl = map["photoUrl"] as? String ?: ""
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        mediaUris = mediaUris + uris
    }

    // Video picker
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { mediaUris = mediaUris + it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank() && mediaUris.isEmpty()) return@Button
                            scope.launch {
                                isPosting = true
                                val postId = UUID.randomUUID().toString()
                                val uploadedUrls = mutableListOf<String>()
                                val mediaTypes = mutableListOf<String>()

                                // Upload media to Supabase
                                mediaUris.forEach { uri ->
                                    try {
                                        val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true
                                        val bucket = if (isVideo) {
                                            SupabaseClient.storage.from(SupabaseClient.Buckets.POST_MEDIA)
                                        } else {
                                            SupabaseClient.storage.from(SupabaseClient.Buckets.POST_MEDIA)
                                        }
                                        val fileName = "${userId}_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                            bucket.upload(fileName, inputStream.readBytes())
                                        }
                                        uploadedUrls.add(bucket.publicUrl(fileName))
                                        mediaTypes.add(if (isVideo) "video" else "image")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                val tagList = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                val post = Post(
                                    id = postId,
                                    userId = userId,
                                    userName = userName,
                                    userPhotoUrl = userPhotoUrl,
                                    title = title,
                                    content = content,
                                    tags = tagList,
                                    mediaUrls = uploadedUrls,
                                    mediaTypes = mediaTypes,
                                    createdAt = System.currentTimeMillis()
                                )

                                FirebaseConfig.postsRef().child(postId).setValue(post.toMap())
                                FirebaseConfig.usersRef().child(userId).child("postCount").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                        val current = snapshot.getValue(Int::class.java) ?: 0
                                        FirebaseConfig.usersRef().child(userId).child("postCount").setValue(current + 1)
                                    }
                                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                                })

                                isPosting = false
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        enabled = !isPosting && (title.isNotBlank() || content.isNotBlank() || mediaUris.isNotEmpty())
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Post", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary
                ),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. technology, news, fun") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media Previews
            if (mediaUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mediaUris) { uri ->
                        Box {
                            Card(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = { mediaUris = mediaUris - uri },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MediaOptionButton(Icons.Default.PhotoCamera, "Photo") {
                        imagePicker.launch("image/*")
                    }
                    MediaOptionButton(Icons.Default.Videocam, "Video") {
                        videoPicker.launch("video/*")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MediaOptionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PinkLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = PinkPrimary, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}
