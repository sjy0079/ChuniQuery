package org.bbs.chuniquery.network

import android.content.Context
import okhttp3.OkHttpClient
import org.bbs.chuniquery.MainActivity
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-12
 */
class MinimeOnlineClient private constructor() {

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MinimeOnlineClient()
        }
        /**
         * url to tencent cloud
         * port 3000 for minime-support
         */
        const val BASE_URL = "http://123.57.246.220:3000"
    }

    /**
     * the only retrofit object
     */
    private lateinit var retrofit: Retrofit

    /**
     * should init manually
     */
    fun init(customIp: String?) {
        val url = if (customIp.isNullOrBlank()) {
            BASE_URL
        } else {
            customIp
        }
        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(url)
            .client(
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
            )
            .build()
    }

    /**
     * get the service to request net interface
     */
    fun getService(): MinimeOnlineService = retrofit.create(MinimeOnlineService::class.java)
}