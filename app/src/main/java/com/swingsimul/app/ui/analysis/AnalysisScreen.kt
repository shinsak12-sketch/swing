package com.swingsimul.app.ui.analysis

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swingsimul.app.R
import com.swingsimul.app.data.VideoTiming
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    uriString: String,
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = viewModel(),
) {
    LaunchedEffect(uriString) { viewModel.load(uriString) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analysis)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                state.error != null -> Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )
                else -> AnalysisContent(
                    state = state,
                    onStep = viewModel::step,
                    onSeek = viewModel::setIndex,
                )
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    state: AnalysisUiState,
    onStep: (Int) -> Unit,
    onSeek: (Int) -> Unit,
) {
    val timing = state.timing ?: return
    val lastIndex = (timing.frameCount - 1).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        // Frame viewer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val frame = state.frame
            if (frame != null) {
                Image(
                    bitmap = frame,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Text(
                    text = "#${state.frameIndex} / $lastIndex",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        // Frame controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FrameStepButton(Icons.Filled.NavigateBefore, stringResource(R.string.prev_frame)) {
                onStep(-1)
            }
            Slider(
                value = state.frameIndex.toFloat(),
                onValueChange = { onSeek(it.roundToInt()) },
                valueRange = 0f..lastIndex.toFloat().coerceAtLeast(0f),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            FrameStepButton(Icons.Filled.NavigateNext, stringResource(R.string.next_frame)) {
                onStep(1)
            }
        }

        // Timing report — the actual deliverable of this first step.
        TimingReport(timing = timing, currentPtsUs = timing.ptsUs.getOrNull(state.frameIndex))
    }
}

@Composable
private fun FrameStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        modifier = Modifier.size(48.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TimingReport(timing: VideoTiming, currentPtsUs: Long?) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.timing_report),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ReportRow(stringResource(R.string.frame_count), "${timing.frameCount}")
            ReportRow(stringResource(R.string.duration), "%.3f s".format(timing.durationUs / 1_000_000.0))
            ReportRow(stringResource(R.string.avg_fps), "%.1f fps".format(timing.avgFps))
            ReportRow(
                stringResource(R.string.frame_gap),
                "중앙값 %.2f · 최소 %.2f · 최대 %.2f ms".format(
                    timing.medianDeltaUs / 1000.0,
                    timing.minDeltaUs / 1000.0,
                    timing.maxDeltaUs / 1000.0,
                ),
            )
            if (currentPtsUs != null) {
                ReportRow(stringResource(R.string.current_pts), "%.3f s".format(currentPtsUs / 1_000_000.0))
            }

            val slow = timing.slowSegment
            val slowText = if (slow != null) {
                "프레임 %d–%d · 추정 %.0f fps".format(slow.startFrame, slow.endFrame, slow.impliedFps)
            } else {
                "감지 안 됨 (타이밍 균일 → 컨테이너에서 이미 시간 확장됨)"
            }
            ReportRow(stringResource(R.string.slow_segment), slowText)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
