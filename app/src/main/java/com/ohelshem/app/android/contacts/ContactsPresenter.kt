package com.ohelshem.app.android.contacts

import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.SharedStorage

class ContactsPresenter(val storage: SharedStorage, private val contactsProvider: ContactsProvider) : BasePresenter<ContactsView>() {
    override fun onCreate() {
        if (storage.isStudent()) {
            val layer = storage.userData.layer
            val clazz = storage.userData.clazz
            load(layer = layer, clazz =clazz)
        } else {
            onChoosingClass()
        }
    }

    override fun onChoosingClass() {
        val currentClass = currentClass
        if (currentClass == null) handleTeacherContacts()
        else load(layer = currentClass.layer, clazz = currentClass.clazz)
    }

    private fun handleTeacherContacts() {
       view?.showContacts(0, 0, emptyList())
    }

    private fun load(layer: Int, clazz: Int) {
        view?.showContacts(layer, clazz, contacts = contactsProvider.getContacts(layer, clazz))
    }

    override fun onDestroy() = Unit
}