package org.bbs.chuniquery.chunithm.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.size
import com.google.android.material.card.MaterialCardView
import org.bbs.chuniquery.R

/**
 * @author BBS
 * @since  2020-03-13
 */
class ChuniQueryColumnView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attr, defStyle) {
    /**
     * key of column
     */
    private var keyView: TextView
    /**
     * value of column
     */
    private var valueView: TextView
    /**
     * when value is rating
     */
    private var ratingView: ChuniQueryRatingView

    init {
        View.inflate(context, R.layout.chuni_query_column_layout, this)
        setCardBackgroundColor(0xFF212121.toInt())
        elevation = 0F

        keyView = findViewById(R.id.keyView)
        valueView = findViewById(R.id.valueView)
        ratingView = findViewById(R.id.ratingView)
    }

    /**
     * set text size in sp
     */
    fun setTextSize(size: Float) {
        keyView.textSize = size
        valueView.textSize = size
        ratingView.textSize = size
    }

    /**
     * set text selectable
     */
    fun setValueSelectable(selectable: Boolean) {
        valueView.setTextIsSelectable(selectable)
    }

    /**
     * set column data
     */
    fun setKV(key: CharSequence, value: CharSequence, isRating: Boolean = false) {
        keyView.text = key
        if (isRating) {
            valueView.visibility = View.GONE
            ratingView.visibility = View.VISIBLE
            ratingView.setRating(value.toString().toFloat())
        } else {
            ratingView.visibility = View.GONE
            valueView.visibility = View.VISIBLE
            valueView.text = value
        }
    }
}

class AutoMarqueeTextView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : TextView(context, attr, defStyle) {
    override fun isFocused() = true
}