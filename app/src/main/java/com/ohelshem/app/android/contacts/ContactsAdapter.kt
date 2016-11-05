package com.ohelshem.app.android.contacts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.app.model.Contact
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.contact_item.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import java.text.SimpleDateFormat
import java.util.*

class ContactsAdapter(val contacts: List<Contact>, val dial: (Contact) -> Unit): RecyclerView.Adapter<ContactsAdapter.VH>() {
    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.birthday.text = dateFormat.format(Date(contact.birthday))
        holder.dial.onClick { dial(contact) }
        //FIXME add context menu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.context.layoutInflater.inflate(R.layout.contact_item, parent, false))
    }

    class VH(view: View): RecyclerView.ViewHolder(view) {
        val menu: View = itemView.menu
        val dial: View = itemView.dial
        val name: TextView = itemView.name
        val birthday: TextView = itemView.birthday

    }

    val dateFormat = SimpleDateFormat("dd/MM/yy")
}