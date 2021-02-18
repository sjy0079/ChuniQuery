package org.bbs.chuniquery

import android.app.Application
import android.content.Context
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.utils.CommonAssetJsonLoader

/**
 * main application of app
 * do some loading work
 *
 * @author BBS
 * @since  2021/2/18
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CommonAssetJsonLoader.instance.loadChuniMusicDBData(this)
        CommonAssetJsonLoader.instance.loadOngekiCardListData(this)
        CommonAssetJsonLoader.instance.loadOngekiSkillListData(this)

        MinimeOnlineClient.instance.init(
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                .getString(MainActivity.IP_STORED_KEY, "")
        )
    }
}