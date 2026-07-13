package com.ebchat.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark

// ===== NOTIFICATIONS SCREEN =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            SettingsCard {
                SettingsToggleItem(Icons.Default.Notifications, "Message Notifications", "Get notified for new messages", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Notifications, "Post Reactions", "Notifications for likes and comments", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Notifications, "New Stories", "When friends post stories", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Notifications, "Group Activity", "Group messages and updates", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Notifications, "Mentions", "When someone mentions you", true) {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "\u2022 All notifications will be sent via FCM\n\u2022 You can reply directly from notification panel\n\u2022 Sound and vibration can be customized",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                lineHeight = 20.sp
            )
        }
    }
}

// ===== BLOCKED USERS SCREEN =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Users", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = PinkLight)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No blocked users", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Users you block will appear here", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

// ===== MUTED USERS SCREEN =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedUsersScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muted Users", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = PinkLight)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No muted users", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Muted users won't send notifications", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

// ===== PRIVACY SCREEN =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            SettingsCard {
                SettingsToggleItem(Icons.Default.Security, "Online Status", "Show when you're online", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Security, "Read Receipts", "Show when you've read messages", true) {}
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(Icons.Default.Security, "Profile Photo", "Who can see your photo", true) {}
            }
        }
    }
}

// ===== ABOUT SCREEN =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PinkPrimary, PinkPrimaryDark)),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("EB", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("EBChat", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
            Text("Version 1.0.0", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "About EBChat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PinkPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "EBChat is a modern messaging and social media application built with love. Connect with friends, share stories, create posts, and stay connected with your community.",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Features:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PinkPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    val features = listOf(
                        "\u2022 Real-time messaging with reply, forward, delete",
                        "\u2022 Voice messages and media sharing",
                        "\u2022 Stories with auto-delete after 12 hours",
                        "\u2022 Post feed with reactions and comments",
                        "\u2022 Group chats with admin controls",
                        "\u2022 Beautiful pink glass theme design",
                        "\u2022 Push notifications with FCM",
                        "\u2022 Online status and typing indicators",
                        "\u2022 Block and mute users",
                        "\u2022 Customizable themes"
                    )
                    features.forEach { feature ->
                        Text(feature, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "\u00a9 2025 EBChat. All rights reserved.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
