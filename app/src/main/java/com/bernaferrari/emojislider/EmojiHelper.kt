package com.bernaferrari.emojislider

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.Choreographer
import android.view.Choreographer.FrameCallback

class EmojiHelper(context: Context) : Drawable(), FrameCallback {
    private val particleMinSize: Int
    private val particleMaxSize: Int
    private val particleAnchorOffset: Int
    private val trackingList = mutableListOf<Tracking>()
    private val pendingList = mutableListOf<Tracking>()
    private val rect = Rect()
    private val textpaint = TextPaint(1)
    var emoji = "😍"
    private var paddingLeft: Float = 0f
    private var paddingTop: Float = 0f
    private var emojiSize: Float = 0f
    private var isTracking: Boolean = false
    private var previousTime: Long = 0
    private var tracking: Tracking? = null
    private var direction: Direction = Direction.UP

    enum class Direction {
        UP, DOWN
    }

    init {
        val resources = context.resources
        this.particleAnchorOffset =
                resources.getDimensionPixelSize(R.dimen.slider_particle_system_anchor_offset)
        this.particleMinSize =
                resources.getDimensionPixelSize(R.dimen.slider_particle_system_particle_min_size)
        this.particleMaxSize =
                resources.getDimensionPixelSize(R.dimen.slider_particle_system_particle_max_size)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    fun progressStarted() {
        this.tracking = Tracking(this.emoji)
        this.tracking!!.paddingLeft = this.paddingLeft
        this.tracking!!.paddingTop = this.paddingTop
        this.tracking!!.emojiSize = this.emojiSize
        if (!this.isTracking) {
            this.isTracking = true
            doFrame(System.currentTimeMillis())
        }
    }

    fun updateProgress(percent: Float) {
        emojiSize = particleMinSize + percent * (particleMaxSize - particleMinSize)
        tracking?.emojiSize = emojiSize
        invalidateSelf()
    }

    fun onProgressChanged(paddingLeft: Float, paddingTop: Float) {
        this.paddingLeft = paddingLeft
        this.paddingTop = paddingTop
        if (tracking != null) {
            tracking!!.paddingLeft = this.paddingLeft
            tracking!!.paddingTop = this.paddingTop
        }
        invalidateSelf()
    }

    private fun m9941a(canvas: Canvas, Tracking: Tracking) {
        textpaint.textSize = Tracking.emojiSize
        textpaint.getTextBounds(
            Tracking.mainEmoji,
            0,
            Tracking.mainEmoji.length,
            rect
        )

        canvas.drawText(
            Tracking.mainEmoji,
            Tracking.paddingLeft - rect.width() / 2.0f,
            Tracking.paddingTop + Tracking.breathing - rect.height() / 2.0f,
            textpaint
        )
    }

    fun onStopTrackingTouch() {
        trackingList.add(0, tracking!!)
        tracking = null
    }

    private fun Double.toRadians() = Math.toRadians(this)

    private fun Double.toSin() = Math.sin(this)

    private fun Long.toDoubleRadiansSin() = this.toDouble().toRadians().toSin()

    override fun doFrame(j: Long) {

        tracking?.breathing =
                ((System.currentTimeMillis() / 8).toDoubleRadiansSin() * 16.0 - particleAnchorOffset).toFloat()

        val currentTimeMillis = System.currentTimeMillis()
        if (previousTime != 0L) {
            val f = (currentTimeMillis - previousTime) / 1000.0f
            for (i in trackingList.indices) {
                trackingList[i].let {
                    it.dismissPadding += 1000f * f

                    when (direction) {
                        Direction.UP -> it.paddingTop -= it.dismissPadding * f
                        Direction.DOWN -> it.paddingTop += it.dismissPadding * f
                    }

                    if (it.paddingTop < bounds.top - 2f * it.emojiSize || it.emojiSize < 0) {
                        pendingList.add(it)
                    }
                }
            }
            if (!pendingList.isEmpty()) {
                trackingList.removeAll(pendingList)
                pendingList.clear()
            }
        }

        previousTime = currentTimeMillis
        if (tracking == null && trackingList.isEmpty()) {
            isTracking = false
        } else {
            Choreographer.getInstance().postFrameCallback(this)
        }
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (this.tracking != null) {
            m9941a(canvas, this.tracking!!)
        }
        for (i in this.trackingList.indices) {
            m9941a(canvas, this.trackingList[i])
        }
    }

    override fun setAlpha(i: Int) {
        this.textpaint.alpha = i
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        this.textpaint.colorFilter = colorFilter
        invalidateSelf()
    }

    class Tracking(val mainEmoji: String) {
        var paddingLeft: Float = 0f
        var paddingTop: Float = 0f
        var breathing: Float = 0f
        var emojiSize: Float = 0f
        var dismissPadding: Float = 0f
    }

}
