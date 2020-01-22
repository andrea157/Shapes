package it.andrea.shapes

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat

/**
 * Created by a.gerardi@wakala.it on 17-01-2020
 */
open class ShapeLayout : FrameLayout {

    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()

    protected val pdMode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    protected var drawable: Drawable? = null
    private val clipManager: ClipManager = ClipPathManager()
    private var requiersShapeUpdate = true
    private var clipBitmap: Bitmap? = null

    private val rectView = Path()

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

    override fun setBackground(background: Drawable?) {
        //TODO disabled here, please set a background to to this view child
        //super.setBackground(background);
    }

    override fun setBackgroundResource(resid: Int) {
        //TODO disabled here, please set a background to to this view child
        //super.setBackgroundResource(resid);
    }

    override fun setBackgroundColor(color: Int) {
        //TODO disabled here, please set a background to to this view child
        //super.setBackgroundColor(color);
    }


    private fun init(context: Context, attrs: AttributeSet? = null) {
        clipPaint.apply {
            isAntiAlias = true
            this@ShapeLayout.isDrawingCacheEnabled = true
            this@ShapeLayout.setWillNotDraw(false)
            color = Color.BLUE
            style = Paint.Style.FILL
            strokeWidth = 1f

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                this@ShapeLayout.setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    clipPaint
                ) //Only works for software layers
            } else {
                xfermode = pdMode
                this@ShapeLayout.setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    null
                ) //Only works for software layers
            }
        }

        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout).apply {
                if (hasValue(R.styleable.ShapeLayout_drawableId)) {
                    getResourceId(R.styleable.ShapeLayout_drawableId, -1)
                        .let { resourceId ->
                            if (-1 != resourceId) {
                                setDrawable(resourceId)
                            }
                        }
                    recycle()
                }
            }
        }
    }

    protected open fun dpToPx(dp: Float): Float {
        return dp * this.context.resources.displayMetrics.density
    }

    protected open fun pxToDp(px: Float): Float {
        return px / this.context.resources.displayMetrics.density
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            requiresShapeUpdate()
        }
    }

    private fun requiresBitmap(): Boolean {
        return isInEditMode || clipManager != null && clipManager.requireBitmap() || drawable != null
    }

    fun setDrawableTest(drawable: Drawable?) {
        this.drawable = drawable
        requiresShapeUpdate()
    }

    open fun setDrawable(redId: Int) {
        setDrawableTest(AppCompatResources.getDrawable(context, redId))
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (requiersShapeUpdate) {
            calculateLayout(canvas.width, canvas.height)
            requiersShapeUpdate = false
        }
        if (requiresBitmap()) {
            clipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            clipBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, clipPaint)
            }
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                canvas.drawPath(clipPath, clipPaint)
            } else {
                canvas.drawPath(rectView, clipPaint)
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }


    private fun calculateLayout(width: Int, height: Int) {
        rectView.reset()
        rectView.addRect(
            0f,
            0f,
            1f * getWidth(),
            1f * getHeight(),
            Path.Direction.CW
        )
        if (width > 0 && height > 0) {
            clipManager.setupClipLayout(width, height)
            clipPath.reset()
            clipPath.set(clipManager.createMask(width, height))
            if (requiresBitmap()) {
                clipBitmap?.recycle()
                val canvas =
                    Canvas(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                        clipBitmap = it
                    })
                drawable?.let {
                    it.setBounds(0, 0, width, height)
                    it.draw(canvas)
                } ?: canvas.drawPath(clipPath, clipManager.getPaint())
            }
            //invert the path for android P
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val success =
                    rectView.op(clipPath, Path.Op.DIFFERENCE)
            }
            //this needs to be fixed for 25.4.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ViewCompat.getElevation(
                    this
                ) > 0f
            ) {
                try {
                    outlineProvider = outlineProvider
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        postInvalidate()
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getOutlineProvider(): ViewOutlineProvider? {
        return object : ViewOutlineProvider() {
            override fun getOutline(
                view: View,
                outline: Outline
            ) {
                if (!isInEditMode) {
                    val shadowConvexPath = clipManager.getShadowConvexPath()
                    if (shadowConvexPath != null) {
                        try {
                            outline.setConvexPath(shadowConvexPath)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    open fun setClipPathCreator(createClipPath: ClipPathCreator?) {
        (clipManager as ClipPathManager).clipPathCreator = createClipPath
        requiresShapeUpdate()
    }

    open fun requiresShapeUpdate() {
        requiersShapeUpdate = true
        postInvalidate()
    }


}