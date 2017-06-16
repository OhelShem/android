package com.ohelshem.app.android.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.Switch
import android.widget.TextView
import io.palaima.debugdrawer.actions.Action
import io.palaima.debugdrawer.actions.R

class DebugMenuSwitchAction(val name: String, val default: Boolean, val callback: (Boolean) -> Unit) : Action {
    private lateinit var context: Context
    private lateinit var switchButton: Switch

    override fun getView(linearLayout: LinearLayout): View {
        context = linearLayout.context
        val resources = context.resources

        val viewGroupLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewGroupLayoutParams.topMargin = resources.getDimensionPixelOffset(R.dimen.dd_padding_small)

        val viewGroup = LinearLayout(context)
        viewGroup.layoutParams = viewGroupLayoutParams
        viewGroup.orientation = LinearLayout.HORIZONTAL

        val textViewLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        textViewLayoutParams.rightMargin = resources.getDimensionPixelSize(R.dimen.dd_spacing_big)

        val textView = TextView(context)
        textView.layoutParams = textViewLayoutParams
        textView.text = name
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.dd_font_normal))
        textView.gravity = Gravity.CENTER_VERTICAL

        switchButton = Switch(context)
        switchButton.setOnCheckedChangeListener(switchListener)

        viewGroup.addView(textView)
        viewGroup.addView(switchButton)

        return viewGroup
    }

    override fun onOpened() {
        /* no-op */
    }

    override fun onClosed() {
        /* no-op */
    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun onStart() {
        val isChecked = default

        switchButton.setOnCheckedChangeListener(null)
        switchButton.isChecked = isChecked
        switchButton.setOnCheckedChangeListener(switchListener)
    }

    override fun onStop() {
        /* no-op */
    }

    private val switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        callback(isChecked)
    }
}
