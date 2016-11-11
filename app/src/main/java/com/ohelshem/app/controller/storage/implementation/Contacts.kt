package com.ohelshem.app.controller.storage.implementation

import android.content.Context
import com.ohelshem.app.controller.serialization.*
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.model.Contact
import com.yoavst.changesystemohelshem.R

object Contacts : ContactsProvider {
    private lateinit var generator: (Int) -> SimpleReader

    fun init(context: Context) {
        this.generator = {
            val resource = when (it) {
                9 -> R.raw.contacts9
                10 -> R.raw.contacts10
                11 -> R.raw.contacts11
                12 -> R.raw.contacts12
                else -> throw IllegalArgumentException("No Such layer: " + it)
            }
            context.resources.openRawResource(resource).simpleReader()
        }
    }

    fun init(generator: (Int) -> SimpleReader) {
        this.generator = generator
    }

    private val serialization = ContactSerialization.ofList()
    override fun getContacts(layer: Int, clazz: Int): List<Contact> {
        if (layer==-1) {
            var theList: List<Contact> = emptyList()
            generator(9).use { reader ->
                theList+= serialization.deserialize(reader)
            }
            generator(10).use { reader ->
                theList+= serialization.deserialize(reader)
            }
            generator(11).use { reader ->
                theList+= serialization.deserialize(reader)
            }
            generator(12).use { reader ->
                theList+= serialization.deserialize(reader)
            }
            return theList
        } else {
            generator(layer).use { reader ->
                if (clazz == -1) {
                    return serialization.deserialize(reader)
                } else {
                    return serialization.filter { it.clazz == clazz }.deserialize(reader)
                }
            }
        }
    }
}