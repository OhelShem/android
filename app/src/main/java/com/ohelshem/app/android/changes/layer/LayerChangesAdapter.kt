package com.ohelshem.app.android.changes.layer

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.screenSize
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.layoutInflater

class LayerChangesAdapter(context: Context, val changes: List<Change>, val classes: Int, val maxChanges: Int, val headerPrefix: String) : RecyclerView.Adapter<LayerChangesAdapter.VH>() {
    private val items = Array(classes) { clazz -> Array(maxChanges) { hour -> changes.firstOrNull { it.clazz == clazz + 1 && it.hour == hour + 1 } } }
    private val rows = maxChanges + 1
    private val count = classes * rows
    private val standardColumnWidth = context.screenSize.x / 6

    override fun getItemCount(): Int = count

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.textView.layoutParams = holder.textView.layoutParams.apply { width = standardColumnWidth }

        val col = col(position)
        val row = row(position)

        val type = getItemViewType(position)
        if (type == Type_Header) {
            holder.textView.text = "$headerPrefix${col + 1}"
        } else {
            try {
                val change: Change? = items[col][row - 1]
                holder.itemView.setBackgroundColor(if (change == null) NoChangesColor[row % 2] else change.color)
                holder.textView.text = change?.content ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (viewType == Type_Header) return VH(parent.context.layoutInflater.inflate(R.layout.layer_changes_title, parent, false))
        else return VH(parent.context.layoutInflater.inflate(R.layout.layer_changes_item, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return if (row(position) == 0) Type_Header
        else Type_Item
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view as TextView
    }

    private fun row(position: Int) = position % rows
    private fun col(position: Int) = position / rows

    companion object {
        private const val Type_Header = 2
        private const val Type_Item = 3
        private val NoChangesColor = arrayOf(Color.parseColor("#D9D9D9"), Color.parseColor("#BABABA"))
    }
}