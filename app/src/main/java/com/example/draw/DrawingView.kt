package com.example.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(
    ctx: Context,
    attrs: AttributeSet
) : View(ctx, attrs) {
    private lateinit var drawPath: CustomPath
    private lateinit var drawPaint: Paint
    private lateinit var canvasBitmap: Bitmap
    private lateinit var canvasPaint: Paint
    private lateinit var canvas: Canvas
    private var brushSize = 0f
    private var color = Color.BLACK
    private val paths = ArrayList<CustomPath>()

    init {
        setup()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)

        for(p in paths) {
            drawPaint.strokeWidth = p.thickness
            drawPaint.color = p.color
            canvas?.drawPath(p, drawPaint)
        }

        if(!drawPath.isEmpty) {
            drawPaint.strokeWidth = drawPath.thickness
            drawPaint.color = drawPath.color
            canvas?.drawPath(drawPath, drawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event?.x
        val touchy = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.color = color
                drawPath.thickness = brushSize

                drawPath.reset()
                if (touchx != null && touchy != null)
                    drawPath.moveTo(touchx, touchy)
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchx != null && touchy != null)
                    drawPath.lineTo(touchx, touchy)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(drawPath)
                drawPath = CustomPath(color, brushSize)
            }
            else -> return false
        }

        invalidate()

        return true
    }

    private fun setup() {
        drawPath = CustomPath(color, brushSize)

        drawPaint = Paint()
        drawPaint.color = color
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)

        setBrushSize(10f)
    }

    fun setBrushSize(size: Float) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics)
    }

    fun setBrushColor(colorString: String) {
        color = Color.parseColor(colorString)
    }

    internal inner class CustomPath(var color: Int, var thickness: Float) : Path() {}
}