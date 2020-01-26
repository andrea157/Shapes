package it.andrea.shapes.layouts

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import it.andrea.shapes.ClipPathCreator
import it.andrea.shapes.R
import it.andrea.shapes.ShapeLayout

/**
 * Created by a.gerardi@wakala.it on 23-01-2020
 */
class ParallelogramLayout : ShapeLayout {

    var heightProjectionPx = 0f
        set(value) {
            field = value
            requiresShapeUpdate()
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet? = null) {

        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ParallelogramLayout).apply {

                heightProjectionPx = getDimensionPixelSize(
                    R.styleable.ParallelogramLayout_height_projection,
                    heightProjectionPx.toInt()
                ).toFloat()

                recycle()
            }
        }

        setClipPathCreator(object : ClipPathCreator {
            override fun createClipPath(width: Int, height: Int): Path {
                return Path().apply {
                    moveTo(0f, height.toFloat())
                    lineTo(0f + heightProjectionPx, 0f)
                    lineTo(width.toFloat(), 0f)
                    lineTo(width.toFloat() - heightProjectionPx, height.toFloat())
                    close()
                }
            }

            override fun requireBitmap(): Boolean = false

        })

    }

    fun getHeightProjectionDp(): Float {
        return pxToDp(heightProjectionPx)
    }

    fun setHeightProjectionDp(heightProjection: Float) {
        heightProjectionPx = dpToPx(heightProjection)
    }
}