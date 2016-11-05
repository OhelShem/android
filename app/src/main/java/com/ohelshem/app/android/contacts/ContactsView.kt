package com.ohelshem.app.android.contacts

import com.hannesdorfmann.mosby.mvp.MvpView
import com.ohelshem.app.model.Contact

interface ContactsView: MvpView {
    fun showContacts(layer: Int, clazz: Int, contacts: List<Contact>)
}