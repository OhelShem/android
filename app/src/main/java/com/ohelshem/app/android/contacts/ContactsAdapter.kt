package com.ohelshem.app.android.contacts

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.ContactsContract.Intents.Insert
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.android.toFullName
import com.ohelshem.app.model.Contact
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.contact_item.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import org.jetbrains.anko.sendSMS
import java.text.SimpleDateFormat
import java.util.*

class ContactsAdapter(val context: Context, val contacts: List<Contact>, val dial: (Contact) -> Unit) : RecyclerView.Adapter<ContactsAdapter.VH>() {
    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name.toFullName()
        holder.birthday.hint = dateFormat.format(Date(contact.birthday))
        if (contact.phone.isEmpty()) {
            holder.dial.hide()
            holder.menu.hide()
        } else {
            holder.dial.show()
            holder.menu.show()

            holder.dial.onClick { dial(contact) }
            holder.menu.onClick {
                val menu = PopupMenu(context, it!!)
                menu.inflate(R.menu.contact)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.sendSms -> context.sendSMS(contact.phone)
                        R.id.addContact -> {
                            val intent = Intent(Intent.ACTION_INSERT)
                                    .setType(ContactsContract.Contacts.CONTENT_TYPE)
                                    .putExtra(Insert.NAME, contact.name.toFullName())
                                    .putExtra(Insert.PHONE, contact.phone)

                            val format = SimpleDateFormat("yyyy-MM-dd")
                            val data = arrayListOf(ContentValues().apply {
                                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                                put(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
                                put(ContactsContract.CommonDataKinds.Event.START_DATE, format.format(Date(contact.birthday)))
                            })
                            intent.putParcelableArrayListExtra(Insert.DATA, data)

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }

                        }
                    }
                    true
                }
                menu.show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(context.layoutInflater.inflate(R.layout.contact_item, parent, false))
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val menu: View = itemView.menu
        val dial: View = itemView.dial
        val name: TextView = itemView.name
        val birthday: TextView = itemView.birthday

    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
}