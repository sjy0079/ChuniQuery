package org.bbs.chuniquery.ui.items

import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.R
import org.bbs.chuniquery.event.ChuniQueryRefreshEvent
import org.bbs.chuniquery.model.ChuniQueryItemsModel
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.utils.ChuniQueryRequests
import org.bbs.chuniquery.utils.getCardId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-15
 */
class ChuniQueryItemsFragment : Fragment() {
    companion object {
        /**
         * limited item ids for mapping
         */
        private val itemIds = listOf(
            8000, 110, 5060, 5230, 5310
        )
        /**
         * limited item list
         */
        private val itemMap = mapOf(
            Pair(
                8000,
                ItemInfo(
                    8000,
                    R.string.chuni_query_items_gold_penguin,
                    R.drawable.chuni_query_item_gold_penguin,
                    0
                )
            ),
            Pair(
                110,
                ItemInfo(
                    110,
                    R.string.chuni_query_items_chuni_net_ticket,
                    R.drawable.chuni_query_item_chuni_net,
                    0
                )
            ),
            Pair(
                5060,
                ItemInfo(
                    5060,
                    R.string.chuni_query_items_4x_map,
                    R.drawable.chuni_query_item_4x_map,
                    0
                )
            ),
            Pair(
                5230,
                ItemInfo(
                    5230,
                    R.string.chuni_query_items_6x_track,
                    R.drawable.chuni_query_item_6x_track,
                    0
                )
            ),
            Pair(
                5310,
                ItemInfo(
                    5310,
                    R.string.chuni_query_items_we,
                    R.drawable.chuni_query_item_we,
                    0
                )
            )
        )
    }

    /**
     * request canceller
     */
    private var disposable: Disposable? = null
    /**
     * main list view
     */
    private lateinit var listView: RecyclerView
    /**
     * refresher of the page
     */
    private lateinit var refresher: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.chuni_query_refresh_list_layout,
        container,
        false
    ).also {
        init(it)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        refresher.setOnRefreshListener {
            refresh()
        }
        disposable?.dispose()
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ChuniQueryRefreshEvent) {
        disposable?.dispose()
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        disposable?.dispose()
    }

    /**
     * init list view
     */
    private fun init(root: View) {
        itemMap.forEach {
            it.value.count = 0
        }
        refresher = root.findViewById(R.id.refresher)
        listView = root.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<ChuniQueryItemsViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ChuniQueryItemsViewHolder {
                    val cardView = LayoutInflater.from(context)
                        .inflate(
                            R.layout.chuni_query_item_card,
                            parent,
                            false
                        )
                    return ChuniQueryItemsViewHolder(cardView)
                }

                override fun getItemCount(): Int = itemMap.size

                override fun onBindViewHolder(holder: ChuniQueryItemsViewHolder, position: Int) {
                    val item = itemMap[itemIds[position]] ?: error("")
                    holder.icon.setImageResource(item.drawableRes)
                    holder.desc.text = getString(item.desc)
                    holder.count.text = item.count.toString()
                    holder.itemView.setOnClickListener {
                        modifyCount(getString(item.desc), item.id, item.count)
                    }
                }
            }
            visibility = if (getCardId().isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    /**
     * refresh action
     */
    private fun refresh() {
        if (getCardId().isEmpty()) {
            refresher.isRefreshing = false
            return
        }
        disposable = ChuniQueryRequests
            .fetchItems(getCardId())
            .subscribe({
                updateItemsInfo(it)
                Handler().postDelayed({ refresher.isRefreshing = false }, 300)
            }, {
                refresher.isRefreshing = false
            })
    }

    /**
     * update items list info
     */
    private fun updateItemsInfo(items: ChuniQueryItemsModel) {
        itemMap.forEach {
            it.value.count = 0
        }
        for (item in items) {
            val itemId = item.itemId ?: continue
            itemMap[itemId]?.count = item.stock ?: 0
        }
        listView.visibility = View.VISIBLE
        listView.adapter?.notifyDataSetChanged()
    }

    /**
     * modify the count of item
     */
    private fun modifyCount(name: String, id: Int, count: Int) {
        context?.let {
            MaterialDialog.Builder(it)
                .title(getString(R.string.chuni_query_items_modify_title, name))
                .inputRange(1, 4)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input(
                    null, count.toString()
                ) { _, _ -> }
                .positiveText(R.string.chuni_query_confirm)
                .autoDismiss(false)
                .onPositive { dialog, which ->
                    val modifyCount = dialog.inputEditText?.text?.toString() ?: "1"
                    if (DialogAction.POSITIVE == which) {
                        ChuniQueryRequests
                            .modifyItems(
                                getCardId(),
                                id.toString(),
                                modifyCount
                            )
                            .subscribe({
                                itemMap[id]?.count = modifyCount.toInt()
                                listView.adapter?.notifyDataSetChanged()
                                Toast.makeText(
                                    context,
                                    R.string.chuni_query_items_modify_succeed,
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }, { e ->
                                if (e is MinimeOnlineException) {
                                    Toast.makeText(context, e.errMsg, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.chuni_query_items_modify_failed,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            })
                    }
                }
                .autoDismiss(false)
                .cancelable(true)
                .show()
        }
    }

    /**
     * item info to render
     */
    private data class ItemInfo(
        /**
         * item id
         */
        var id: Int,
        /**
         * description of item
         */
        @StringRes var desc: Int,
        /**
         * item image id
         */
        @DrawableRes var drawableRes: Int = 0,
        /**
         * item count
         */
        var count: Int = 0
    )
}