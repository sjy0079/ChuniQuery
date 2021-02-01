package org.bbs.chuniquery.chunithm.model

import com.google.gson.annotations.SerializedName
import org.bbs.chuniquery.utils.CommonAssetJsonLoader


/**
 * @author BBS
 * @since  2020-03-12
 */
class ChuniQueryMusicModel : ArrayList<ChuniQueryMusicBean>()

/**
 * bean of the model
 */
class ChuniQueryMusicBean {
    /**
     * music id, for mapping the detail of song
     */
    @SerializedName("music_id")
    var musicId: String? = null

    /**
     * class id
     */
    @SerializedName("level")
    var classId: String? = null

    /**
     * rank id
     */
    @SerializedName("score_rank")
    var rankId: String? = null
        get() = field ?: rankIdPlayLog

    /**
     * score of the record
     */
    @SerializedName("score_max")
    var score: String? = null
        get() = field ?: scorePlayLog

    // flowing field used in cm_user_playlog

    /**
     * score in playlog
     */
    @SerializedName("score")
    var scorePlayLog: String? = null

    /**
     * rank id in playlog
     */
    @SerializedName("rank")
    var rankIdPlayLog: String? = null

    /**
     * record date
     */
    @SerializedName("user_play_date")
    var playDate: String? = null
        get() = field?.replace("T", " ")?.substring(
            0, field!!.length - 5
        )

    /**
     * justice-c count
     */
    @SerializedName("judge_critical")
    var justiceCritical: String? = null

    /**
     * justice count
     */
    @SerializedName("judge_justice")
    var justice: String? = null

    /**
     * attack count
     */
    @SerializedName("judge_attack")
    var attack: String? = null

    /**
     * miss count
     */
    @SerializedName("judge_guilty")
    var miss: String? = null

    /**
     * user's rating, calc it myself
     */
    var ratingCalc: Float? = null

    /**
     * convert raw data to readable data for game record fragment
     */
    fun convertData(skipWorldSEnd: Boolean = false): ChuniQueryGameRecordModel? {
        val musicDetail =
            CommonAssetJsonLoader.instance.chuniMusicDB[this.musicId] ?: return null
        if (musicDetail.isWorldsEnd() && skipWorldSEnd) {
            return null
        }
        return ChuniQueryGameRecordModel().apply {
            title = musicDetail.name ?: String()
            classId = this@ChuniQueryMusicBean.classId?.toInt() ?: 0
            rankId = this@ChuniQueryMusicBean.rankId?.toInt() ?: 0
            score = this@ChuniQueryMusicBean.score?.toInt() ?: 0
            diff = if (musicDetail.isWorldsEnd()) {
                "0.0"
            } else {
                ((musicDetail.difficultyList?.get(this@ChuniQueryMusicBean.classId?.toInt() ?: 0)
                    ?: 0).toFloat() / 100).toString()
            }
            rating = this@ChuniQueryMusicBean.ratingCalc ?: 0F
            if (!this@ChuniQueryMusicBean.playDate.isNullOrBlank()) {
                playDate = this@ChuniQueryMusicBean.playDate!!
            }
            if (!this@ChuniQueryMusicBean.justiceCritical.isNullOrBlank() &&
                !this@ChuniQueryMusicBean.justice.isNullOrBlank() &&
                !this@ChuniQueryMusicBean.attack.isNullOrBlank() &&
                !this@ChuniQueryMusicBean.miss.isNullOrBlank()
            ) {
                recordDetail = intArrayOf(
                    this@ChuniQueryMusicBean.justiceCritical!!.toInt(),
                    this@ChuniQueryMusicBean.justice!!.toInt(),
                    this@ChuniQueryMusicBean.attack!!.toInt(),
                    this@ChuniQueryMusicBean.miss!!.toInt()
                )
            }
        }
    }
}
