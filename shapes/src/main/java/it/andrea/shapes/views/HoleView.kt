package it.andrea.shapes.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import it.andrea.shapes.R
import kotlin.math.max
import kotlin.math.min


//https://medium.com/@rey5137/let-s-drill-a-hole-in-your-view-e7f53fa23376
//https://medium.com/@dbottillo/animate-android-custom-view-a94473412054
class HoleView : View {

    private var fillColor: Int = Color.BLACK
        private set(value) {
            field = value
            paintWithoutHole = getDefaultBkg()
        }
    private var defaultSizeOffsetPercent: Float = 1f
    private var defaultSwitchPercent: Float = 1f
    private var defaultAnimDuration: Long = 300

    private val displayPoint = Point()
    private var paintWithoutHole: Paint
    private var paintWithHole: Paint? = null
    private var pathWithHole: Path? = null

    init {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).let { windowManager ->
            windowManager.defaultDisplay.getSize(displayPoint)
        }

        paintWithoutHole = getDefaultBkg()
    }

    /**
     * Android View Callbacks **********************************************************************
     */
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val tmpPaint = paintWithHole
        val tmpPath = pathWithHole
        when {
            tmpPath != null && tmpPaint != null -> {
                canvas?.drawPath(tmpPath, tmpPaint)
                paintWithHole = null
                pathWithHole = null
            }
            tmpPaint != null -> {
                canvas?.drawRect(
                    0f, 0f, displayPoint.x.toFloat(), displayPoint.y.toFloat(),
                    tmpPaint
                )
                paintWithHole = null
            }
            else -> {
                canvas?.drawRect(
                    0f,
                    0f,
                    displayPoint.x.toFloat(),
                    displayPoint.y.toFloat(),
                    paintWithoutHole
                )
            }
        }
    }

    private fun init(context: Context?, attrs: AttributeSet? = null) {
        context?.let { ctx ->
            attrs?.let { attrs ->
                ctx.obtainStyledAttributes(attrs, R.styleable.HoleView).apply {
                    fillColor = getColor(R.styleable.HoleView_color_fill, Color.BLACK)

                    defaultSizeOffsetPercent =
                        getFloat(R.styleable.HoleView_default_size_offset_percent, 1f)

                    defaultSwitchPercent = getFloat(R.styleable.HoleView_default_switch_percent, 1f)

                    defaultAnimDuration =
                        getInteger(R.styleable.HoleView_anim_duration, 300).toLong()

                    recycle()
                }
            }
        }
    }

    private fun getDefaultBkg(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
    }


    /**
     * UI API **************************************************************************************
     */
    private var viewToShow: View? = null
    private var currentSizeOffsetPercent: Float? = null
    private var currentSwitchPercent: Float? = null

    /**
     * @param duration - animation duration
     * @param viewToShow - view around which the hole is built
     * @param sizeOffsetPercent - defines the size of the hole as a percentage of the largest size of viewToShow
     * @param switchPercent - defines the threshold of difference between the major and minor size of the reference view to switch to an RoundRect hole
     */
    fun animateHole(
        viewToShow: View,
        duration: Long = defaultAnimDuration,
        sizeOffsetPercent: Float = defaultSizeOffsetPercent,
        switchPercent: Float = defaultSwitchPercent,
        reverse: Boolean = false
    ) {
        //TODO Check viewToShow id valid....
        //TODO Check for no fullscreen....
        this.viewToShow = viewToShow
        this.currentSizeOffsetPercent = sizeOffsetPercent
        this.currentSwitchPercent = switchPercent

        val maxSize = max(viewToShow.width, viewToShow.height)
        val minSize = min(viewToShow.width, viewToShow.height).toFloat()
        if ((minSize / maxSize) < switchPercent) {
            animateRoundRectHole(duration, viewToShow, reverse)
        } else {
            animateCircleHole(duration, viewToShow, maxSize * sizeOffsetPercent, reverse)
        }
    }

    private fun animateCircleHole(
        duration: Long,
        viewToShow: View,
        radius: Float,
        reverse: Boolean = false
    ) {

        val coordinates = IntArray(2)
        viewToShow.getLocationInWindow(coordinates)

        val centerX = coordinates[0].toFloat() + viewToShow.width / 2
        val centerY = coordinates[1].toFloat() + viewToShow.height / 2

        val startValue: Float
        val endValue: Float
        if (reverse) {
            startValue = radius
            endValue = 0f
        } else {
            endValue = radius
            startValue = 0f
        }

        ValueAnimator.ofFloat(startValue, endValue).apply {
            this.duration = duration
            interpolator = OvershootInterpolator()
            addUpdateListener {
                (animatedValue as? Float)?.let { currentRadius ->
                    if (currentRadius > 0) {
                        paintWithHole = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            shader = RadialGradient(
                                centerX,
                                centerY, currentRadius,
                                intArrayOf(
                                    Color.TRANSPARENT,
                                    Color.TRANSPARENT,
                                    fillColor
                                ),
                                floatArrayOf(0f, 0.99f, 1f),
                                Shader.TileMode.CLAMP
                            )
                        }
                        invalidate()
                    } else if (reverse) {
                        this@HoleView.viewToShow = null
                        this@HoleView.currentSizeOffsetPercent = null
                        this@HoleView.currentSwitchPercent = null
                        invalidate()
                    }
                }
            }
        }.start()
    }


    private fun animateRoundRectHole(duration: Long, viewToShow: View, reverse: Boolean = false) {

        val coordinates = IntArray(2)
        viewToShow.getLocationInWindow(coordinates)

        val halfX = (viewToShow.width / 2)
        val halfY = (viewToShow.height / 2)
        val centerX = coordinates[0].toFloat() + halfX
        val centerY = coordinates[1].toFloat() + halfY
        val radius = min(halfX, halfY).toFloat()

        val startValue: Float
        val endValue: Float
        if (reverse) {
            startValue = 1f
            endValue = 0.0f
        } else {
            startValue = 0.0f
            endValue = 1f
        }

        ValueAnimator.ofFloat(startValue, endValue).apply {
            this.duration = duration
            interpolator = OvershootInterpolator()
            addUpdateListener {
                (animatedValue as? Float)?.let { currentPercent ->
                    if (currentPercent > 0) {
                        pathWithHole = Path().apply {
                            fillType = Path.FillType.WINDING
                            addRect(
                                0f,
                                0f,
                                displayPoint.x.toFloat(),
                                displayPoint.y.toFloat(),
                                Path.Direction.CW
                            )
                            addRoundRect(
                                centerX - (halfX * currentPercent),
                                centerY - (halfY * currentPercent),
                                centerX + (halfX * currentPercent),
                                centerY + (halfY * currentPercent),
                                radius,
                                radius,
                                Path.Direction.CCW
                            )
                        }
                        paintWithHole = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            color = fillColor
                        }
                        invalidate()
                    } else if (reverse) {
                        this@HoleView.viewToShow = null
                        this@HoleView.currentSizeOffsetPercent = null
                        this@HoleView.currentSwitchPercent = null
                        invalidate()
                    }
                }
            }
        }.start()
    }


    fun removeAllHole() {
        viewToShow?.let { viewToShow ->
            currentSizeOffsetPercent?.let { sizeOffsetPercent ->
                currentSwitchPercent?.let { switchPercent ->
                    animateHole(
                        viewToShow = viewToShow,
                        sizeOffsetPercent = sizeOffsetPercent,
                        switchPercent = switchPercent,
                        reverse = true
                    )
                } ?: invalidate()
            } ?: invalidate()
        } ?: invalidate()
    }

}