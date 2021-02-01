package org.bbs.chuniquery.network

import java.lang.Exception

/**
 * @author BBS
 * @since  2020-03-12
 */
class MinimeOnlineException(val errNum: String, val errMsg: String) : Exception() {

    companion object {
        /**
         * fetch an empty list from sql
         */
        fun createNoDataException() = MinimeOnlineException("-1", "no data record!")
    }
}
