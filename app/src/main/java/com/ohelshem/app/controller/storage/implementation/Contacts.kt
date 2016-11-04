package com.ohelshem.app.controller.storage.implementation

import android.content.Context
import com.ohelshem.app.controller.serialization.ContactSerialization
import com.ohelshem.app.controller.serialization.SimpleReader
import com.ohelshem.app.controller.serialization.ofList
import com.ohelshem.app.controller.serialization.simpleReader
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
                else -> throw IllegalArgumentException("No Such layer")
            }
            context.resources.openRawResource(resource).simpleReader()
        }
    }

    fun init(generator: (Int) -> SimpleReader) {
        this.generator = generator
    }

    private val serialization = ContactSerialization.ofList()
    override fun getContacts(layer: Int, clazz: Int): List<Contact> {
        generator(layer).use { reader ->
            if (clazz == -1) {
                return serialization.deserialize(reader)
            } else {
                return serialization.deserialize(reader).filter { it.clazz == clazz } // TODO improve performance
            }
        }
    }
}