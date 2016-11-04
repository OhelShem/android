package com.ohelshem.app.controller.storage

import com.ohelshem.app.model.Contact

interface ContactsProvider {
    fun getContacts(layer: Int, clazz: Int = -1): List<Contact>
}