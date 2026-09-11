package com.tonapp.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AssistantScreen()
            }
        }
    }
}

@Composable
fun AssistantScreen(viewModel: AssistantViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CAMERA
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission = grants[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        hasPermission = allGranted
        if (!allGranted) permissionLauncher.launch(permissions)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = when (state) {
                    is AssistantViewModel.State.Idle -> "Appuie pour parler"
                    is AssistantViewModel.State.Listening -> "🎤 Je t'écoute..."
                    is AssistantViewModel.State.Thinking -> "🧠 Je réfléchis..."
                    is AssistantViewModel.State.Speaking -> (state as AssistantViewModel.State.Speaking).message
                    is AssistantViewModel.State.Error -> (state as AssistantViewModel.State.Error).message
                },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            val micColor = when (state) {
                is AssistantViewModel.State.Listening -> Color.Red
                is AssistantViewModel.State.Thinking -> Color.Yellow
                else -> Color(0xFF4CAF50)
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(micColor)
                    .clickable(enabled = hasPermission && state is AssistantViewModel.State.Idle) {
                        viewModel.onMicPressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("", fontSize = 40.sp)
            }
        }
    }
}
