package com.ohelshem.app.android.contacts

import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.Storage

class ContactsPresenter(val storage: Storage, val contactsProvider: ContactsProvider): BasePresenter<ContactsView>() {
    override fun onCreate() {
        val layer = storage.userData.layer
        val clazz = storage.userData.clazz
        view?.showContacts(layer, clazz, contacts = contactsProvider.getContacts(layer, clazz))
    }

    override fun onDestroy() = Unit
}