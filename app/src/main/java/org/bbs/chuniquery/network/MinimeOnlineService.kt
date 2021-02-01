package org.bbs.chuniquery.network

import com.google.gson.JsonElement
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author BBS
 * @since  2020-03-12
 */
interface MinimeOnlineService {
    @GET("/checkUpdate")
    fun getUpdateInfo(): Observable<JsonElement>

    // chunithm

    @GET("/query?table=cm_user_data")
    fun getChuniPlayerInfo(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/query?table=cm_user_music")
    fun getChuniMusicInfo(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/query?table=cm_user_playlog")
    fun getChuniPlayLog(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/items?action=fetch")
    fun getChuniItems(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/items?action=modify")
    fun modifyChuniItems(
        @Query("card") cardId: String,
        @Query("item_id") itemId: String,
        @Query("item_count") itemCount: String
    ): Observable<JsonElement>

    @GET("/userInfo")
    fun modifyChuniName(
        @Query("card") cardId: String,
        @Query("user_name") userName: String
    ): Observable<JsonElement>

    // ongeki
    @GET("/query?table=mu3_user_data")
    fun getOngekiPlayerInfo(@Query("card_number") cardNumber: String): Observable<JsonElement>

    @GET("/query?table=mu3_user_card")
    fun getOngekiUserCardInfo(@Query("card_number") cardNumber: String): Observable<JsonElement>

    @GET("/ongekiAddCard")
    fun getOngekiCard(
        @Query("card_number") cardNumber: String,
        @Query("ongeki_card_id") ongekiCardId: String,
        @Query("add_number") addNumber: Int
    ): Observable<JsonElement>

    @GET("/ongekiModifyCard")
    fun modifyOngekiCard(
        @Query("card_number") cardNumber: String,
        @Query("ongeki_card_id") ongekiCardId: String,
        @Query("action") action: String
    ): Observable<JsonElement>
}