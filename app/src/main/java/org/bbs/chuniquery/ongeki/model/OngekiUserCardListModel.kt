package org.bbs.chuniquery.ongeki.model

import com.google.gson.annotations.SerializedName
import org.bbs.chuniquery.utils.CommonAssetJsonLoader

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-23
 */
class OngekiUserCardListModel : ArrayList<OngekiUserCardBean>() {
    /**
     * get card by id
     */
    fun getCard(id: Int?): OngekiUserCardBean? {
        if (id == null || isEmpty()) {
            return null
        }
        for (bean in this) {
            if (bean.cardId == id) {
                return bean
            }
        }
        return null
    }
}

class OngekiUserCardBean {
    /**
     * card id
     */
    @SerializedName("card_id")
    var cardId: Int? = null
    /**
     * card stock (stars)
     */
    @SerializedName("digital_stock")
    var digitalStock: Int? = null
    /**
     * card level
     */
    @SerializedName("level")
    var level: Int? = null
    /**
     * card max level
     */
    @SerializedName("max_level")
    var maxLevel: Int? = null
    /**
     * card kaika date
     */
    @SerializedName("kaika_date")
    var kaikaDate: String? = null
    /**
     * card cho kaika date
     */
    @SerializedName("cho_kaika_date")
    var choKaikaDate: String? = null
    /**
     * card skill id
     */
    @SerializedName("skill_id")
    var skillId: Int? = null

    /**
     * kaikaData != 0000.00.00 00:00:00 means kaika
     */
    fun isKaika(): Boolean = kaikaDate != "0000-00-00T00:00:00.000Z"

    /**
     * just like kaika
     */
    fun isChoKaika(): Boolean = choKaikaDate != "0000-00-00T00:00:00.000Z"
}