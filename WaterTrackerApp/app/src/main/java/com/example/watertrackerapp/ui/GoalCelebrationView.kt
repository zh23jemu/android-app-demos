package com.example.watertrackerapp.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GoalCelebrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        val angle: Double,
        val distance: Float,
        val speed: Float,
        val size: Float,
        val color: Int,
        val drift: Float
    )

    private val particles = mutableListOf<Particle>()
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.argb(180, 255, 255, 255)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 56f
        isFakeBoldText = true
    }

    private var progress = 0f
    private var animator: ValueAnimator? = null

    init {
        alpha = 0f
        visibility = GONE
    }

    fun play() {
        if (width == 0 || height == 0) {
            post { play() }
            return
        }
        generateParticles()
        visibility = VISIBLE
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                alpha = when {
                    progress < 0.1f -> progress / 0.1f
                    progress > 0.82f -> (1f - progress) / 0.18f
                    else -> 1f
                }.coerceIn(0f, 1f)
                invalidate()
            }
            doOnEnd {
                visibility = GONE
                alpha = 0f
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (visibility != VISIBLE) return

        val centerX = width / 2f
        val centerY = height * 0.34f
        val ringRadius = 80f + 180f * progress
        canvas.drawCircle(centerX, centerY, ringRadius, glowPaint)

        particles.forEach { particle ->
            val travel = particle.distance * progress * particle.speed
            val x = centerX + (cos(particle.angle) * travel).toFloat()
            val y = centerY + (sin(particle.angle) * travel).toFloat() + particle.drift * progress
            particlePaint.color = particle.color
            canvas.drawCircle(x, y, particle.size * (1.15f - progress * 0.7f), particlePaint)
        }

        canvas.drawText("今日达标!", centerX, centerY + 18f, textPaint)
    }

    private fun generateParticles() {
        particles.clear()
        repeat(44) {
            particles += Particle(
                angle = Random.nextDouble(0.0, Math.PI * 2),
                distance = Random.nextInt(180, 420).toFloat(),
                speed = Random.nextDouble(0.75, 1.3).toFloat(),
                size = Random.nextInt(8, 20).toFloat(),
                color = COLORS.random(),
                drift = Random.nextInt(-120, 180).toFloat()
            )
        }
    }

    private fun ValueAnimator.doOnEnd(action: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) = Unit
            override fun onAnimationEnd(animation: android.animation.Animator) = action()
            override fun onAnimationCancel(animation: android.animation.Animator) = action()
            override fun onAnimationRepeat(animation: android.animation.Animator) = Unit
        })
    }

    companion object {
        private val COLORS = listOf(
            Color.parseColor("#7CF7FF"),
            Color.parseColor("#B794FF"),
            Color.parseColor("#FFE082"),
            Color.parseColor("#80FFB4"),
            Color.parseColor("#FF8DA1"),
            Color.parseColor("#FFFFFF")
        )
    }
}
