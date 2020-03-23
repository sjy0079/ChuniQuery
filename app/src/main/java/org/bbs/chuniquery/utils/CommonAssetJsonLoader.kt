package org.bbs.chuniquery.utils

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.bbs.chuniquery.chunithm.model.ChuniMusicDBModel
import org.bbs.chuniquery.ongeki.model.OngekiCardListModel
import org.bbs.chuniquery.ongeki.model.OngekiSkillListModel
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
class CommonAssetJsonLoader private constructor() {

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CommonAssetJsonLoader()
        }
    }

    /**
     * chunithm music db model
     */
    lateinit var chuniMusicDB: ChuniMusicDBModel
    /**
     * ongeki cards model
     */
    lateinit var ongekiCards: OngekiCardListModel
    /**
     * ongeki cards model
     */
    lateinit var ongekiSkills: OngekiSkillListModel
    /**
     * canceller
     */
    private var disposable: Disposable? = null

    /**
     * load chunithm music db data from assets
     */
    fun loadChuniMusicDBData(context: Context) {
        disposable = Observable.just("chuni_music_db.json")
            .map {
                var json = String()
                try {
                    val input = context.assets.open(it)
                    json = convertStreamToString(input)
                } catch (ignore: Exception) {
                }
                json
            }
            .map {
                Gson().fromJson(it, ChuniMusicDBModel::class.java)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                chuniMusicDB = it
            }
    }

    /**
     * load ongeki card list data from assets
     */
    fun loadOngekiCardListData(context: Context) {
        disposable = Observable.just("ongeki_card_list.json")
            .map {
                var json = String()
                try {
                    val input = context.assets.open(it)
                    json = convertStreamToString(input)
                } catch (ignore: Exception) {
                }
                json
            }
            .map {
                Gson().fromJson(it, OngekiCardListModel::class.java)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ongekiCards = it
            }
    }

    /**
     * load ongeki skill list data from assets
     */
    fun loadOngekiSkillListData(context: Context) {
        disposable = Observable.just("ongeki_skill_list.json")
            .map {
                var json = String()
                try {
                    val input = context.assets.open(it)
                    json = convertStreamToString(input)
                } catch (ignore: Exception) {
                }
                json
            }
            .map {
                Gson().fromJson(it, OngekiSkillListModel::class.java)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ongekiSkills = it
            }
    }

    /**
     * convert byte stream to string
     */
    private fun convertStreamToString(input: InputStream): String {
        var s = String()
        try {
            val scanner = Scanner(input, "UTF-8").useDelimiter("\\A")
            if (scanner.hasNext()) {
                s = scanner.next()
            }
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return s
    }

}