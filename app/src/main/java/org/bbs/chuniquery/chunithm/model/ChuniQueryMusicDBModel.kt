package org.bbs.chuniquery.chunithm.model

import com.google.gson.annotations.SerializedName

/**
 * music db model
 * k: id of the music
 * v: music detail
 *
 * @author BBS
 * @since  2020-03-13
 */
class ChuniMusicDBModel : HashMap<String, ChuniMusicBean>()

class ChuniMusicBean : ArrayList<Any>() {
    /**
     * music name
     */
    val name: String
        get() {
            return this[0].toString()
        }

    /**
     * artist of the music
     */
    val artist: String
        get() {
            return this[1].toString()
        }

    /**
     * real official internal difficulty list
     */
    val difficultyList: ArrayList<Int>
        get() {
            if (this[2] is ArrayList<*>) {
                return if ((this[2] as ArrayList<*>).isEmpty()) {
                    arrayListOf()
                } else {
                    @Suppress("UNCHECKED_CAST")
                    this[2] as ArrayList<Int>
                }
            }
            return arrayListOf()
        }

    /**
     * world's end songs will not come to rating calc
     */
    fun isWorldsEnd(): Boolean = difficultyList.isEmpty()
}