package org.bbs.chuniquery.chunithm.model

import com.google.gson.annotations.SerializedName


/**
 * @author BBS
 * @since  2020-03-12
 */
class ChuniQueryItemsModel : ArrayList<ChuniQueryItemsBean>()

/**
 * bean of the model
 */
class ChuniQueryItemsBean {
    /**
     * item's kind, always 5
     */
    @SerializedName("item_kind")
    var itemKind: Int? = null

    /**
     * item id, id + kind = one unique item
     */
    @SerializedName("item_id")
    var itemId: Int? = null

    /**
     * stock of the item
     */
    @SerializedName("stock")
    var stock: Int? = null

    /**
     * is valid, should be true
     */
    @SerializedName("is_valid")
    var isValid: Boolean? = null
}
