package org.bbs.chuniquery.network

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-12
 */
class MinimeOnlineTransformer {
    companion object {
        /**
         * switch thread & check err code
         */
        fun handleResult(): ObservableTransformer<JsonElement, JsonElement> {
            return ObservableTransformer { upstream ->
                upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        if (it is JsonObject && it.has("code") && it["code"].toString() != "0") {
                            val msg = if (it.has("msg")) {
                                it.get("msg").toString()
                            } else {
                                String()
                            }
                            Observable.error(MinimeOnlineException(it.get("code").toString(), msg))
                        } else {
                            Observable.just(it)
                        }
                    }
            }
        }
    }
}