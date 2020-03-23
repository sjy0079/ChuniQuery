package org.bbs.chuniquery.ongeki.model

import com.google.gson.annotations.SerializedName

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-22
 */
class OngekiSkillListModel : ArrayList<OngekiSkillBean>() {
    /**
     * get skill by id
     */
    fun getSkill(id: Int?): OngekiSkillBean? {
        if (id == null || isEmpty()) {
            return null
        }
        for (bean in this) {
            if (bean.id == id) {
                return bean
            }
        }
        return null
    }
}

/**
 * bean of the list
 */
class OngekiSkillBean {
    /**
     * id of the skill
     */
    @SerializedName("id")
    var id: Int? = null
    /**
     * skill name
     */
    @SerializedName("name")
    var name: String? = null
    /**
     * skill category
     */
    @SerializedName("category")
    var category: String? = null
    /**
     * skill detail
     */
    @SerializedName("info")
    var info: String? = null
}