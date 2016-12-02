package com.ohelshem.app.android.contacts

import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.SharedStorage

class ContactsPresenter(val storage: SharedStorage, val contactsProvider: ContactsProvider): BasePresenter<ContactsView>() {
    override fun onCreate() {
        if (storage.isStudent()) {
            val layer = storage.userData.layer
            val clazz = storage.userData.clazz
            load(layer, clazz)
        } else {
            handleTeacherContacts()
        }
    }

    override fun onChoosingClass() {
        val currentClass = currentClass
        if (currentClass == null) handleTeacherContacts()
        else load(currentClass.clazz, currentClass.layer)
    }

    private fun handleTeacherContacts() {
        //FIXME
    }

    private fun load(clazz: Int, layer: Int) {
        view?.showContacts(layer, clazz, contacts = contactsProvider.getContacts(layer, clazz))
    }
    override fun onDestroy() = Unit
}