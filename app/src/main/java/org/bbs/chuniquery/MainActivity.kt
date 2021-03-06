package org.bbs.chuniquery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.chunithm.event.ChuniTeamNameAction
import org.bbs.chuniquery.chunithm.model.ChuniQueryProfileBean
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.bbs.chuniquery.event.CommonRefreshEvent
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.ongeki.utils.OngekiRequests
import org.bbs.chuniquery.utils.CommonDataFetcher
import org.bbs.chuniquery.utils.getFelicaCardId
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

    /**
     * loading view
     */
    private lateinit var loadingView: View

    /**
     * navigation view
     */
    private lateinit var navView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.chuni_query_activity_main)
        indicator = findViewById(R.id.indicator)
        spinner = findViewById(R.id.spinner)
        loadingView = createLoadingView()

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
        navView = this.findViewById(R.id.nav_view)
        navView.getHeaderView(0).apply {
            findViewById<TextView>(R.id.team).setOnClickListener {
                modifyTeamName(it as TextView)
            }
            setOnClickListener {
                bindFelicaCard()
            }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ChuniTeamNameAction) {
        navView.getHeaderView(0).apply {
            if (event.name.isNotBlank()) {
                findViewById<TextView>(R.id.team).text = event.name
            }
        }
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
        menu.getItem(1).apply {
            setOnMenuItemClickListener {
                showLoading()
                CommonDataFetcher.fetch(this@MainActivity) {
                    hideLoading()
                }
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

    private fun showLoading() {
        loadingView.alpha = 0F
        (this@MainActivity.window.decorView as ViewGroup).addView(loadingView)
        ObjectAnimator.ofFloat(loadingView, "alpha", 0F, 1F).apply {
            duration = 200L
        }.start()
    }

    private fun hideLoading() {
        ObjectAnimator.ofFloat(loadingView, "alpha", 1F, 0F).apply {
            duration = 200L
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    (this@MainActivity.window.decorView as ViewGroup).removeView(loadingView)
                }
            })
        }.start()
    }

    /**
     * create a view with circle progress bar
     */
    private fun createLoadingView(): View = FrameLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(0x7E000000)
        setOnClickListener { }
        addView(CircularProgressIndicator(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            isIndeterminate = true
        })
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
            .fetchUserTeam(cardId)
            .subscribe({
                navView.getHeaderView(0).apply {
                    if (it.isNotBlank()) {
                        findViewById<TextView>(R.id.team).text = it
                    }
                }
            }, {
                it.printStackTrace()
                val msg =
                    if (it is MinimeOnlineException) {
                        it.errMsg
                    } else {
                        getString(R.string.common_network_error)
                    }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            })
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

    /**
     * modify team name, max 20 chars
     */
    private fun modifyTeamName(textView: TextView) {
        MaterialDialog.Builder(this)
            .title(R.string.chuni_query_team_modify_title)
            .inputRangeRes(1, 20, R.color.colorAccent)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(String(), textView.text) { _, _ -> }
            .positiveText(R.string.common_confirm)
            .autoDismiss(false)
            .onPositive { dialog, which ->
                if (DialogAction.POSITIVE == which) {
                    val modifyTeam = dialog.inputEditText?.text?.toString()
                        ?: return@onPositive
                    ChuniQueryRequests
                        .modifyUserTeam(getFelicaCardId(), modifyTeam)
                        .subscribe({ _ ->
                            Toast.makeText(
                                this,
                                R.string.chuni_query_profile_modify_succeed,
                                Toast.LENGTH_SHORT
                            ).show()
                            textView.text = modifyTeam
                            dialog.dismiss()
                        }, { e ->
                            if (e is MinimeOnlineException) {
                                Toast.makeText(this, e.errMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.common_network_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
            .cancelable(true)
            .show()
    }
}
