package org.bbs.chuniquery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.View
import android.widget.Spinner
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
import com.google.android.material.tabs.TabLayout
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.chunithm.model.ChuniQueryProfileBean
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.bbs.chuniquery.event.CommonRefreshEvent
import org.bbs.chuniquery.network.MinimeOnlineClient
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.ongeki.utils.OngekiRequests
import org.bbs.chuniquery.utils.CommonAssetJsonLoader
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {
    companion object {
        /**
         * to store/get felica card id
         */
        const val FELICA_CARD_STORED_KEY = "felica_card_id"
        /**
         * to store/get amie card id
         */
        const val AIME_CARD_STORED_KEY = "aime_card_id"
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
    /**
     * indicator for chuni rating fragment
     */
    private lateinit var indicator: TabLayout
    /**
     * spinner for ongeki card maker fragment
     */
    private lateinit var spinner: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.chuni_query_activity_main)
        indicator = findViewById(R.id.indicator)
        spinner = findViewById(R.id.spinner)

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
            bindFelicaCard()
        }
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.chuni_nav_profile,
                R.id.chuni_nav_rating,
                R.id.chuni_nav_recent_play,
                R.id.chuni_nav_items,
                R.id.ongeki_nav_card_maker
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val oldNavListener =
            NavigationView::class.java.getDeclaredField("listener").apply {
                isAccessible = true
            }.get(navView) as NavigationView.OnNavigationItemSelectedListener
        navView.setNavigationItemSelectedListener {
            if (it.itemId != R.id.chuni_nav_rating) {
                indicator.visibility = View.GONE
            }
            if (it.itemId != R.id.ongeki_nav_card_maker) {
                spinner.visibility = View.GONE
            }
            if (it.itemId == R.id.ongeki_nav_card_maker) {
                checkHasBindingAimeCard(Runnable {
                    oldNavListener.onNavigationItemSelected(it)
                })
                return@setNavigationItemSelectedListener false
            } else {
                oldNavListener.onNavigationItemSelected(it)
            }
        }

        MinimeOnlineClient.instance.init(
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                .getString(IP_STORED_KEY, "")
        )
        CommonAssetJsonLoader.instance.loadChuniMusicDBData(this)
        CommonAssetJsonLoader.instance.loadOngekiCardListData(this)
        CommonAssetJsonLoader.instance.loadOngekiSkillListData(this)
        checkHasBindingFelicaCard()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkHasBindingFelicaCard()
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

    override fun onBackPressed() {
        indicator.visibility = View.GONE
        spinner.visibility = View.GONE
        super.onBackPressed()
    }

    /**
     * check is card info stored
     */
    private fun checkHasBindingFelicaCard() {
        val cardId =
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE).getString(
                FELICA_CARD_STORED_KEY, String()
            )
        if (cardId.isNullOrBlank() || !UriControlActivity.CARD_ID_FROM_URI.isNullOrBlank()) {
            bindFelicaCard()
        } else {
            fetchChuniProfileData(cardId, null, false)
        }
    }

    /**
     * check is card info stored
     */
    private fun checkHasBindingAimeCard(callback: Runnable) {
        val cardId =
            getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE).getString(
                AIME_CARD_STORED_KEY, String()
            )
        bindAimeCard(cardId, callback)
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
            .positiveText(R.string.common_confirm)
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
    private fun bindFelicaCard() {
        MaterialDialog.Builder(this)
            .title(R.string.common_bind_card_title)
            .inputRangeRes(16, 16, R.color.colorAccent)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(
                getString(R.string.chuni_query_bind_card_desc), UriControlActivity.CARD_ID_FROM_URI
            ) { _, _ -> }
            .positiveText(R.string.common_confirm)
            .autoDismiss(false)
            .onPositive { dialog, which ->
                if (DialogAction.POSITIVE == which) {
                    fetchChuniProfileData(dialog.inputEditText?.text.toString(), dialog, true)
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
    private fun fetchChuniProfileData(
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
                        .putString(FELICA_CARD_STORED_KEY, cardId)
                        .commit()
                    EventBus.getDefault().post(CommonRefreshEvent())
                    Toast.makeText(this, R.string.common_bind_card_succeed, Toast.LENGTH_SHORT)
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
                        getString(R.string.common_bind_card_failed)
                    }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            })
    }

    /**
     * bind aime card id
     */
    private fun bindAimeCard(cardId: String?, callback: Runnable) {
        MaterialDialog.Builder(this)
            .title(R.string.common_bind_card_title)
            .inputRangeRes(20, 20, R.color.colorAccent)
            .inputType(InputType.TYPE_CLASS_NUMBER)
            .input(
                getString(R.string.ongeki_bind_card_desc), cardId
            ) { _, _ -> }
            .positiveText(R.string.common_confirm)
            .autoDismiss(false)
            .onPositive { dialog, which ->
                if (DialogAction.POSITIVE == which) {
                    fetchOngekiProfileData(dialog.inputEditText?.text.toString(), dialog, callback)
                }
            }
            .cancelable(true)
            .show()
    }

    /**
     * fetch data to check id
     */
    @SuppressLint("CheckResult", "ApplySharedPref")
    private fun fetchOngekiProfileData(
        cardId: String,
        dialog: MaterialDialog?,
        callback: Runnable
    ) {
        OngekiRequests
            .fetchProfile(cardId)
            .subscribe({
                getSharedPreferences(MainActivity::class.java.name, Context.MODE_PRIVATE)
                    .edit()
                    .putString(AIME_CARD_STORED_KEY, cardId)
                    .commit()
                EventBus.getDefault().post(CommonRefreshEvent())
                Toast.makeText(this, R.string.common_confirm_card_succeed, Toast.LENGTH_SHORT)
                    .show()
                callback.run()
                dialog?.dismiss()
            }, {
                it.stackTrace
                val msg =
                    if (it is MinimeOnlineException) {
                        it.errMsg
                    } else {
                        getString(R.string.common_bind_card_failed)
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
                        .positiveText(R.string.common_confirm)
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
