package com.swingsimul.app.ui.analysis

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swingsimul.app.analysis.SwingAnalyzer
import com.swingsimul.app.data.SwingAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalysisUiState(
    val loading: Boolean = true,
    val progress: Float = 0f,
    val error: String? = null,
    val analysis: SwingAnalysis? = null,
    val overlay: ImageBitmap? = null,
)

class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val analyzer = SwingAnalyzer(app)

    private val _state = MutableStateFlow(AnalysisUiState())
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    private var started = false

    fun load(uriString: String) {
        if (started) return
        started = true
        val uri = Uri.parse(uriString)
        viewModelScope.launch {
            _state.value = AnalysisUiState(loading = true, progress = 0f)
            try {
                val analysis = analyzer.analyze(uri) { p ->
                    _state.value = _state.value.copy(progress = p)
                }
                val overlay = withContext(Dispatchers.IO) { buildOverlay(uri, analysis) }
                _state.value = AnalysisUiState(
                    loading = false,
                    analysis = analysis,
                    overlay = overlay,
                )
            } catch (e: Exception) {
                _state.value = AnalysisUiState(
                    loading = false,
                    error = e.message ?: "분석에 실패했습니다.",
                )
            }
        }
    }

    private fun buildOverlay(uri: Uri, analysis: SwingAnalysis): ImageBitmap? {
        val frame = analyzer.decodeFrame(uri, analysis.impactFrame) ?: return null
        val canvasBmp = frame.copy(Bitmap.Config.ARGB_8888, true) ?: return null
        if (canvasBmp != frame) frame.recycle()

        val sx = canvasBmp.width.toFloat() / analysis.analysisWidth
        val sy = canvasBmp.height.toFloat() / analysis.analysisHeight
        val stroke = (canvasBmp.width / 240f).coerceAtLeast(2f)
        val canvas = Canvas(canvasBmp)

        // Ball
        analysis.ballCenter?.let { c ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(80, 220, 120)
                style = Paint.Style.STROKE
                strokeWidth = stroke
            }
            canvas.drawCircle(c.x * sx, c.y * sy, analysis.ballRadius * sx + stroke, paint)
        }

        drawPath(canvas, analysis.clubPath, sx, sy, Color.rgb(245, 200, 70), stroke)   // club: yellow
        drawPath(canvas, analysis.ballPath, sx, sy, Color.rgb(90, 200, 240), stroke)   // ball flight: cyan

        return canvasBmp.asImageBitmap()
    }

    private fun drawPath(
        canvas: Canvas,
        points: List<PointF>,
        sx: Float,
        sy: Float,
        color: Int,
        stroke: Float,
    ) {
        if (points.isEmpty()) return
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        for (i in 1 until points.size) {
            canvas.drawLine(
                points[i - 1].x * sx, points[i - 1].y * sy,
                points[i].x * sx, points[i].y * sy,
                line,
            )
        }
        for (p in points) {
            canvas.drawCircle(p.x * sx, p.y * sy, stroke * 1.6f, dot)
        }
    }
}
