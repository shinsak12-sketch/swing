package com.swingsimul.app.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.camera.video.Recorder
import androidx.lifecycle.AndroidViewModel
import com.swingsimul.app.data.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class CaptureState { IDLE, RECORDING }

class CameraViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RecordingRepository(app)

    private val _lensFacing = MutableStateFlow(androidx.camera.core.CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _captureState = MutableStateFlow(CaptureState.IDLE)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private var activeRecording: Recording? = null

    fun toggleLens() {
        if (_captureState.value != CaptureState.IDLE) return
        _lensFacing.value =
            if (_lensFacing.value == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
            } else {
                androidx.camera.core.CameraSelector.LENS_FACING_BACK
            }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        videoCapture: VideoCapture<Recorder>,
        onFinalized: (File) -> Unit,
    ) {
        if (_captureState.value == CaptureState.RECORDING) return

        val context = getApplication<Application>()
        val targetFile = repository.newRecordingFile()
        val outputOptions = FileOutputOptions.Builder(targetFile).build()

        val audioGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        val pending = videoCapture.output
            .prepareRecording(context, outputOptions)
            .apply { if (audioGranted) withAudioEnabled() }

        _captureState.value = CaptureState.RECORDING
        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Status -> {
                    _elapsedMs.value = event.recordingStats.recordedDurationNanos / 1_000_000
                }
                is VideoRecordEvent.Finalize -> {
                    _captureState.value = CaptureState.IDLE
                    _elapsedMs.value = 0L
                    activeRecording = null
                    if (!event.hasError()) {
                        onFinalized(targetFile)
                    } else {
                        // Clean up a failed/empty capture.
                        targetFile.delete()
                    }
                }
                else -> Unit
            }
        }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    override fun onCleared() {
        super.onCleared()
        activeRecording?.stop()
        activeRecording = null
    }
}
