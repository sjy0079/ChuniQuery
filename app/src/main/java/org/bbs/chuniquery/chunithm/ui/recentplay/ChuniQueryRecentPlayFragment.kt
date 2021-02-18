package org.bbs.chuniquery.chunithm.ui.recentplay

import android.view.View
import io.reactivex.Observable
import org.bbs.chuniquery.chunithm.model.ChuniQueryGameRecordModel
import org.bbs.chuniquery.chunithm.ui.common.ChuniQueryGameRecordFragment
import org.bbs.chuniquery.utils.CommonAssetJsonLoader
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.bbs.chuniquery.utils.calcChuniRating
import org.bbs.chuniquery.utils.getFelicaCardId

class ChuniQueryRecentPlayFragment : ChuniQueryGameRecordFragment() {

    override fun init(root: View) {
        super.init(root)
        dataFetcher = ChuniQueryRequests
            .fetchPlayLog(getFelicaCardId())
            .flatMap {
                it.sortByDescending { bean ->
                    bean.playDate
                }
                val convertList = ArrayList<ChuniQueryGameRecordModel>()
                for (bean in it) {
                    val musicDetail =
                        CommonAssetJsonLoader.instance.chuniMusicDB[bean.musicId] ?: continue
                    if (musicDetail.isWorldsEnd()) {
                        bean.ratingCalc = 0F
                    } else {
                        bean.ratingCalc = calcChuniRating(
                            bean.score?.toInt() ?: 0,
                            musicDetail.difficultyList[bean.classId?.toInt() ?: 0].toFloat() / 100
                        )
                    }
                    val recordModel = bean.convertData() ?: continue
                    convertList.add(recordModel)
                }
                Observable.just(convertList)
            }
    }
}
