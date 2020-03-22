package org.bbs.chuniquery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.navigation.NavigationView
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.chunithm.event.ChuniQueryRefreshEvent
import org.bbs.chuniquery.chunithm.model.ChuniQueryProfileBean
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.chunithm.utils.ChuniQueryMusicDBLoader
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {
    companion object {
        /**
         * to store/get card id
         */
        const val CARD_STORED_KEY = "felica_card_id"
        /**
         * to store/get custom ip
         */
        const val IP_STORED_KEY = "custom_ip_address"
        /**
         * avoid duplicate update check
         */
        var isUpdateChecked = false
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    /**
     * update request canceller
     */
    private var updateDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.chuni_query_activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        val navView: NavigationView = this.findViewById(R.id.nav_view)
        navView.getHeaderView(0).setOnClickListener {
            bindCard()
        }
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.chuni_nav_profile,
                R.id.chuni_nav_rating,
                R.id.chuni_nav_recent_play,
                R.id.chuni_nav_items
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        MinimeOnlineClient.instance.init(
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                .getString(IP_STORED_KEY, "")
        )
        ChuniQueryMusicDBLoader.instance.loadData(this)
        checkHasBindingCard()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkHasBindingCard()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ChuniQueryProfileBean) {
        renderHeaderData(event)
    }

    override fun onStop() {
        super.onStop()
        updateDisposable?.dispose()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.chuni_query_main_menu, menu)
        @Suppress("DEPRECATION")
        menu.getItem(0).apply {
            setOnMenuItemClickListener {
                bindIp()
                true
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * check is card info stored
     */
    private fun checkHasBindingCard() {
        val cardId =
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE).getString(
                CARD_STORED_KEY, String()
            )
        if (cardId.isNullOrBlank() || !UriControlActivity.CARD_ID_FROM_URI.isNullOrBlank()) {
            bindCard()
        } else {
            fetchData(cardId, null, false)
        }
    }

    /**
     * bind custom ip
     */
    private fun bindIp() {
        val customIp = getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
            .getString(IP_STORED_KEY, "")
        MaterialDialog.Builder(this)
            .title(R.string.chuni_query_set_server_title)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(
                getString(R.string.chuni_query_set_server_desc), customIp
            ) { _, _ -> }
            .positiveText(R.string.chuni_query_confirm)
            .autoDismiss(false)
            .onPositive { dialog, which ->
                if (DialogAction.POSITIVE == which) {
                    var ipInput = dialog.inputEditText?.text?.toString()
                    if (!ipInput.isNullOrBlank() &&
                        !ipInput.startsWith("http://") &&
                        !ipInput.startsWith("https://")
                    ) {
                        ipInput = "http://${ipInput}"
                    }
                    getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                        .edit()
                        .putString(IP_STORED_KEY, ipInput ?: "")
                        .apply()
                    val msg = if (ipInput.isNullOrBlank()) {
                        getString(R.string.chuni_query_set_server_untying)
                    } else {
                        getString(R.string.chuni_query_set_server_bind)
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
            .autoDismiss(true)
            .cancelable(true)
            .show()
    }

    /**
     * bind card id
     */
    private fun bindCard() {
        MaterialDialog.Builder(this)
            .title(R.string.chuni_query_bind_card_title)
            .inputRangeRes(16, 16, R.color.colorAccent)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(
                getString(R.string.chuni_query_bind_card_desc), UriControlActivity.CARD_ID_FROM_URI
            ) { _, _ -> }
            .positiveText(R.string.chuni_query_confirm)
            .autoDismiss(false)
            .onPositive { dialog, which ->
                if (DialogAction.POSITIVE == which) {
                    fetchData(dialog.inputEditText?.text.toString(), dialog, true)
                }
            }
            .cancelable(true)
            .show()
        UriControlActivity.CARD_ID_FROM_URI = null
    }

    /**
     * fetch data to check id
     */
    @SuppressLint("CheckResult", "ApplySharedPref")
    private fun fetchData(
        cardId: String,
        dialog: MaterialDialog?,
        isBindingAction: Boolean = false
    ) {
        ChuniQueryRequests
            .fetchProfile(cardId)
            .subscribe({
                renderHeaderData(it)
                if (isBindingAction) {
                    getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                        .edit()
                        .putString(CARD_STORED_KEY, cardId)
                        .commit()
                    EventBus.getDefault().post(ChuniQueryRefreshEvent())
                    Toast.makeText(this, R.string.chuni_query_bind_card_succeed, Toast.LENGTH_SHORT)
                        .show()
                }
                if (!isUpdateChecked) {
                    updateDisposable?.dispose()
                    checkUpdate()
                }
                dialog?.dismiss()
            }, {
                it.stackTrace
                val msg =
                    if (it is MinimeOnlineException) {
                        it.errMsg
                    } else {
                        getString(R.string.chuni_query_bind_card_failed)
                    }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            })
    }

    /**
     * render profile data in header
     */
    private fun renderHeaderData(data: ChuniQueryProfileBean) {
        val navView: NavigationView = this.findViewById(R.id.nav_view)
        navView.getHeaderView(0).apply {
            findViewById<TextView>(R.id.noDataInfo).visibility = View.GONE
            findViewById<View>(R.id.mainInfoContainer).visibility = View.VISIBLE
            findViewById<TextView>(R.id.name).text = data.userName
            findViewById<TextView>(R.id.levelNumber).text = data.userLevel
            findViewById<ChuniQueryRatingView>(R.id.ratingNumber)
                .setRating(ChuniQueryRatingView.formatRating(data.userRating).toFloat())
        }
    }

    /**
     * check update
     */
    private fun checkUpdate() {
        updateDisposable = ChuniQueryRequests
            .fetchUpdateInfo()
            .subscribe({
                if (it.version != null &&
                    it.version!! > BuildConfig.VERSION_NAME &&
                    !it.url.isNullOrBlank()
                ) {
                    isUpdateChecked = true
                    MaterialDialog.Builder(this)
                        .title(R.string.chuni_query_update_title)
                        .content(R.string.chuni_query_update_content)
                        .positiveText(R.string.chuni_query_confirm)
                        .onPositive { _, which ->
                            if (DialogAction.POSITIVE == which) {
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data = Uri.parse(it.url)
                                })
                            }
                        }
                        .autoDismiss(true)
                        .cancelable(true)
                        .show()
                }
            }, {
                it.printStackTrace()
            })
    }
}
