package org.bbs.chuniquery.chunithm.utils

import com.google.gson.Gson
import io.reactivex.Observable
import org.bbs.chuniquery.chunithm.model.*
import org.bbs.chuniquery.model.CommonUpdateModel
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.network.MinimeOnlineTransformer

/**
 * @author BBS
 * @since  2020-03-13
 */
object ChuniQueryRequests {
    /**
     * fetch the version info for updating
     */
    fun fetchUpdateInfo(): Observable<CommonUpdateModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getUpdateInfo()
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, CommonUpdateModel::class.java)
                Observable.just(model)
            }

    /**
     * fetch the profile of user, input the card id from felica.txt
     */
    fun fetchProfile(cardId: String): Observable<ChuniQueryProfileBean> =
        MinimeOnlineClient
            .instance
            .getService()
            .getChuniPlayerInfo(cardId)
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
            .getChuniMusicInfo(cardId)
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
            .getChuniPlayLog(cardId)
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
     * fetch user team name
     */
    fun fetchUserTeam(cardId: String): Observable<String> =
        MinimeOnlineClient
            .instance
            .getService()
            .getChuniGeneralData(cardId)
            .compose(MinimeOnlineTransformer.handleResult())
            .flatMap {
                val model = Gson().fromJson(it, ChuniQueryGeneralDataModel::class.java)
                if (model.isEmpty()) {
                    Observable.just(String())
                } else {
                    for (bean in model) {
                        if (bean.key == "user_team_name" && !bean.value.isNullOrBlank()) {
                            return@flatMap Observable.just(bean.value!!)
                        }
                    }
                    Observable.just(String())
                }
            }

    /**
     * fetch user's items
     */
    fun fetchItems(cardId: String): Observable<ChuniQueryItemsModel> =
        MinimeOnlineClient
            .instance
            .getService()
            .getChuniItems(cardId)
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
            .modifyChuniItems(cardId, itemId, itemCount)
            .compose(MinimeOnlineTransformer.handleResult())

    /**
     * modify user name
     */
    fun modifyUserName(cardId: String, userName: String): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .modifyChuniName(cardId, userName)
            .compose(MinimeOnlineTransformer.handleResult())

    /**
     * modify user team
     */
    fun modifyUserTeam(cardId: String, userTeam: String): Observable<Any> =
        MinimeOnlineClient
            .instance
            .getService()
            .modifyChuniTeamName(cardId, userTeam)
            .compose(MinimeOnlineTransformer.handleResult())
}