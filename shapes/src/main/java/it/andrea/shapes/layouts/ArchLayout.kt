package it.andrea.shapes.layouts

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import androidx.annotation.IntDef
import it.andrea.shapes.ClipPathCreator
import it.andrea.shapes.R
import it.andrea.shapes.ShapeLayout
import kotlin.math.abs

/**
 * Created by a.gerardi@wakala.it on 17-01-2020
 */
class ArchLayout : ShapeLayout {

    @ArcPosition
    var arcPosition = POSITION_TOP
        set(value) {
            field = value
            requiresShapeUpdate()
        }

    var arcHeightPx = 0f
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
            context.obtainStyledAttributes(attrs, R.styleable.ArchLayout).apply {
                arcHeightPx = getDimensionPixelSize(
                    R.styleable.ArchLayout_arch_height,
                    arcHeightPx.toInt()
                ).toFloat()

                arcPosition = getInteger(R.styleable.ArchLayout_arch_position, arcPosition)

                recycle()
            }
        }

        setClipPathCreator(object : ClipPathCreator {
            override fun createClipPath(width: Int, height: Int): Path {
                return Path().apply {
                    val isCropInside = getCropDirection() == CROP_INSIDE
                    val arcHeightAbs = abs(arcHeightPx)

                    when (arcPosition) {
                        POSITION_BOTTOM -> {
                            if (isCropInside) {
                                moveTo(0f, 0f)
                                lineTo(0f, height.toFloat())
                                quadTo(
                                    width / 2.toFloat(),
                                    height - 2 * arcHeightAbs,
                                    width.toFloat(),
                                    height.toFloat()
                                )
                                lineTo(width.toFloat(), 0f)
                                close()
                            } else {
                                moveTo(0f, 0f)
                                lineTo(0f, height - arcHeightAbs)
                                quadTo(
                                    width / 2.toFloat(),
                                    height + arcHeightAbs,
                                    width.toFloat(),
                                    height - arcHeightAbs
                                )
                                lineTo(width.toFloat(), 0f)
                                close()
                            }
                        }
                        POSITION_TOP -> {
                            if (isCropInside) {
                                moveTo(0f, height.toFloat())
                                lineTo(0f, 0f)
                                quadTo(width / 2.toFloat(), 2 * arcHeightAbs, width.toFloat(), 0f)
                                lineTo(width.toFloat(), height.toFloat())
                                close()
                            } else {
                                moveTo(0f, arcHeightAbs)
                                quadTo(
                                    width / 2.toFloat(),
                                    -arcHeightAbs,
                                    width.toFloat(),
                                    arcHeightAbs
                                )
                                lineTo(width.toFloat(), height.toFloat())
                                lineTo(0f, height.toFloat())
                                close()
                            }
                        }
                        POSITION_LEFT -> {
                            if (isCropInside) {
                                moveTo(width.toFloat(), 0f)
                                lineTo(0f, 0f)
                                quadTo(
                                    arcHeightAbs * 2,
                                    height / 2.toFloat(),
                                    0f,
                                    height.toFloat()
                                )
                                lineTo(width.toFloat(), height.toFloat())
                                close()
                            } else {
                                moveTo(width.toFloat(), 0f)
                                lineTo(arcHeightAbs, 0f)
                                quadTo(
                                    -arcHeightAbs,
                                    height / 2.toFloat(),
                                    arcHeightAbs,
                                    height.toFloat()
                                )
                                lineTo(width.toFloat(), height.toFloat())
                                close()
                            }
                        }
                        POSITION_RIGHT -> {
                            if (isCropInside) {
                                moveTo(0f, 0f)
                                lineTo(width.toFloat(), 0f)
                                quadTo(
                                    width - arcHeightAbs * 2,
                                    height / 2.toFloat(),
                                    width.toFloat(),
                                    height.toFloat()
                                )
                                lineTo(0f, height.toFloat())
                                close()
                            } else {
                                moveTo(0f, 0f)
                                lineTo(width - arcHeightAbs, 0f)
                                quadTo(
                                    width + arcHeightAbs,
                                    height / 2.toFloat(),
                                    width - arcHeightAbs,
                                    height.toFloat()
                                )
                                lineTo(0f, height.toFloat())
                                close()
                            }
                        }
                    }
                }
            }

            override fun requireBitmap(): Boolean = false
        })
    }

    fun getCropDirection(): Int {
        return if (arcHeightPx > 0) {
            CROP_OUTSIDE
        } else {
            CROP_INSIDE
        }
    }

    fun getArcHeightDp(): Float {
        return pxToDp(arcHeightPx)
    }

    fun setArcHeightDp(arcHeight: Float) {
        arcHeightPx = dpToPx(arcHeight)
    }


    @IntDef(POSITION_BOTTOM, POSITION_TOP, POSITION_LEFT, POSITION_RIGHT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ArcPosition

    @IntDef(CROP_INSIDE, CROP_OUTSIDE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CropDirection

    companion object {
        const val POSITION_BOTTOM = 1
        const val POSITION_TOP = 2
        const val POSITION_LEFT = 3
        const val POSITION_RIGHT = 4

        const val CROP_INSIDE = 1
        const val CROP_OUTSIDE = 2
    }
}