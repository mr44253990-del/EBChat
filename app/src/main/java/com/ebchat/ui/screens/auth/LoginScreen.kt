package com.ebchat.ui.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ebchat.data.remote.FirebaseConfig
import com.ebchat.data.remote.SupabaseClient
import com.ebchat.ui.navigation.NavRoutes
import com.ebchat.ui.theme.GlassBackground
import com.ebchat.ui.theme.GlassSurface
import com.ebchat.ui.theme.PinkAccent
import com.ebchat.ui.theme.PinkLight
import com.ebchat.ui.theme.PinkPrimary
import com.ebchat.ui.theme.PinkPrimaryDark
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseConfig.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Google Sign In Setup
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("404800892817-4ovh3uvpn9mhee3kgedub3sr1k00udg1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                scope.launch {
                    isLoading = true
                    try {
                        auth.signInWithCredential(credential).await()
                        // Save FCM token
                        val userId = FirebaseConfig.getCurrentUserId()
                        if (userId.isNotBlank()) {
                            runCatching {
                                val token = FirebaseMessaging.getInstance().token.await()
                                FirebaseConfig.usersRef().child(userId).child("fcmToken").setValue(token)
                                FirebaseConfig.usersRef().child(userId).child("isOnline").setValue(true)
                            }.onFailure { Log.w("LoginScreen", "Google post-login update failed", it) }
                        }

                        isLoading = false
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Google signInWithCredential failed", e)
                        isLoading = false
                        errorMessage = e.localizedMessage ?: e.message ?: "Login failed"
                    }
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google Sign-In failed", e)
                isLoading = false
                errorMessage = "Google Sign-In failed: ${e.localizedMessage ?: e.message ?: e.statusCode.toString()}"
            }
        }
    }

    // Background Gradient
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            PinkPrimary.copy(alpha = 0.15f),
            GlassBackground,
            PinkLight.copy(alpha = 0.3f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo / App Name
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PinkPrimary, PinkPrimaryDark)
                        )
                    )
                    .shadow(8.dp, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EB",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EBChat",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PinkPrimaryDark
            )

            Text(
                text = "Welcome back! Login to continue",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Card with Glass Effect
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassSurface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PinkPrimary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            focusedLabelColor = PinkPrimary,
                            cursorColor = PinkPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PinkPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = PinkPrimary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            focusedLabelColor = PinkPrimary,
                            cursorColor = PinkPrimary
                        )
                    )

                    // Error Message
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password
                    TextButton(
                        onClick = {
                            if (email.isNotBlank()) {
                                scope.launch {
                                    try {
                                        auth.sendPasswordResetEmail(email).await()
                                        Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("LoginScreen", "sendPasswordResetEmail failed", e)
                                        errorMessage = e.localizedMessage ?: e.message ?: "Failed to send reset email"
                                    }
                                }
                            } else {
                                errorMessage = "Please enter your email first"
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Forgot Password?",
                            color = PinkPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    auth.signInWithEmailAndPassword(email, password).await()
                                    // Update online status
                                    val userId = FirebaseConfig.getCurrentUserId()
                                    if (userId.isNotBlank()) {
                                        runCatching {
                                            val token = FirebaseMessaging.getInstance().token.await()
                                            FirebaseConfig.usersRef().child(userId).child("fcmToken").setValue(token)
                                            FirebaseConfig.usersRef().child(userId).child("isOnline").setValue(true)
                                        }.onFailure { Log.w("LoginScreen", "post-login update failed", it) }
                                    }

                                    isLoading = false
                                    navController.navigate(NavRoutes.MAIN) {
                                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "signInWithEmailAndPassword failed", e)
                                    isLoading = false
                                    errorMessage = e.localizedMessage ?: e.message ?: "Login failed"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PinkPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Login",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = PinkLight)
                Text(
                    "  OR  ",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = PinkLight)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign In Button
            Button(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Icon
                    Text(
                        text = "G",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(onClick = {
                    navController.navigate(NavRoutes.SIGNUP)
                }) {
                    Text(
                        "Sign Up",
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
