package org.bbs.chuniquery.model

import com.google.gson.annotations.SerializedName

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-20
 */
class CommonUpdateModel {
    /**
     * return code
     */
    @SerializedName("code")
    var code: Int? = null

    /**
     * version string
     */
    @SerializedName("version")
    var version: String? = null

    /**
     * newest apk url
     */
    @SerializedName("url")
    var url: String? = null
}