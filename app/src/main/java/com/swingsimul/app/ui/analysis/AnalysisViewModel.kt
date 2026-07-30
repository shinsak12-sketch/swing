package com.swingsimul.app.ui.analysis

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swingsimul.app.analysis.VideoTimingAnalyzer
import com.swingsimul.app.data.VideoTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class AnalysisUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val timing: VideoTiming? = null,
    val frameIndex: Int = 0,
    val frame: ImageBitmap? = null,
)

class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AnalysisUiState())
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    private val retrieverLock = Mutex()
    private var retriever: MediaMetadataRetriever? = null
    private var loaded = false

    fun load(uriString: String) {
        if (loaded) return
        loaded = true
        val uri = Uri.parse(uriString)
        viewModelScope.launch {
            _state.value = AnalysisUiState(loading = true)
            try {
                val timing = VideoTimingAnalyzer.analyze(getApplication(), uri)
                retrieverLock.withLock {
                    retriever = MediaMetadataRetriever().apply {
                        setDataSource(getApplication(), uri)
                    }
                }
                val first = decodeFrame(0)
                _state.value = AnalysisUiState(
                    loading = false,
                    timing = timing,
                    frameIndex = 0,
                    frame = first,
                )
            } catch (e: Exception) {
                _state.value = AnalysisUiState(
                    loading = false,
                    error = e.message ?: "영상을 불러오지 못했습니다.",
                )
            }
        }
    }

    fun step(delta: Int) {
        val timing = _state.value.timing ?: return
        val target = (_state.value.frameIndex + delta)
            .coerceIn(0, (timing.frameCount - 1).coerceAtLeast(0))
        setIndex(target)
    }

    fun setIndex(index: Int) {
        val timing = _state.value.timing ?: return
        val clamped = index.coerceIn(0, (timing.frameCount - 1).coerceAtLeast(0))
        _state.value = _state.value.copy(frameIndex = clamped)
        viewModelScope.launch {
            val bmp = decodeFrame(clamped)
            // Guard against out-of-order decodes overwriting a newer index.
            if (_state.value.frameIndex == clamped) {
                _state.value = _state.value.copy(frame = bmp)
            }
        }
    }

    private suspend fun decodeFrame(index: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        retrieverLock.withLock {
            val r = retriever ?: return@withContext null
            try {
                r.getFrameAtIndex(index)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val r = retriever
        retriever = null
        r?.release()
    }
}
