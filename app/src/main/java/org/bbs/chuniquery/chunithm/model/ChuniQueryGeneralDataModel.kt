package org.bbs.chuniquery.chunithm.model

/**
 * @author BBS
 * @since  2020-03-12
 */
class ChuniQueryGeneralDataModel : ArrayList<ChuniQueryGeneralDataBean>()

/**
 * bean of the model, simple key-value pair
 */
class ChuniQueryGeneralDataBean {
    var key: String? = null
    var value: String? = null
}
