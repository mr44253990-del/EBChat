package com.ebchat.ui.screens.auth

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.theme.GlassBackground
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkAccent
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 5 })
    var currentStep by remember { mutableStateOf(0) }

    // Check if onboarding was already shown
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val onboardingShown = prefs.getBoolean("onboarding_shown", false)

    if (onboardingShown) {
        LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
            }
        }
        return
    }

    // Permission states
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            PinkPrimary.copy(alpha = 0.1f),
            GlassBackground,
            PinkLight.copy(alpha = 0.2f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    icon = Icons.Default.Chat,
                    title = "Welcome to EBChat",
                    subtitle = "Connect with friends and family",
                    description = "A beautiful messaging experience with stunning pink glass theme design. Chat, share stories, and stay connected.",
                    step = 0,
                    totalSteps = 5
                )
                1 -> OnboardingPage(
                    icon = Icons.Default.Share,
                    title = "Share Your Moments",
                    subtitle = "Post, Stories & More",
                    description = "Share photos, videos, and posts with your friends. Create stories that disappear after 12 hours.",
                    step = 1,
                    totalSteps = 5
                )
                2 -> OnboardingPage(
                    icon = Icons.Default.Group,
                    title = "Group Chats",
                    subtitle = "Connect with communities",
                    description = "Create and join groups. Share messages, photos, videos, and voice messages with your groups.",
                    step = 2,
                    totalSteps = 5
                )
                3 -> OnboardingPage(
                    icon = Icons.Default.Notifications,
                    title = "Stay Notified",
                    subtitle = "Never miss a message",
                    description = "Get instant notifications even when the app is closed. Reply directly from notifications.",
                    step = 3,
                    totalSteps = 5
                )
                4 -> PermissionsPage(
                    notificationPermission = notificationPermission,
                    cameraPermission = cameraPermission,
                    audioPermission = audioPermission
                )
            }
        }

        // Bottom Navigation Buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                if (pagerState.currentPage < 4) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                prefs.edit().putBoolean("onboarding_shown", true).apply()
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text(
                            "Skip",
                            color = PinkPrimary,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(80.dp))
                }

                // Page Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val isSelected = pagerState.currentPage == index
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1f,
                            animationSpec = tween(300),
                            label = "indicator_scale"
                        )
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) PinkPrimary else PinkLight
                                )
                        )
                    }
                }

                // Next/Done Button
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 4) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                // Save onboarding shown
                                prefs.edit().putBoolean("onboarding_shown", true).apply()
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        }
                    },
                    containerColor = PinkPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    step: Int,
    totalSteps: Int
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "page_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Icon Card with Glass Effect
        Card(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(600, delayMillis = 100),
                    label = "icon_scale"
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale),
                    tint = PinkPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        val titleAlpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(500, delayMillis = 200),
            label = "title_alpha"
        )
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PinkPrimaryDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(titleAlpha)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = PinkPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage(
    notificationPermission: com.google.accompanist.permissions.PermissionState?,
    cameraPermission: com.google.accompanist.permissions.PermissionState,
    audioPermission: com.google.accompanist.permissions.PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Permissions",
            modifier = Modifier.size(80.dp),
            tint = PinkPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "App Permissions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PinkPrimaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EBChat needs the following permissions to work properly:",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Notification Permission
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Get notified about new messages",
            isGranted = notificationPermission?.status?.isGranted ?: true,
            onRequest = { notificationPermission?.launchPermissionRequest() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Permission
        PermissionItem(
            icon = Icons.Default.CameraAlt,
            title = "Camera",
            description = "Take photos and videos",
            isGranted = cameraPermission.status.isGranted,
            onRequest = { cameraPermission.launchPermissionRequest() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Permission
        PermissionItem(
            icon = Icons.Default.VideoLibrary,
            title = "Microphone",
            description = "Record voice messages",
            isGranted = audioPermission.status.isGranted,
            onRequest = { audioPermission.launchPermissionRequest() }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFE8F5E9) else GlassSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = if (isGranted) Color(0xFF4CAF50) else PinkPrimary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Allow")
                }
            } else {
                Text(
                    text = "✓ Granted",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
