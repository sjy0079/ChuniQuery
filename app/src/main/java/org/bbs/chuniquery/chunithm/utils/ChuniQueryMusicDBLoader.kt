package org.bbs.chuniquery.chunithm.utils

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.bbs.chuniquery.chunithm.model.ChuniMusicDBModel
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
class ChuniQueryMusicDBLoader private constructor() {

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ChuniQueryMusicDBLoader()
        }
    }

    /**
     * music db
     */
    lateinit var data: ChuniMusicDBModel
    /**
     * canceller
     */
    private var disposable: Disposable? = null

    /**
     * load data from assets
     */
    fun loadData(context: Context) {
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
                data = it
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