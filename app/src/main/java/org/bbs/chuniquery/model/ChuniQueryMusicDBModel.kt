package org.bbs.chuniquery.model

import com.google.gson.annotations.SerializedName

/**
 * music db model
 * k: id of the music
 * v: music detail
 *
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
class ChuniMusicDBModel : HashMap<String, ChuniMusicBean>()

class ChuniMusicBean {
    /**
     * music name
     */
    @SerializedName("music_name")
    var name: String? = null

    /**
     * artist of the music
     */
    @SerializedName("artist")
    var artist: String? = null

    /**
     * real official internal difficulty list
     */
    @SerializedName("difficulty")
    var difficultyList: IntArray? = null

    /**
     * world's end songs will not come to rating calc
     */
    fun isWorldsEnd(): Boolean = difficultyList == null || difficultyList!!.isEmpty()
}