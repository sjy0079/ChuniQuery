package org.bbs.chuniquery

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger

/**
 * @author BBS
 * @since  2020-03-14
 */
class UriControlActivity : AppCompatActivity() {
    companion object {
        /**
         * card id from outside app
         */
        var CARD_ID_FROM_URI: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleUri(intent)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleUri(intent)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * handle action of jumping by uri
     */
    @SuppressLint("DefaultLocale")
    private fun handleUri(newIntent: Intent? = null) {
        val uri = newIntent?.data ?: intent.data ?: return
        val code = uri.toString().replace("https://my-aime.net/aime/i/registq.html?ac=", "")
        CARD_ID_FROM_URI = BigInteger(code).toString(16).toUpperCase()
        if (!CARD_ID_FROM_URI.isNullOrBlank()) {
            while (CARD_ID_FROM_URI!!.length < 16) {
                CARD_ID_FROM_URI = "0${CARD_ID_FROM_URI}"
            }
        }
    }
}