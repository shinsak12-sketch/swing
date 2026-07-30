package com.swingsimul.app.ui.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.swingsimul.app.R
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onOpenGallery: () -> Unit,
    onRecorded: (File) -> Unit,
    viewModel: CameraViewModel = viewModel(),
) {
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (permissions.allPermissionsGranted) {
            CameraContent(
                viewModel = viewModel,
                onOpenGallery = onOpenGallery,
                onRecorded = onRecorded,
            )
        } else {
            PermissionRequest(
                onGrant = { permissions.launchMultiplePermissionRequest() },
            )
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CameraViewModel,
    onOpenGallery: () -> Unit,
    onRecorded: (File) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing by viewModel.lensFacing.collectAsState()
    val captureState by viewModel.captureState.collectAsState()
    val elapsedMs by viewModel.elapsedMs.collectAsState()

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Keep one VideoCapture instance so the ViewModel can record against it.
    val videoCapture = remember {
        val recorder = Recorder.Builder()
            // Prefer the highest quality the device supports; the slow-motion
            // effect itself is applied during playback (0.25x / frame-step).
            .setQualitySelector(
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                ),
            )
            .build()
        VideoCapture.withOutput(recorder)
    }

    // (Re)bind camera use cases whenever the lens changes.
    androidx.compose.runtime.LaunchedEffect(lensFacing) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        // Top bar: elapsed timer while recording.
        if (captureState == CaptureState.RECORDING) {
            RecordingBadge(
                elapsedMs = elapsedMs,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
            )
        }

        // Bottom controls.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlButton(
                icon = Icons.Filled.PhotoLibrary,
                contentDescription = stringResource(R.string.gallery),
                enabled = captureState == CaptureState.IDLE,
                onClick = onOpenGallery,
            )

            RecordButton(
                recording = captureState == CaptureState.RECORDING,
                onClick = {
                    if (captureState == CaptureState.RECORDING) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording(videoCapture, onRecorded)
                    }
                },
            )

            ControlButton(
                icon = Icons.Filled.Cameraswitch,
                contentDescription = stringResource(R.string.switch_camera),
                enabled = captureState == CaptureState.IDLE,
                onClick = viewModel::toggleLens,
            )
        }
    }
}

@Composable
private fun RecordingBadge(elapsedMs: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.55f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
            )
            Text(
                text = formatElapsed(elapsedMs),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = if (enabled) 0.18f else 0.06f),
        modifier = Modifier.size(56.dp),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
            )
        }
    }
}

@Composable
private fun RecordButton(recording: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.25f),
        modifier = Modifier.size(84.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(if (recording) 34.dp else 64.dp)
                        .clip(if (recording) RoundedCornerShape(8.dp) else CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                )
            }
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun PermissionRequest(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.permission_rationale),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 12.dp),
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 28.dp)
                .aspectRatio(3.2f)
                .fillMaxWidth(),
        ) {
            IconButton(onClick = onGrant, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.grant_permission),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
