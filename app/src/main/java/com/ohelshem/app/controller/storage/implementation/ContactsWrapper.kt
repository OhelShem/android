package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.model.Contact

class ContactsWrapper(private val contactsProvider: ContactsProvider) : ContactsProvider {
    private val layersContacts: MutableMap<Int, List<Contact>> = mutableMapOf()
    private val cachedClassContacts: MutableMap<Pair<Int, Int>, List<Contact>> = mutableMapOf()
    override fun getContacts(layer: Int, clazz: Int): List<Contact> {
        return if (clazz == -1) {
            layersContacts.getOrPut(layer) { contactsProvider.getContacts(layer, clazz) }
        } else {
            val pair = layer to clazz
            when {
                pair in cachedClassContacts -> cachedClassContacts[pair]!!
                layer in layersContacts -> {
                    val value = layersContacts[layer]!!.filter { it.clazz == clazz }
                    cachedClassContacts += pair to value
                    value
                }
                else -> cachedClassContacts.getOrPut(pair) { contactsProvider.getContacts(layer, clazz) }
            }
        }
    }
}