package org.bbs.chuniquery.chunithm.utils

import com.google.gson.Gson
import io.reactivex.Observable
import org.bbs.chuniquery.chunithm.model.*
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.network.MinimeOnlineTransformer

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-13
 */
object ChuniQueryRequests {
    /**
     * fetch the version info for updating
     */
    fun fetchUpdateInfo(): Observable<ChuniQueryUpdateModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getUpdateInfo()
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryUpdateModel::class.java)
                Observable.just(model)
            }

    /**
     * fetch the profile of user, input the card id from felica.txt
     */
    fun fetchProfile(cardId: String): Observable<ChuniQueryProfileBean> =
        MinimeOnlineClient
            .instance
            .getService()
            .getPlayerInfo(cardId)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryProfileModel::class.java)
                if (model.isEmpty()) {
                    Observable.error(MinimeOnlineException.createNoDataException())
                } else {
                    Observable.just(model[0])
                }
            }

    /**
     * fetch all music record
     */
    fun fetchMusic(cardId: String): Observable<ChuniQueryMusicModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getMusicInfo(cardId)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryMusicModel::class.java)
                if (model.isEmpty()) {
                    Observable.error(MinimeOnlineException.createNoDataException())
                } else {
                    Observable.just(model)
                }
            }

    /**
     * fetch all history record
     */
    fun fetchPlayLog(cardId: String): Observable<ChuniQueryMusicModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getPlayLog(cardId)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryMusicModel::class.java)
                if (model.isEmpty()) {
                    Observable.error(MinimeOnlineException.createNoDataException())
                } else {
                    Observable.just(model)
                }
            }

    /**
     * fetch user's items
     */
    fun fetchItems(cardId: String): Observable<ChuniQueryItemsModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getItems(cardId)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryItemsModel::class.java)
                if (model.isEmpty()) {
                    Observable.error(MinimeOnlineException.createNoDataException())
                } else {
                    Observable.just(model)
                }
            }

    /**
     * modify the info of one item
     */
    fun modifyItems(cardId: String, itemId: String, itemCount: String): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .modifyItems(cardId, itemId, itemCount)
            .compose(MinimeOnlineTransformer.handleResult())

    /**
     * modify user name
     */
    fun modifyUserName(cardId: String, userName: String): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .modifyName(cardId, userName)
            .compose(MinimeOnlineTransformer.handleResult())
}