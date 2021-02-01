package org.bbs.chuniquery.ongeki.model

import com.google.gson.annotations.SerializedName

/**
 * @author BBS
 * @since  2020-03-22
 */
class OngekiCardListModel : ArrayList<OngekiCardBean>()

/**
 * bean of the list
 */
class OngekiCardBean {
    /**
     * id of the card
     *
     * cards with id 1-3 are boss card id, not shown
     */
    @SerializedName("id")
    var id: Int? = null
    /**
     * card name
     */
    @SerializedName("name")
    var name: String? = null
    /**
     * card nick name
     */
    @SerializedName("nickName")
    var nickName: String? = null
    /**
     * card attribute
     */
    @SerializedName("attribute")
    var attribute: String? = null
    /**
     * card character id
     * mean this card for which character
     */
    @SerializedName("charaId")
    var charaId: Int? = null
    /**
     * card character school name
     */
    @SerializedName("school")
    var school: String? = null
    /**
     * card character school year
     */
    @SerializedName("gakunen")
    var gakunen: String? = null
    /**
     * card rarity
     */
    @SerializedName("rarity")
    var rarity: String? = null
    /**
     * card level params (?)
     */
    @SerializedName("levelParam")
    var levelParam: String? = null
    /**
     * card character skill id
     */
    @SerializedName("skillId")
    var skillId: Int? = null
    /**
     * card character over limit ("kaika") skill id
     */
    @SerializedName("choKaikaSkillId")
    var choKaikaSkillId: Int? = null
    /**
     * card number
     */
    @SerializedName("cardNumber")
    var cardNumber: String? = null
    /**
     * card added game version
     */
    @SerializedName("version")
    var version: String? = null
}