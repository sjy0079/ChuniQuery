package org.bbs.chuniquery.utils

import android.content.Context
import androidx.fragment.app.Fragment
import org.bbs.chuniquery.MainActivity

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
/**
 * convert dip to px
 */
fun Context.dip2px(dip: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}

/**
 * calc rating by score and diff
 */
fun calcRating(score: Int, diff: Float): Float {
    return when {
        score < 800000 -> 0F
        score < 900000 -> (diff - 5) / 2 + ((diff - 5) - (diff - 5) / 2) * (score - 800000) / 100000
        score < 925000 -> diff - 5 + 2F * (score - 900000) / 25000
        score < 975000 -> diff - 3 + 3F * (score - 925000) / 50000
        score < 1000000 -> diff + 1F * (score - 975000) / 25000
        score < 1005000 -> diff + 1 + 0.5F * (score - 1000000) / 5000
        score < 1007500 -> diff + 1.5F + 0.5F * (score - 1005000) / 2500
        else -> diff + 2
    }
}

/**
 * get card id
 */
fun Fragment.getCardId(): String =
    context
        ?.getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
        ?.getString(
            MainActivity.CARD_STORED_KEY, String()
        ) ?: ""


/**
 * some color define
 */
const val CHUNI_RATING_COLOR_GREEN = 0xFF60E270.toInt()
const val CHUNI_RATING_COLOR_ORANGE = 0xFFEF8E4C.toInt()
const val CHUNI_RATING_COLOR_RED = 0xFFEF1F1F.toInt()
const val CHUNI_RATING_COLOR_PURPLE = 0xFF8136E4.toInt()
const val CHUNI_RATING_COLOR_COPPER = 0xFFBB9346.toInt()
const val CHUNI_RATING_COLOR_SILVER = 0xFFC3F8FF.toInt()
const val CHUNI_RATING_COLOR_GOLD = 0xFFFFD700.toInt()
const val CHUNI_RATING_COLOR_PLATINUM = 0xFFFBF6CA.toInt()
