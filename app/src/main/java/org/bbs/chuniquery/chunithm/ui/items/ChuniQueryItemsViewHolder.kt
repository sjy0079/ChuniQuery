package org.bbs.chuniquery.chunithm.ui.items

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bbs.chuniquery.R

/**
 * @author BBS
 * @since  2020-03-15
 */
class ChuniQueryItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * icon of the item
     */
    val icon: ImageView = itemView.findViewById(R.id.icon)
    /**
     * description of the item
     */
    val desc: TextView = itemView.findViewById(R.id.desc)
    /**
     * count of the item
     */
    val count: TextView = itemView.findViewById(R.id.count)
}