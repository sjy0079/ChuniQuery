package org.bbs.chuniquery.chunithm.model

/**
 * @author BBS
 * @since  2020-03-13
 */
class ChuniQueryGameRecordModel {
    /**
     * title of the song
     */
    var title: String = String()

    /**
     * class id of the song
     */
    var classId: Int = 0

    /**
     * rank id of the song
     */
    var rankId: Int = 0

    /**
     * score of the song
     */
    var score: Int = 0

    /**
     * official difficulty of the song
     */
    var diff: String = "0.0"

    /**
     * user rating of the song
     */
    var rating: Float = 0.0F

    /**
     * user's play date of the record
     */
    var playDate: String = String()

    /**
     * record detail
     *
     * length should be 4
     * [0] -> Justice-C
     * [1] -> Justice
     * [2] -> Attack
     * [3] -> Miss
     */
    var recordDetail: IntArray = intArrayOf()
}