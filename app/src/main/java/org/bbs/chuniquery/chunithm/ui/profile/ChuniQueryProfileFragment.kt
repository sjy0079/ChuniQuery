package org.bbs.chuniquery.chunithm.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.R
import org.bbs.chuniquery.event.CommonRefreshEvent
import org.bbs.chuniquery.chunithm.model.ChuniQueryProfileBean
import org.bbs.chuniquery.network.MinimeOnlineException
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryColumnView
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryRatingView
import org.bbs.chuniquery.chunithm.utils.ChuniQueryRequests
import org.bbs.chuniquery.utils.dip2px
import org.bbs.chuniquery.utils.getFelicaCardId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChuniQueryProfileFragment : Fragment() {
    /**
     * the disposable of request, for cancel request after fragment detached
     */
    private var disposable: Disposable? = null
    /**
     * container of user info column
     */
    private lateinit var columnContainer: LinearLayout
    /**
     * container of the main info
     */
    private lateinit var mainInfoContainer: View
    /**
     * player's name
     */
    private lateinit var nameView: TextView
    /**
     * player's rating
     */
    private lateinit var ratingView: ChuniQueryRatingView
    /**
     * refresher of the page
     */
    private lateinit var refresher: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.chuni_query_fragment_profile, container, false)
        columnContainer = root.findViewById(R.id.container)
        mainInfoContainer = root.findViewById(R.id.mainInfoContainer)
        nameView = root.findViewById(R.id.name)
        ratingView = root.findViewById(R.id.rating)
        refresher = root.findViewById(R.id.refresher)
        return root
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        refresher.setOnRefreshListener {
            refresh()
        }
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CommonRefreshEvent) {
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        disposable?.dispose()
    }

    /**
     * refresh profile info
     */
    private fun refresh() {
        if (getFelicaCardId().isEmpty()) {
            refresher.isRefreshing = false
            return
        }
        disposable = ChuniQueryRequests
            .fetchProfile(getFelicaCardId())
            .subscribe({
                EventBus.getDefault().post(it)
                if (activity == null || activity!!.isFinishing) {
                    return@subscribe
                }
                parseData(it)
                Handler().postDelayed({ refresher.isRefreshing = false }, 300)
            }, {
                it.stackTrace
                refresher.isRefreshing = false
            })
    }

    /**
     * render the data
     */
    private fun parseData(data: ChuniQueryProfileBean) {
        mainInfoContainer.visibility = View.VISIBLE
        nameView.apply {
            text = data.userName
            setOnLongClickListener {
                changeName()
                true
            }
        }
        ratingView.setRating(ChuniQueryRatingView.formatRating(data.userRating).toFloat())

        columnContainer.removeAllViews()
        addColumn(
            getString(R.string.chuni_query_profile_column_key_level),
            data.userLevel
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_best_rating),
            ChuniQueryRatingView.formatRating(data.userBestRating),
            true
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_point),
            data.userPoint
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_play_count),
            data.userPlayCount
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_first_play),
            data.userFirstPlayDate
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_last_play),
            data.userLastPlayDate
        )
        addColumn(
            getString(R.string.chuni_query_profile_column_key_card_id),
            data.userAccessCode,
            isValueSelectable = true
        )
    }

    /**
     * add column view
     */
    private fun addColumn(
        key: String,
        value: String?,
        isRating: Boolean = false,
        isValueSelectable: Boolean = false
    ) {
        columnContainer.addView(
            ChuniQueryColumnView(activity!!).apply {
                setKV(key, value ?: String(), isRating)
                setValueSelectable(isValueSelectable)
                if (columnContainer.childCount % 2 == 1) {
                    setCardBackgroundColor(0x22212121)
                }
            },
            LinearLayout.LayoutParams(MATCH_PARENT, context!!.dip2px(40F))
        )
    }

    /**
     * change user name
     */
    private fun changeName() {
        context?.let {
            MaterialDialog.Builder(it)
                .title(R.string.chuni_query_profile_modify_title)
                .inputRange(1, 8)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(
                    null, nameView.text?.toString()
                ) { _, _ -> }
                .positiveText(R.string.common_confirm)
                .autoDismiss(false)
                .onPositive { dialog, which ->
                    if (DialogAction.POSITIVE == which) {
                        val modifyName = dialog.inputEditText?.text?.toString()
                            ?: return@onPositive
                        sendModifyUserNameRequest(modifyName, dialog)
                    }
                }
                .autoDismiss(false)
                .cancelable(true)
                .show()
        }
    }

    /**
     * request user info api
     */
    @SuppressLint("CheckResult")
    private fun sendModifyUserNameRequest(modifyName: String, dialog: MaterialDialog) {
        ChuniQueryRequests
            .modifyUserName(getFelicaCardId(), modifyName)
            .subscribe({
                Toast.makeText(
                    context,
                    R.string.chuni_query_profile_modify_succeed,
                    Toast.LENGTH_SHORT
                ).show()
                refresher.isRefreshing = true
                Handler().postDelayed({ refresh() }, 500)
                dialog.dismiss()
            }, { e ->
                if (e is MinimeOnlineException) {
                    Toast.makeText(context, e.errMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        R.string.chuni_query_profile_modify_failed,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }
}