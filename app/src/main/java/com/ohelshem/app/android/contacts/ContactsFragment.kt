package com.ohelshem.app.android.contacts

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.storage.implementation.Contacts
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.birthdays_dialog.view.*
import kotlinx.android.synthetic.main.contacts_fragment.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.act
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

    override fun showContacts(layer: Int, clazz: Int, contacts: List<Contact>) {
        studentsInClass.text = contacts.size.toString()
        classname.text = stringArrayRes(R.array.layers)[layer - 9] + clazz
        recyclerView.adapter = ContactsAdapter(activity, contacts) {
            makeCall(it.phone)
        }
    }

    fun makeCall(number: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun showBirthdaysDialog() {
        val today = Calendar.getInstance()
        val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
        val birthdays = Contacts.getContacts(-1, -1).filter {
            val cal = it.birthday.toCalendar()
            cal[Calendar.DAY_OF_MONTH] == day && cal[Calendar.MONTH] == month
        }

        val view = act.layoutInflater.inflate(R.layout.birthdays_dialog, null, false) //fixme
        view.birthdaysRecyclerView.adapter = BirthdaysAdapter(activity, birthdays)

        val alert = AlertDialog.Builder(context)
                .setTitle(getString(R.string.birthdays_in_school))
                .setNeutralButton(getString(R.string.tests_dialog_close)) {
                    dialog, whichButton ->  dialog.cancel()
                }.create()
        alert.setView(view)
        alert.show()

    }

}