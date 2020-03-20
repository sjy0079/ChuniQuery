package org.bbs.chuniquery.ui.common

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.R
import org.bbs.chuniquery.event.ChuniQueryRefreshEvent
import org.bbs.chuniquery.model.ChuniQueryGameRecordModel
import org.bbs.chuniquery.utils.getCardId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
open class ChuniQueryGameRecordFragment : Fragment() {
    /**
     * to get render data
     */
    var dataFetcher: Observable<List<ChuniQueryGameRecordModel>>? = null
    /**
     * request canceller
     */
    private var disposable: Disposable? = null
    /**
     * is fragment init with data
     */
    private var isInit = false
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
        if (!isInit) {
            disposable?.dispose()
            refresher.isRefreshing = true
            Handler().postDelayed({ refresh() }, 500)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ChuniQueryRefreshEvent) {
        if (!isInit) {
            disposable?.dispose()
            refresher.isRefreshing = true
            Handler().postDelayed({ refresh() }, 500)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        disposable?.dispose()
    }

    /**
     * init view
     */
    open fun init(root: View) {
        refresher = root.findViewById(R.id.refresher)
        listView = root.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
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
        disposable = dataFetcher?.subscribe({
            setData(it)
            isInit = true
            Handler().postDelayed({ refresher.isRefreshing = false }, 300)
        }, {
            refresher.isRefreshing = false
        })
    }

    /**
     * set the data of the list view
     */
    private fun setData(data: List<ChuniQueryGameRecordModel>) {
        listView.adapter = object : RecyclerView.Adapter<ChuniQueryGameRecordViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ChuniQueryGameRecordViewHolder {
                val cardView = LayoutInflater.from(context)
                    .inflate(
                        R.layout.chuni_query_game_record_card,
                        parent,
                        false
                    )
                return ChuniQueryGameRecordViewHolder(cardView)
            }

            override fun getItemCount(): Int = data.size

            override fun onBindViewHolder(holder: ChuniQueryGameRecordViewHolder, position: Int) {
                val bean = data[position]
                holder.setPlacing(position)
                holder.setClass(bean.classId)
                holder.setRank(bean.rankId)
                holder.setPlayDate(bean.playDate)
                holder.setPlayDetail(bean.recordDetail)
                holder.title.text = bean.title
                holder.score.text = bean.score.toString()
                holder.difficulty.text = bean.diff
                holder.rating.setRating(bean.rating)
            }
        }
    }
}
