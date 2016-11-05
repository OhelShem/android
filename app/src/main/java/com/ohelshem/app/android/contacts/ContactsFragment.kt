package com.ohelshem.app.android.contacts

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import com.github.salomonbrys.kodein.instance
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.model.Contact
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.contacts_fragment.*

class ContactsFragment : BaseMvpFragment<ContactsView, ContactsPresenter>(), ContactsView {
    override val layoutId: Int = R.layout.contacts_fragment

    override fun createPresenter(): ContactsPresenter = with(kodein()) { ContactsPresenter(instance(), instance()) }

    override fun init() {
        screenManager.screenTitle = ""
        screenManager.setToolbarElevation(false)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
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
}