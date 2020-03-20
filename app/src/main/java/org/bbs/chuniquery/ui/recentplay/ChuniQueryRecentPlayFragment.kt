package org.bbs.chuniquery.ui.recentplay

import android.view.View
import io.reactivex.Observable
import org.bbs.chuniquery.model.ChuniQueryGameRecordModel
import org.bbs.chuniquery.ui.common.ChuniQueryGameRecordFragment
import org.bbs.chuniquery.utils.ChuniQueryMusicDBLoader
import org.bbs.chuniquery.utils.ChuniQueryRequests
import org.bbs.chuniquery.utils.calcRating
import org.bbs.chuniquery.utils.getCardId

class ChuniQueryRecentPlayFragment : ChuniQueryGameRecordFragment() {

    override fun init(root: View) {
        super.init(root)
        dataFetcher = ChuniQueryRequests
            .fetchPlayLog(getCardId())
            .flatMap {
                it.sortByDescending { bean ->
                    bean.playDate
                }
                val convertList = ArrayList<ChuniQueryGameRecordModel>()
                for (bean in it) {
                    val musicDetail =
                        ChuniQueryMusicDBLoader.instance.data[bean.musicId] ?: continue
                    if (musicDetail.isWorldsEnd()) {
                        bean.ratingCalc = 0F
                    } else {
                        bean.ratingCalc = calcRating(
                            bean.score?.toInt() ?: 0,
                            (musicDetail.difficultyList?.get(bean.classId?.toInt() ?: 0)
                                ?: 0).toFloat() / 100
                        )
                    }
                    val recordModel = bean.convertData() ?: continue
                    convertList.add(recordModel)
                }
                Observable.just(convertList)
            }
    }
}
