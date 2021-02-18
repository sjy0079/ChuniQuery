package org.bbs.chuniquery.network

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author BBS
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

        private fun getUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
            return try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts: Array<TrustManager> =
                    arrayOf(object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) = Unit

                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) = Unit

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: java.lang.Exception) {
                throw RuntimeException(e)
            }
        }
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
                getUnsafeOkHttpClientBuilder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        // interceptor: change url by header["urlName"]
                        val originalRequest: Request = chain.request()
                        val oldUrl: HttpUrl = originalRequest.url()
                        val builder: Request.Builder = originalRequest.newBuilder()
                        val urlNameList: List<String> =
                            originalRequest.headers("urlName")
                        return@addInterceptor if (urlNameList.isNotEmpty()) {
                            builder.removeHeader("urlName")
                            val baseURL: HttpUrl = HttpUrl.parse(urlNameList[0])
                                ?: return@addInterceptor chain.proceed(originalRequest)
                            val newHttpUrl = oldUrl.newBuilder()
                                .scheme(baseURL.scheme())
                                .host(baseURL.host())
                                .port(baseURL.port())
                                .build()
                            val newRequest: Request = builder.url(newHttpUrl).build()
                            chain.proceed(newRequest)
                        } else {
                            chain.proceed(originalRequest)
                        }
                    }
                    .build()
            )
            .build()
    }

    /**
     * get the service to request net interface
     */
    fun getService(): MinimeOnlineService = retrofit.create(MinimeOnlineService::class.java)
}