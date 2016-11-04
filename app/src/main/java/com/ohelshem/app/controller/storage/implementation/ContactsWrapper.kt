package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.model.Contact

class ContactsWrapper(val contactsProvider: ContactsProvider) : ContactsProvider {
    private val layersContacts: MutableMap<Int, List<Contact>> = mutableMapOf()
    private val cachedClassContacts: MutableMap<Pair<Int, Int>, List<Contact>> = mutableMapOf()
    override fun getContacts(layer: Int, clazz: Int): List<Contact> {
        if (clazz == -1) {
            return layersContacts.getOrPut(layer) { contactsProvider.getContacts(layer, clazz) }
        } else {
            val pair = layer to clazz
            if (pair in cachedClassContacts) {
                return cachedClassContacts[pair]!!
            } else if (layer in layersContacts) {
                val value = layersContacts[layer]!!.filter { it.clazz == clazz }
                cachedClassContacts += pair to value
                return value
            } else {
                return cachedClassContacts.getOrPut(pair) { contactsProvider.getContacts(layer, clazz) }
            }
        }
    }
}