package com.ebchat.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark

data class ThemeOption(
    val name: String,
    val key: String,
    val primaryColor: Color,
    val gradientColors: List<Color>
)

val themes = listOf(
    ThemeOption("Pink Glass", "pink_glass", Color(0xFFFF69B4), listOf(Color(0xFFFF69B4), Color(0xFFFFB6C1))),
    ThemeOption("Ocean Blue", "blue", Color(0xFF2196F3), listOf(Color(0xFF2196F3), Color(0xFF64B5F6))),
    ThemeOption("Royal Purple", "purple", Color(0xFF9C27B0), listOf(Color(0xFF9C27B0), Color(0xFFCE93D8))),
    ThemeOption("Forest Green", "green", Color(0xFF4CAF50), listOf(Color(0xFF4CAF50), Color(0xFFA5D6A7))),
    ThemeOption("Sunset Orange", "orange", Color(0xFFFF9800), listOf(Color(0xFFFF9800), Color(0xFFFFCC80))),
    ThemeOption("Ruby Red", "red", Color(0xFFE91E63), listOf(Color(0xFFE91E63), Color(0xFFF48FB1))),
    ThemeOption("Teal Wave", "teal", Color(0xFF009688), listOf(Color(0xFF009688), Color(0xFF80CBC4))),
    ThemeOption("Dark Night", "dark", Color(0xFF9C27B0), listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(navController: NavHostController) {
    val userId = FirebaseConfig.getCurrentUserId()
    var selectedTheme by remember { mutableStateOf("pink_glass") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PinkPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = PinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Choose your favorite theme",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(themes) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = selectedTheme == theme.key,
                    onSelect = {
                        selectedTheme = theme.key
                        FirebaseConfig.usersRef().child(userId).child("themePreference").setValue(theme.key)
                        FirebaseConfig.usersCollection().document(userId).update("themePreference", theme.key)
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(theme: ThemeOption, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .then(if (isSelected) Modifier.border(2.dp, PinkPrimary, RoundedCornerShape(20.dp)) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(theme.gradientColors))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(theme.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Tap to apply", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
