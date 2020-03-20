package org.bbs.chuniquery.network

import com.google.gson.JsonElement
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-12
 */
interface MinimeOnlineService {
    @GET("/checkUpdate")
    fun getUpdateInfo(): Observable<JsonElement>

    @GET("/query?table=cm_user_data")
    fun getPlayerInfo(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/query?table=cm_user_music")
    fun getMusicInfo(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/query?table=cm_user_playlog")
    fun getPlayLog(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/items?action=fetch")
    fun getItems(@Query("card") cardId: String): Observable<JsonElement>

    @GET("/items?action=modify")
    fun modifyItems(
        @Query("card") cardId: String,
        @Query("item_id") itemId: String,
        @Query("item_count") itemCount: String
    ): Observable<JsonElement>

    @GET("/userInfo")
    fun modifyName(
        @Query("card") cardId: String,
        @Query("user_name") userName: String
    ): Observable<JsonElement>
}