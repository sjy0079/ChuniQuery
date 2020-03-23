package org.bbs.chuniquery.ongeki.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.reactivex.Observable
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.network.MinimeOnlineTransformer
import org.bbs.chuniquery.ongeki.model.OngekiUserCardListModel

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-23
 */
object OngekiRequests {
    /**
     * fetch user info, input card number in aime.txt
     */
    fun fetchProfile(cardNumber: String): Observable<JsonElement> =
        MinimeOnlineClient
            .instance
            .getService()
            .getOngekiPlayerInfo(cardNumber)
            .compose(MinimeOnlineTransformer.handleResult())

    /**
     * fetch user card info
     */
    fun fetchUserCardList(cardNumber: String): Observable<OngekiUserCardListModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getOngekiUserCardInfo(cardNumber)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, OngekiUserCardListModel::class.java)
                if (model.isEmpty()) {
                    Observable.error(MinimeOnlineException.createNoDataException())
                } else {
                    Observable.just(model)
                }
            }

    /**
     * get character card of ongeki
     */
    fun getCard(cardNumber: String, ongekiCardId: String, addNumber: Int): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .getOngekiCard(cardNumber, ongekiCardId, addNumber)
            .compose(MinimeOnlineTransformer.handleResult())

    /**
     * modify character card of ongeki
     */
    fun modifyCard(cardNumber: String, ongekiCardId: String, action: String): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .modifyOngekiCard(cardNumber, ongekiCardId, action)
            .compose(MinimeOnlineTransformer.handleResult())
}