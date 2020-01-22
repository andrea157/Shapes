package it.andrea.shapes

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class ClipPathManager : ClipManager {

    protected val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
        strokeWidth = 1f
    }

    var clipPathCreator: ClipPathCreator? = null

    override fun createMask(width: Int, height: Int): Path = path

    override fun getShadowConvexPath(): Path? = path

    override fun setupClipLayout(width: Int, height: Int) {
        path.apply {
            reset()
            createClipPath(width, height)?.let { clipPath ->
                set(clipPath)
            }
        }
    }

    override fun getPaint(): Paint = paint

    override fun requireBitmap(): Boolean = clipPathCreator?.requireBitmap() == true

    protected fun createClipPath(width: Int, height: Int): Path? =
        clipPathCreator?.createClipPath(width, height)
}