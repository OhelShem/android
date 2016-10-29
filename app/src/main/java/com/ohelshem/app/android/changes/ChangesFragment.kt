package com.ohelshem.app.android.changes

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.stringResource
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.controller.timetable.TimetableController.Companion.DayType
import com.ohelshem.app.getDay
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.changes_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class ChangesFragment : BaseMvpFragment<ChangesView, LayerChangesPresenter>(), ChangesView {
    private val day by stringResource(R.string.day)
    private val weekDays by lazy { resources.getStringArray(R.array.week_days) }

    override val layoutId: Int = R.layout.changes_fragment

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    override fun init() {
        screenManager.setToolbarElevation(false)

        initPager()
    }

    private fun initPager() {
        pager.adapter = ChangesFragmentAdapter(childFragmentManager)
    }

    private fun initTabs() {
        val tabs = screenManager.inlineTabs

        if (tabs.tabCount == 0) {
            tabs.setupWithViewPager(pager)
            tabs.getTabAt(0)!!.icon = drawableRes(R.drawable.ic_list)
            tabs.getTabAt(1)!!.icon = drawableRes(R.drawable.ic_table)
        }
    }

    override fun onError(error: UpdateError) {
        screenManager.screenTitle = getString(R.string.changes)
        when (error) {
            UpdateError.NoData -> {
                progressActivity.showError(drawableRes(R.drawable.ic_sync_problem), getString(R.string.no_data), getString(R.string.no_data_subtitle),
                        getString(R.string.refresh)) {
                    presenter.refresh(screenManager)
                }
            }
            UpdateError.Connection -> {
                progressActivity.showError(drawableRes(R.drawable.ic_sync_problem), getString(R.string.no_connection), getString(R.string.no_connection_subtitle),
                        getString(R.string.try_again)) {
                    presenter.refresh(screenManager)
                }
            }
            else -> {
                progressActivity.showError(drawableRes(R.drawable.ic_error), getString(R.string.general_error), getString(R.string.try_again), getString(R.string.try_again)) {
                    presenter.refresh(screenManager)
                }
            }
        }
    }

    /**
     * Layer empty data, therefore there are no changes and it is safe to hide the tabs.
     */
    override fun onEmptyData(dayType: DayType) {
        if (progressActivity.isError)
            progressActivity.showContent()
        setTitle()
        if (dayType == DayType.Holiday || dayType == DayType.Summer) {
            progressActivity.showEmpty(drawableRes(R.drawable.ic_beach), getString(R.string.holiday_today), getString(R.string.holiday_today_subtitle))
        } else {
            if (dayType == DayType.Saturday)
                progressActivity.showEmpty(drawableRes(R.drawable.ic_beach), getString(R.string.shabat_today), getString(R.string.shabat_today_subtitle))
            else if (dayType == DayType.Friday)
                progressActivity.showEmpty(drawableRes(R.drawable.ic_beach), getString(R.string.friday_today), getString(R.string.friday_today_subtitle))
            else
                progressActivity.showError(drawableRes(R.drawable.ic_error), getString(R.string.no_changes), getString(R.string.no_changes_subtitle), getString(R.string.go_to_timetable)) {
                    presenter.launchTimetableScreen(screenManager)
                }
        }
    }


    override fun setData(changes: List<Change>) {
        initTabs()

        childFragmentManager.fragments?.forEach {
            (it as? BaseChangesFragment<*>)?.presenter?.update()
        }
    }

    override val isShowingData: Boolean
        get() = presenter.hasData


    private fun setTitle() {
        val data = presenter.changesDate
        screenManager.screenTitle = day + " " + weekDays[data.toCalendar().getDay() - 1] + " " + ChangesDataFormat.format(Date(data))
    }

    class ChangesFragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return if (position == 0) ClassChangesFragment()
            else LayerChangesFragment()
        }

        override fun getCount(): Int = 2

    }

    companion object {
        private val ChangesDataFormat = SimpleDateFormat("dd/MM")
    }
}