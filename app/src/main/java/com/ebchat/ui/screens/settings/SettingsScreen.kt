package com.ebchat.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.OnlineGreen
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Appearance Section
            SettingsSectionTitle("Appearance")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Themes",
                    subtitle = "Customize app colors",
                    onClick = { navController.navigate(NavRoutes.THEMES) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(
                    icon = Icons.Default.BrightnessMedium,
                    title = "Dark Mode",
                    subtitle = "Switch to dark theme",
                    checked = false,
                    onCheckedChange = { }
                )
            }

            // Notifications Section
            SettingsSectionTitle("Notifications")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification Settings",
                    subtitle = "Manage notification preferences",
                    onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound",
                    subtitle = "Notification sounds",
                    checked = true,
                    onCheckedChange = { }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsToggleItem(
                    icon = Icons.Default.NotificationsOff,
                    title = "Mute All",
                    subtitle = "Temporarily mute notifications",
                    checked = false,
                    onCheckedChange = { }
                )
            }

            // Privacy Section
            SettingsSectionTitle("Privacy")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Settings",
                    subtitle = "Control your privacy",
                    onClick = { navController.navigate(NavRoutes.PRIVACY) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsItem(
                    icon = Icons.Default.PersonOff,
                    title = "Blocked Users",
                    subtitle = "Manage blocked accounts",
                    onClick = { navController.navigate(NavRoutes.BLOCKED_USERS) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsItem(
                    icon = Icons.Default.NotificationsOff,
                    title = "Muted Users",
                    subtitle = "Manage muted accounts",
                    onClick = { navController.navigate(NavRoutes.MUTED_USERS) }
                )
            }

            // About Section
            SettingsSectionTitle("About")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About EBChat",
                    subtitle = "Version 1.0.0",
                    onClick = { navController.navigate(NavRoutes.ABOUT) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = PinkLight.copy(alpha = 0.3f))
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help and contact us",
                    onClick = { }
                )
            }

            // Logout
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseConfig.usersRef().child(FirebaseConfig.getCurrentUserId()).child("isOnline").setValue(false)
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
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFE53935), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Logout", color = Color(0xFFE53935), fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = PinkPrimary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PinkLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = PinkPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PinkLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = PinkPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PinkPrimary,
                checkedTrackColor = PinkLight
            )
        )
    }
}
