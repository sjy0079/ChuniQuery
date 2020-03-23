package org.bbs.chuniquery.ongeki.ui.cardmaker

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import io.reactivex.disposables.Disposable
import org.bbs.chuniquery.R
import org.bbs.chuniquery.chunithm.ui.widgets.ChuniQueryColumnView
import org.bbs.chuniquery.drawable.HollowMaskDrawable
import org.bbs.chuniquery.event.CommonRefreshEvent
import org.bbs.chuniquery.ongeki.model.OngekiCardBean
import org.bbs.chuniquery.ongeki.model.OngekiUserCardBean
import org.bbs.chuniquery.ongeki.model.OngekiUserCardListModel
import org.bbs.chuniquery.ongeki.utils.OngekiRequests
import org.bbs.chuniquery.utils.CommonAssetJsonLoader
import org.bbs.chuniquery.utils.dip2px
import org.bbs.chuniquery.utils.getAimeCardId
import org.bbs.chuniquery.utils.getScreenWidth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author shenjiayi@didiglobal.com
 * @since  2020-03-22
 */
class OngekiCardMakerFragment : Fragment() {
    /**
     * main list view
     */
    private lateinit var listView: RecyclerView
    /**
     * refresher of the page
     */
    private lateinit var refresher: SwipeRefreshLayout
    /**
     * mask, to highlight card
     */
    private lateinit var mask: View
    /**
     * spinner, to choice filter
     */
    private lateinit var spinner: Spinner
    /**
     * card list filter
     */
    private var cardFilter: (OngekiCardBean) -> Boolean = {
        true
    }
    /**
     * user's cards
     */
    private var userCardList = OngekiUserCardListModel()
    /**
     * request canceller
     */
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(
            R.layout.ongeki_card_maker_layout,
            container,
            false
        ).also {
            init(it)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        spinner = activity!!.findViewById<Spinner>(R.id.spinner).apply {
            adapter = ArrayAdapter<String>(
                context,
                R.layout.common_drop_down_item,
                listOf(
                    getString(R.string.ongeki_cardmaker_dropdown_all),
                    getString(R.string.ongeki_cardmaker_dropdown_owned),
                    getString(R.string.ongeki_cardmaker_dropdown_N),
                    getString(R.string.ongeki_cardmaker_dropdown_R),
                    getString(R.string.ongeki_cardmaker_dropdown_SR),
                    getString(R.string.ongeki_cardmaker_dropdown_SSR)
                )
            )
            visibility = View.VISIBLE
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> cardFilter = { true }
                        1 -> cardFilter = { userCardList.getCard(it.id) != null }
                        2 -> cardFilter = { it.rarity == "N" }
                        3 -> cardFilter = { it.rarity == "R" }
                        4 -> cardFilter = { it.rarity == "SR" }
                        5 -> cardFilter = { it.rarity == "SSR" }
                    }
                    listView.adapter?.notifyDataSetChanged()
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        refresher.setOnRefreshListener {
            refresh()
        }
        disposable?.dispose()
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CommonRefreshEvent) {
        disposable?.dispose()
        refresher.isRefreshing = true
        Handler().postDelayed({ refresh() }, 500)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        disposable?.dispose()
    }

    /**
     * refresh data
     */
    private fun refresh() {
        disposable = OngekiRequests
            .fetchUserCardList(getAimeCardId())
            .subscribe({
                userCardList = it
                listView.apply {
                    visibility = View.VISIBLE
                    adapter?.notifyDataSetChanged()
                }
                Handler().postDelayed({ refresher.isRefreshing = false }, 300)
            }, {
                it.printStackTrace()
                refresher.isRefreshing = false
            })
    }

    /**
     * init recycler view
     */
    private fun init(root: View) {
        refresher = root.findViewById(R.id.refresher)
        listView = root.findViewById<RecyclerView>(R.id.list).apply {
            visibility = View.GONE
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
                    itemAnimator = null
                }
            adapter = object : RecyclerView.Adapter<OngekiCardMakerViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): OngekiCardMakerViewHolder {
                    val itemView = LayoutInflater
                        .from(context)
                        .inflate(
                            R.layout.ongeki_card_layout,
                            parent,
                            false
                        )
                    val holder = OngekiCardMakerViewHolder(itemView)
                    val cardWidth = context.getScreenWidth() / 2 - context.dip2px(15F)
                    holder.container.layoutParams = FrameLayout.LayoutParams(
                        cardWidth, cardWidth * 356 / 256
                    ).apply {
                        if (viewType == 0) {
                            marginStart = context.dip2px(10F)
                            marginEnd = context.dip2px(5F)
                        } else {
                            marginStart = context.dip2px(5F)
                            marginEnd = context.dip2px(10F)
                        }
                    }
                    return holder
                }

                override fun getItemCount(): Int =
                    CommonAssetJsonLoader.instance.ongekiCards.filter(cardFilter).size

                /**
                 * 0: left
                 * 1: right
                 */
                override fun getItemViewType(position: Int): Int = position % 2

                override fun onBindViewHolder(holder: OngekiCardMakerViewHolder, position: Int) {
                    val cardData =
                        CommonAssetJsonLoader.instance.ongekiCards.filter(cardFilter)[position]
                    val userCardData = userCardList.getCard(cardData.id)
                    holder.apply {
                        container.setOnClickListener {
                            showCardDetail(cardData, it)
                        }
                        container.setOnLongClickListener {
                            showCardStatus(cardData, userCardData, it)
                            true
                        }
                        setOwned(userCardList.getCard(cardData.id) != null)
                        setCardBackground(cardData.rarity)
                    }
                    Glide.with(context)
                        .load(
                            "http://static.samnyan.icu/ongeki/card/UI_Card_${String.format(
                                "%6d",
                                cardData.id
                            )}.png"
                        )
                        .placeholder(R.drawable.ongeki_card_placeholder)
                        .into(holder.card)
                }
            }
        }
        mask = root.findViewById(R.id.mask)
    }

    /**
     * show the card detail
     * if card is not owned, get it!
     */
    private fun showCardDetail(cardInfo: OngekiCardBean, cardView: View) {
        context?.let {
            val dialog = MaterialDialog
                .Builder(it)
                .title(cardInfo.name ?: String())
                .customView(R.layout.ongeki_card_detail_container, false)
                .negativeText(R.string.ongeki_cardmaker_confirm_get_card)
                .positiveText(R.string.ongeki_cardmaker_confirm_get_5_card)
                .onAny { dialog, which ->
                    val addNumber = if (which == DialogAction.NEGATIVE) 1 else 5
                    OngekiRequests
                        .getCard(getAimeCardId(), cardInfo.id.toString(), addNumber)
                        .subscribe({
                            dialog.dismiss()
                            refresh()
                            Toast.makeText(
                                context,
                                R.string.ongeki_cardmaker_toast_get_card_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }, { exception ->
                            exception.printStackTrace()
                            Toast.makeText(
                                context,
                                R.string.ongeki_cardmaker_toast_get_card_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
                .autoDismiss(false)
                .show()
            dialog.apply {
                setOnDismissListener {
                    mask.background = null
                }
                val skills = CommonAssetJsonLoader.instance.ongekiSkills
                customView?.findViewById<LinearLayout>(R.id.container)?.apply {
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_card_id),
                        cardInfo.id.toString()
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_rarity),
                        cardInfo.rarity
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_attribute),
                        cardInfo.attribute
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_school),
                        cardInfo.school
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_gakunen),
                        cardInfo.gakunen
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_skill),
                        skills.getSkill(cardInfo.skillId)?.name
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_cho_kaika_skill),
                        skills.getSkill(cardInfo.choKaikaSkillId)?.name
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_card_number),
                        cardInfo.cardNumber
                    )
                }
            }
        }
        mask.background = HollowMaskDrawable(0x99000000.toInt(), mask, cardView)
    }

    /**
     * show card status
     * if card is owned, provide kaika, cho kaika and digital stock (star)
     */
    private fun showCardStatus(
        cardInfo: OngekiCardBean,
        userCardInfo: OngekiUserCardBean?,
        cardView: View
    ) {
        if (userCardInfo == null) {
            Toast.makeText(context, R.string.ongeki_cardmaker_toast_no_card, Toast.LENGTH_SHORT)
                .show()
            return
        }
        context?.let {
            val dialog = MaterialDialog
                .Builder(it)
                .title(cardInfo.name ?: String())
                .positiveText(R.string.ongeki_cardmaker_title_cho_kaika)
                .negativeText(R.string.ongeki_cardmaker_title_kaika)
                .onAny { dialog, which ->
                    val action = if (which == DialogAction.POSITIVE) "choKaika" else "kaika"
                    OngekiRequests
                        .modifyCard(getAimeCardId(), cardInfo.id.toString(), action)
                        .subscribe({
                            dialog.dismiss()
                            refresh()
                            Toast.makeText(
                                context,
                                R.string.ongeki_cardmaker_toast_modify_card_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }, { exception ->
                            exception.printStackTrace()
                            Toast.makeText(
                                context,
                                R.string.ongeki_cardmaker_toast_modify_card_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
                .customView(R.layout.ongeki_card_detail_container, false)
                .show()

            dialog.apply {
                setOnDismissListener {
                    mask.background = null
                }
                val skills = CommonAssetJsonLoader.instance.ongekiSkills
                customView?.findViewById<LinearLayout>(R.id.container)?.apply {
                    val star =
                        if (userCardInfo.digitalStock ?: 0 > 11 && cardInfo.rarity == "N") {
                            11
                        } else if (userCardInfo.digitalStock ?: 0 > 5 && cardInfo.rarity != "N") {
                            5
                        } else {
                            userCardInfo.digitalStock ?: 1
                        }
                    val skillId =
                        if (userCardInfo.isChoKaika()) {
                            cardInfo.choKaikaSkillId
                        } else {
                            cardInfo.skillId
                        }
                    val maxLevel =
                        if (userCardInfo.isKaika()) {
                            (star - 1) * 5 + 50
                        } else {
                            (star - 1) * 5 + 10
                        }
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_level),
                        userCardInfo.level.toString()
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_max_level),
                        maxLevel.toString()
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_star),
                        star.toString()
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_skill),
                        skills.getSkill(skillId)?.name
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_kaika),
                        if (userCardInfo.isKaika()) getString(R.string.ongeki_cardmaker_yes) else getString(
                            R.string.ongeki_cardmaker_no
                        )
                    )
                    addColumn(
                        this,
                        getString(R.string.ongeki_cardmaker_title_cho_kaika),
                        if (userCardInfo.isChoKaika()) getString(R.string.ongeki_cardmaker_yes) else getString(
                            R.string.ongeki_cardmaker_no
                        )
                    )
                }
            }
        }
        mask.background = HollowMaskDrawable(0x99000000.toInt(), mask, cardView)
    }

    /**
     * add card detail column view
     */
    private fun addColumn(container: LinearLayout, key: String, value: String?) {
        container.addView(
            ChuniQueryColumnView(container.context).apply {
                setTextSize(11F)
                setKV(key, value ?: String(), false)
                if (container.childCount % 2 == 1) {
                    setCardBackgroundColor(0x22212121)
                }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                container.context.dip2px(28F)
            )
        )
    }
}