package org.bbs.chuniquery.ongeki.ui.cardmaker

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.bbs.chuniquery.R
import org.bbs.chuniquery.utils.*

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-22
 */
class OngekiCardMakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * card container
     */
    val container: View = itemView.findViewById(R.id.container)
    /**
     * card bg
     */
    private val background: ImageView = itemView.findViewById(R.id.background)
    /**
     * jacket bg
     */
    private val jacket: ImageView = itemView.findViewById(R.id.jacket)
    /**
     * card image
     * size: 256 × 356
     */
    val card: ImageView = itemView.findViewById(R.id.cardImage)

    /**
     * set owned to judge is jacket shown
     */
    fun setOwned(owned: Boolean) {
        if (owned) {
            jacket.visibility = View.VISIBLE
        } else {
            jacket.visibility = View.GONE
        }
    }

    /**
     * set the card bg by rarity
     */
    fun setCardBackground(rarity: String?) {
        background.setImageResource(0)
        when (rarity) {
            "N" -> background.setBackgroundColor(ONGEKI_CARD_N_BG)
            "R" -> background.setBackgroundColor(ONGEKI_CARD_R_BG)
            "SR" -> background.setBackgroundColor(ONGEKI_CARD_SR_BG)
            "SSR" -> background.setImageResource(R.drawable.ongeki_ssr_background)
        }
    }
}