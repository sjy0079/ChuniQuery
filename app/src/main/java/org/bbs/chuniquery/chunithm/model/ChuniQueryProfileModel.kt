package org.bbs.chuniquery.chunithm.model

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import java.math.BigInteger


/**
 * @author BBS
 * @since  2020-03-12
 */
class ChuniQueryProfileModel : ArrayList<ChuniQueryProfileBean>()

/**
 * bean of the model
 */
class ChuniQueryProfileBean {
    /**
     * user's name
     */
    @SerializedName("user_name")
    var userName: String? = null

    /**
     * user's rating
     */
    @SerializedName("player_rating")
    var userRating: String? = null

    /**
     * user's level
     */
    @SerializedName("level")
    var userLevel: String? = null

    /**
     * user's highest rating
     */
    @SerializedName("highest_rating")
    var userBestRating: String? = null

    /**
     * user's point (in Amazon, it's MEAT)
     */
    @SerializedName("point")
    var userPoint: String? = null

    /**
     * total play count (1 credit 1 count)
     */
    @SerializedName("play_count")
    var userPlayCount: String? = null

    /**
     * first play time
     */
    @SerializedName("first_play_date")
    var userFirstPlayDate: String? = null
        get() = field?.replace("T", " ")?.substring(
            0, field!!.length - 5
        )

    /**
     * last play time
     */
    @SerializedName("last_play_date")
    var userLastPlayDate: String? = null
        get() = field?.replace("T", " ")?.substring(
            0, field!!.length - 5
        )

    /**
     * access code (card id in decimal)
     */
    @SerializedName("access_code")
    var userAccessCode: String? = null
        @SuppressLint("DefaultLocale")
        get() {
            var value = BigInteger(field ?: "0").toString(16)
            while (value.length < 16) {
                value = "0$value"
            }
            return value.toUpperCase()
        }
}
