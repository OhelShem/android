package com.ohelshem.app.android.contacts.birthdays

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.app.android.flipName
import com.ohelshem.app.android.stringArrayRes
import com.ohelshem.app.dateFormat
import com.ohelshem.app.getIsraelCalendar
import com.ohelshem.app.model.Contact
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.birthday_item.view.*
import org.jetbrains.anko.layoutInflater

class BirthdaysAdapter(val context: Context, val contacts: List<Contact>) : RecyclerView.Adapter<BirthdaysAdapter.VH>() {
    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name.flipName() + " (${context.stringArrayRes(R.array.layers)[contact.layer - 9] + "'" + contact.clazz})"
        val cal = getIsraelCalendar()
        cal.timeInMillis = contact.birthday
        holder.birthday.hint = dateFormat.format(cal.time)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(context.layoutInflater.inflate(R.layout.birthday_item, parent, false))
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = itemView.birthdayName
        val birthday: TextView = itemView.birthdayClass
    }
}