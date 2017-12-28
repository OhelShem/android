package com.ohelshem.app.android.contacts

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.backgroundColor
import com.ohelshem.app.android.contacts.birthdays.BirthdaysAdapter
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.storage.implementation.Contacts
import com.ohelshem.app.getIsraelCalendar
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.contacts_fragment.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk15.listeners.onClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.topPadding
import java.util.*


class ContactsFragment : BaseMvpFragment<ContactsView, ContactsPresenter>(), ContactsView {
    override val layoutId: Int = R.layout.contacts_fragment
    val storage: Storage by kodein.instance()

    override fun createPresenter(): ContactsPresenter = with(kodein()) { ContactsPresenter(instance(), instance()) }

    override fun init() {
        screenManager.screenTitle = ""
        screenManager.setToolbarElevation(false)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        birthdaysFab.onClick {
            showBirthdaysDialog()
        }

    }

    @SuppressLint("SetTextI18n")
    override fun showContacts(layer: Int, clazz: Int, contacts: List<Contact>) {
        if (contacts.isEmpty() && !storage.isStudent()) {
            teacherErrorView.show()
        } else {
            teacherErrorView.hide()
            studentsInClass.text = contacts.size.toString()
            classname.text = stringArrayRes(R.array.layers)[layer - 9] + clazz
            recyclerView.adapter = ContactsAdapter(activity!!, contacts) {
                makeCall(it.phone)
            }
        }
    }

    private fun makeCall(number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun showBirthdaysDialog() {
        val today = getIsraelCalendar()
        val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
        val birthdays = Contacts.getContacts(-1, -1).filter {
            val cal = it.birthday.toCalendar()
            cal[Calendar.DAY_OF_MONTH] == day && cal[Calendar.MONTH] == month
        }

        if (birthdays.isNotEmpty()) {
            val view = UI {
                recyclerView {
                    backgroundColor = activity!!.backgroundColor
                    clipToPadding = false
                    topPadding = 4
                    bottomPadding = 4
                    layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    adapter = BirthdaysAdapter(activity!!, birthdays)

                    setHasFixedSize(true)
                }
            }.view
            MaterialStyledDialog.Builder(activity)
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setTitle(getString(R.string.birthdays_in_school))
                    .autoDismiss(false)
                    .setCustomView(view)
                    .show()

        } else toast(getString(R.string.no_birthdays_in_school))
    }
}