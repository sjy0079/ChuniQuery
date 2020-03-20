package org.bbs.chuniquery.ui.common

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bbs.chuniquery.R
import org.bbs.chuniquery.ui.widgets.ChuniQueryColumnView
import org.bbs.chuniquery.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.utils.*

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-15
 */
@SuppressLint("SetTextI18n")
class ChuniQueryGameRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * icon, show on top 3 record
     */
    private val topIcon: ImageView = itemView.findViewById(R.id.topIcon)
    /**
     * text, show on other record
     */
    private val topText: TextView = itemView.findViewById(R.id.topText)
    /**
     * song's title
     */
    val title: TextView = itemView.findViewById(R.id.title)
    /**
     * the name of class (master, expert, advance, basic)
     */
    private val className: TextView = itemView.findViewById(R.id.className)
    /**
     * rank (A-, A, AAA, S, SS, SSS)
     */
    private val rank: TextView = itemView.findViewById(R.id.rank)
    /**
     * score of the song
     */
    val score: TextView = itemView.findViewById(R.id.score)
    /**
     * official difficulty of the song
     */
    val difficulty: TextView = itemView.findViewById(R.id.difficulty)
    /**
     * rating of the song
     */
    val rating: ChuniQueryRatingView = itemView.findViewById(R.id.rating)
    /**
     * title: date
     */
    private val dateTitle: TextView = itemView.findViewById(R.id.dateTitle)
    /**
     * perform time of the song
     */
    private val date: TextView = itemView.findViewById(R.id.date)
    /**
     * detail of the song
     */
    private val detailContainer: LinearLayout =
        itemView.findViewById(R.id.detailContainer)

    /**
     * set place of the record
     */
    fun setPlacing(placing: Int) {
        when (placing) {
            0 -> {
                topIcon.apply {
                    setColorFilter(CHUNI_RATING_COLOR_GOLD)
                    visibility = View.VISIBLE
                }
                topText.visibility = View.INVISIBLE
            }
            1 -> {
                topIcon.apply {
                    setColorFilter(CHUNI_RATING_COLOR_SILVER)
                    visibility = View.VISIBLE
                }
                topText.visibility = View.INVISIBLE
            }
            2 -> {
                topIcon.apply {
                    setColorFilter(CHUNI_RATING_COLOR_COPPER)
                    visibility = View.VISIBLE
                }
                topText.visibility = View.INVISIBLE
            }
            else -> {
                topText.apply {
                    text = "NO." + (placing + 1).toString()
                    visibility = View.VISIBLE
                }
                topIcon.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * set class name by id
     *
     * 0: BASIC
     * 1: ADVANCE
     * 2: EXPERT
     * 3: MASTER
     * 4: WORLD'S END
     */
    fun setClass(classId: Int) {
        val bg = className.background as GradientDrawable
        when (classId) {
            0 -> {
                className.text = "BASIC"
                bg.setColor(CHUNI_RATING_COLOR_GREEN)
            }
            1 -> {
                className.text = "ADVANCE"
                bg.setColor(CHUNI_RATING_COLOR_ORANGE)
            }
            2 -> {
                className.text = "EXPERT"
                bg.setColor(CHUNI_RATING_COLOR_RED)
            }
            3 -> {
                className.text = "MASTER"
                bg.setColor(CHUNI_RATING_COLOR_PURPLE)
            }
            else -> {
                className.text = "WORLD'S END"
                bg.setColor(Color.BLACK)
            }
        }
    }

    /**
     * set rank name by id
     *
     * ~4: A-
     * 5: A
     * 6: AA
     * 7: AAA
     * 8: S
     * 9: SS
     * 10: SSS
     */
    fun setRank(rankId: Int) {
        val bg = rank.background as GradientDrawable
        when (rankId) {
            5 -> {
                rank.text = "A"
                bg.setColor(CHUNI_RATING_COLOR_GOLD)
            }
            6 -> {
                rank.text = "AA"
                bg.setColor(CHUNI_RATING_COLOR_GOLD)
            }
            7 -> {
                rank.text = "AAA"
                bg.setColor(CHUNI_RATING_COLOR_GOLD)
            }
            8 -> {
                rank.text = "S"
                bg.setColor(CHUNI_RATING_COLOR_PLATINUM)
            }
            9 -> {
                rank.text = "SS"
                bg.setColor(CHUNI_RATING_COLOR_PLATINUM)
            }
            10 -> {
                rank.text = "SSS"
                bg.setColor(CHUNI_RATING_COLOR_PLATINUM)
            }
            else -> {
                rank.text = "A-"
                bg.setColor(CHUNI_RATING_COLOR_SILVER)
            }
        }
    }

    /**
     * set play date
     */
    fun setPlayDate(dateStr: String) {
        if (dateStr.isEmpty()) {
            dateTitle.visibility = View.GONE
            date.visibility = View.GONE
        } else {
            dateTitle.visibility = View.VISIBLE
            date.apply {
                text = dateStr
                visibility = View.VISIBLE
            }
        }
    }

    /**
     * set play detail
     */
    fun setPlayDetail(detail: IntArray) {
        detailContainer.removeAllViews()
        if (detail.isEmpty() || detail.size != 4) {
            return
        }
        addColumn("JUSTICE-C", detail[0].toString())
        addColumn("JUSTICE", detail[1].toString())
        addColumn("ATTACK", detail[2].toString())
        addColumn("MISS", detail[3].toString())
    }

    /**
     * add column view
     */
    private fun addColumn(key: String, value: String?) {
        detailContainer.addView(
            ChuniQueryColumnView(detailContainer.context).apply {
                setTextSize(11F)
                setKV(key, value ?: String(), false)
                if (detailContainer.childCount % 2 == 1) {
                    setCardBackgroundColor(0x22212121)
                }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                detailContainer.context.dip2px(18F)
            )
        )
    }
}