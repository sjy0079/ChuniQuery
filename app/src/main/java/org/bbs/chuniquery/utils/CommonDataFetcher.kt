package org.bbs.chuniquery.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import org.bbs.chuniquery.MainActivity
import org.bbs.chuniquery.R
import org.bbs.chuniquery.network.MinimeOnlineClient
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException


/**
 * fetch game data from network from @esterion
 *
 * @author BBS
 * @since  2020/11/19
 */
class CommonDataFetcher {

    companion object {
        const val MUSIC_DB_FETCHED_SP_KEY = "chuni_music_db_fetched_sp_key"

        private val UTF8: Charset = Charset.forName("UTF-8")

        @SuppressLint("CheckResult")
        fun fetch(context: Context, callback: (Boolean) -> Unit) {
            MinimeOnlineClient
                .instance
                .getService()
                .getChuniAllSongData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val str = doJson(it).substring(16)
                    context.getSharedPreferences(
                        MainActivity::class.java.name,
                        Context.MODE_PRIVATE
                    ).edit()
                        .putString(MUSIC_DB_FETCHED_SP_KEY, str).apply()
                    CommonAssetJsonLoader.instance.loadChuniMusicDBData(str)
                    Toast.makeText(
                        context,
                        R.string.chuni_query_refresh_song_data_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    callback.invoke(true)
                }, {
                    it.printStackTrace()
                    Toast.makeText(
                        context,
                        R.string.chuni_query_refresh_song_data_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    callback.invoke(false)
                })
        }

        private fun doJson(responseBody: ResponseBody): String {
            val contentLength = responseBody.contentLength()
            val source = responseBody.source()
            try {
                source.request(Long.MAX_VALUE) // Buffer the entire body.
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val buffer: Buffer = source.buffer()
            var charset: Charset = UTF8
            val contentType: MediaType? = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)!!
                } catch (e: UnsupportedCharsetException) {
                    e.printStackTrace()
                }
            }
            var result = String()
            if (contentLength != 0L) {
                result = buffer.clone().readString(charset)
            }
            return result
        }
    }
}