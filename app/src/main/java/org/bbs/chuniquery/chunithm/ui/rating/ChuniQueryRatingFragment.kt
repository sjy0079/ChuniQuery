package org.bbs.chuniquery.chunithm.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.Observable
import org.bbs.chuniquery.R
import org.bbs.chuniquery.chunithm.event.ChuniQueryRefreshEvent
import org.bbs.chuniquery.chunithm.model.ChuniQueryGameRecordModel
import org.bbs.chuniquery.chunithm.model.ChuniQueryMusicModel
import org.bbs.chuniquery.chunithm.ui.common.ChuniQueryGameRecordFragment
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.chunithm.utils.ChuniQueryMusicDBLoader
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.bbs.chuniquery.chunithm.utils.calcRating
import org.bbs.chuniquery.chunithm.utils.getCardId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChuniQueryRatingFragment : Fragment() {
    /**
     * best 30 fragment
     */
    private lateinit var best30Fragment: ChuniQueryGameRecordFragment
    /**
     * recent 10 fragment
     */
    private lateinit var recent10Fragment: ChuniQueryGameRecordFragment
    /**
     * best 30 tab
     */
    private lateinit var best30Tab: TabLayout.Tab
    /**
     * recent 10 tab
     */
    private lateinit var recent10Tab: TabLayout.Tab

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.chuni_query_fragment_rating, container, false).also {
        init(it)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ChuniQueryRefreshEvent) {
        setDataFetcher()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    /**
     * bind viewPager and tabLayout
     */
    private fun init(root: View) {
        best30Fragment = ChuniQueryGameRecordFragment()
        recent10Fragment = ChuniQueryGameRecordFragment()
        setDataFetcher()
        val fragmentList = arrayOf(best30Fragment, recent10Fragment)
        val viewPager = root.findViewById<ViewPager2>(R.id.viewPager).apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = object : FragmentStateAdapter(this@ChuniQueryRatingFragment) {

                override fun getItemCount() = fragmentList.size

                override fun createFragment(position: Int) =
                    fragmentList[position]
            }
            offscreenPageLimit = 1
        }
        val indicator = root.findViewById<TabLayout>(R.id.indicator)
        TabLayoutMediator(indicator, viewPager, true) { tab, position ->
            if (position == 0) {
                best30Tab = tab
                best30Tab.text = getString(R.string.chuni_query_rating_best_30)
            } else {
                recent10Tab = tab
                best30Tab.text = getString(R.string.chuni_query_rating_recent_10)
            }
        }.attach()
    }

    /**
     * set fetcher of data
     */
    private fun setDataFetcher() {
        best30Fragment.dataFetcher = ChuniQueryRequests
            .fetchMusic(this@ChuniQueryRatingFragment.getCardId())
            .flatMap {
                sortMusicList(it)
                val convertList = ArrayList<ChuniQueryGameRecordModel>()
                var recordCount = 0
                var rating = 0F
                for (bean in it) {
                    val recordModel = bean.convertData(true) ?: continue
                    convertList.add(recordModel)
                    rating += recordModel.rating
                    if (++recordCount == 30) {
                        break
                    }
                }
                rating /= convertList.size
                val tabText = "BEST 30 - ${ChuniQueryRatingView.formatRating(rating)}"
                best30Tab.text = tabText
                Observable.just(convertList)
            }
        recent10Fragment.dataFetcher = ChuniQueryRequests
            .fetchPlayLog(this@ChuniQueryRatingFragment.getCardId())
            .flatMap {
                it.sortByDescending { bean ->
                    bean.playDate
                }
                val recentList = getRecentMusicList(it)
                sortMusicList(recentList)
                val convertList = ArrayList<ChuniQueryGameRecordModel>()
                var recordCount = 0
                var rating = 0F
                for (bean in recentList) {
                    val recordModel = bean.convertData(true) ?: continue
                    convertList.add(recordModel)
                    rating += recordModel.rating
                    if (++recordCount == 10) {
                        break
                    }
                }
                rating /= convertList.size
                val tabText = "RECENT 10 - ${ChuniQueryRatingView.formatRating(rating)}"
                recent10Tab.text = tabText
                Observable.just(convertList)
            }
    }

    /**
     * cause chunithm recent rating calc rule:
     *
     * 1. WORLD'S END songs will be out of the list
     *
     * 2. if you get an SSS, but get a low rating than other 29 songs (like your are in rating 15.00
     *    and you're getting the LEVEL 11 AJ medal), this record will be out of the list
     *
     * I just get an unique way (well, of course it's a slack work too :) to achieve this algorithm
     */
    private fun getRecentMusicList(list: ChuniQueryMusicModel): ChuniQueryMusicModel {
        val resultList = ChuniQueryMusicModel()
        var count = 30
        for (bean in list) {
            if (arrayOf("0", "1", "2", "3").indexOf(bean.classId ?: "-1") == -1) {
                continue
            }
            resultList.add(bean)
            if ((bean.score ?: "").toInt() < 1007500) {
                count--
            }
            if (count == 0) {
                break
            }
        }
        return resultList
    }

    /**
     * calc the rating and sort
     */
    private fun sortMusicList(list: ChuniQueryMusicModel) {
        list.forEach { bean ->
            val musicDetail =
                ChuniQueryMusicDBLoader.instance.data[bean.musicId] ?: return@forEach
            if (musicDetail.isWorldsEnd()) {
                bean.ratingCalc = 0F
            } else {
                bean.ratingCalc = calcRating(
                    bean.score?.toInt() ?: 0,
                    (musicDetail.difficultyList?.get(bean.classId?.toInt() ?: 0)
                        ?: 0).toFloat() / 100
                )
            }
        }
        list.sortByDescending { bean ->
            bean.ratingCalc
        }
    }
}