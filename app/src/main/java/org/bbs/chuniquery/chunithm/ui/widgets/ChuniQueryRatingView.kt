package org.bbs.chuniquery.chunithm.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.TextView
import org.bbs.chuniquery.utils.*


/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-11
 */
class ChuniQueryRatingView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : TextView(context, attr, defStyle) {
    companion object {
        /**
         * parse rating from raw text
         */
        fun formatRating(rating: String?): String {
            val ratingFloat = Integer.parseInt(rating ?: "0").toFloat() / 100
            return formatRating(ratingFloat)
        }

        /**
         * parse rating
         */
        fun formatRating(rating: Float): String {
            val ratingStr = rating.toString()
            return when (val dotIndex = ratingStr.indexOf('.')) {
                -1 -> "${ratingStr}00"
                ratingStr.length - 2 -> "${ratingStr}0"
                else -> ratingStr.substring(0, dotIndex + 3)
            }
        }
    }

    /**
     * over 15.00, congratulation!
     */
    private var isRainbowRating = false
    /**
     * bounds of text, useful in [onDraw]
     */
    private val textBound = Rect()

    /**
     * set the rating number, it will change color itself
     */
    fun setRating(rating: Float) {
        val ratingStr = formatRating(rating)
        text = ratingStr
        isRainbowRating = false
        when {
            rating < 4F -> setTextColor(CHUNI_RATING_COLOR_GREEN)
            rating < 7F -> setTextColor(CHUNI_RATING_COLOR_ORANGE)
            rating < 10F -> setTextColor(CHUNI_RATING_COLOR_RED)
            rating < 12F -> setTextColor(CHUNI_RATING_COLOR_PURPLE)
            rating < 13F -> setTextColor(CHUNI_RATING_COLOR_COPPER)
            rating < 14F -> setTextColor(CHUNI_RATING_COLOR_SILVER)
            rating < 14.5F -> setTextColor(CHUNI_RATING_COLOR_GOLD)
            rating < 15F -> setTextColor(CHUNI_RATING_COLOR_PLATINUM)
            else -> {
                isRainbowRating = true
            }
        }
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        if (!isRainbowRating) {
            paint.shader = null
            super.onDraw(canvas)
            return
        }
        val text = text.toString()
        val rainbowGradient = LinearGradient(
            0F,
            0F,
            0F,
            measuredHeight.toFloat(),
            intArrayOf(0xFFEB8883.toInt(), 0xFFF4E665.toInt(), 0xFF31C9AD.toInt()),
            null,
            Shader.TileMode.REPEAT
        )
        paint.getTextBounds(text, 0, text.length, textBound)
        paint.shader = rainbowGradient
        super.onDraw(canvas)
    }
}