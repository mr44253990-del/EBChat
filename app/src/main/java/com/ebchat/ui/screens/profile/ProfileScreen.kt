package com.ebchat.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ebchat.data.model.User
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.data.remote.SupabaseClient
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.OnlineGreen
import com.ebchat.ui.theme.PinkAccent
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storageMetadata
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseConfig.getCurrentUserId()
    var user by remember { mutableStateOf<User?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(userId) {
        FirebaseConfig.usersRef().child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as? Map<String, Any?>
                if (map != null) {
                    user = User.fromMap(map.mapKeys { it.key.toString() })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            scope.launch {
                isUploading = true
                try {
                    // Upload to Supabase Storage
                    val bucket = SupabaseClient.storage.from(SupabaseClient.Buckets.PROFILE_IMAGES)
                    val fileName = "${userId}_${System.currentTimeMillis()}.jpg"

                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        bucket.upload(fileName, inputStream.readBytes())
                    }

                    // Get public URL
                    val publicUrl = bucket.publicUrl(fileName)

                    // Update user profile
                    FirebaseConfig.usersRef().child(userId).child("photoUrl").setValue(publicUrl)
                    FirebaseConfig.usersCollection().document(userId).update("photoUrl", publicUrl)

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PinkPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = PinkPrimary)
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
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark)),
                                    CircleShape
                                )
                                .border(4.dp, PinkLight, CircleShape)
                        ) {
                            if (user?.photoUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = user?.photoUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (user?.name ?: "U").take(1).uppercase(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Online indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                                .border(3.dp, Color.White, CircleShape)
                        )

                        // Edit button
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.BottomEnd),
                                color = PinkPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = { photoPicker.launch("image/*") },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PinkPrimary)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Icon(Icons.Default.PhotoCamera, "Change Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = user?.name ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkPrimaryDark
                    )

                    // Email
                    Text(
                        text = user?.email ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bio
                    if (user?.bio?.isNotBlank() == true) {
                        Text(
                            text = user?.bio ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(user?.postCount?.toString() ?: "0", "Posts")
                        StatItem(user?.followers?.size?.toString() ?: "0", "Followers")
                        StatItem(user?.following?.size?.toString() ?: "0", "Following")
                    }
                }
            }

            // Profile Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    ProfileOption(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        subtitle = "Update your information",
                        onClick = { navController.navigate(NavRoutes.EDIT_PROFILE) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                    ProfileOption(
                        icon = Icons.Default.PostAdd,
                        title = "My Posts",
                        subtitle = "View and manage your posts",
                        onClick = { navController.navigate(NavRoutes.POST_FEED) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                    ProfileOption(
                        icon = Icons.Default.Group,
                        title = "My Groups",
                        subtitle = "Groups you\'re a member of",
                        onClick = { navController.navigate(NavRoutes.GROUPS) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                    ProfileOption(
                        icon = Icons.Default.Bookmark,
                        title = "Saved Posts",
                        subtitle = "Posts you\'ve bookmarked",
                        onClick = { }
                    )
                }
            }

            // Logout Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        // Set offline
                        FirebaseConfig.usersRef().child(userId).child("isOnline").setValue(false)
                        FirebaseConfig.usersRef().child(userId).child("lastSeen").setValue(System.currentTimeMillis())
                        FirebaseConfig.logout()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.MAIN) { inclusive = true }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFE53935))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Logout",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = PinkPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
