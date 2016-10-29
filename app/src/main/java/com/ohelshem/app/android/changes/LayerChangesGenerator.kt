package com.ohelshem.app.android.changes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.*
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.*
import java.io.File

object LayerChangesGenerator {
    private const val MaxChangeHours = 11
    private val NoChangesColors = intArrayOf(Color.parseColor("#D9D9D9"), Color.parseColor("#BABABA"))

    fun createView(context: Context, classes: Int, layer: Int): View {
        with (context) {
            val headerRowHeight = dip(30)
            val defaultCellMargin = dip(1)
            val standardColumnWidth = screenSize.x / 6
            val layerText = stringArrayRes(R.array.layers)[layer - 9]

           return UI {
                linearLayout {
                    gravity = Gravity.RIGHT
                    orientation = LinearLayout.VERTICAL

                    // Header row:
                    linearLayout {
                        orientation = LinearLayout.HORIZONTAL
                        backgroundColor = primaryColor

                        repeat(classes) { c ->
                            val clazz = classes - c
                            textView {
                                gravity = Gravity.CENTER
                                textColor = Color.WHITE
                                padding = 10
                                text = "$layerText'$clazz"

                                setTypeface(null, Typeface.BOLD)
                            }.lparams(width = standardColumnWidth, height = matchParent) {
                                marginize(clazz, classes, 0, MaxChangeHours, defaultCellMargin)
                            }

                        }
                    }.lparams(width = matchParent, height = headerRowHeight)

                    // Changes rows
                    repeat(MaxChangeHours) { hour ->
                        linearLayout {
                            orientation = LinearLayout.HORIZONTAL

                            repeat(classes) { c ->
                                val clazz = classes - c
                                autoResizeTextView {
                                    gravity = Gravity.CENTER
                                    textColor = Color.WHITE
                                    backgroundColor = NoChangesColors[hour % 2]
                                    padding = 10

                                }.lparams(width = standardColumnWidth, height = matchParent) {
                                    marginize(clazz, classes, hour, MaxChangeHours, defaultCellMargin)
                                }
                            }

                        }.lparams(width = matchParent, height = 0) {
                            weight = 1f
                        }
                    }
                }
            }.view
        }
    }

    fun generateLayerChanges(context: Context, changes: List<Change>, classes: Int, layer: Int, path: File, callback: (File) -> Unit): Boolean {
        if (changes.isEmpty()) return false
        doAsync {
            with(context) {
                val defaultCellMargin = dip(1)
                val screenSize = screenSize
                val standardColumnWidth = screenSize.x / 6
                val width = standardColumnWidth * classes + (defaultCellMargin * classes - 1) + dip(5)
                val height = screenSize.y - dip(56 + 25) // FIXME

                val view = createView(context, classes, layer)
                val rows = view.childrenSequence().drop(1).map { it as LinearLayout }.toList()
                fillTable(changes, classes, rows)

                try {
                    val bitmap = takeScreenShot(view, width, height)
                    path.outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, it)
                    }
                    runOnUiThread {
                        callback(path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    //region Helper drawing
    fun fillTable(changes: List<Change>, classes: Int, rows: List<LinearLayout>) {
        changes.forEach {
            (rows[it.hour - 1][classes - it.clazz] as TextView).apply {
                backgroundColor = it.color
                text = it.content
            }
        }
    }

    private fun ViewGroup.MarginLayoutParams.marginize(clazz: Int, maxClasses: Int, hour: Int, maxHours: Int, margin: Int) {
        if (hour != 0) {
            topMargin = margin
        }
        if (hour != maxHours - 1) {
            bottomMargin = margin
        }

        if (clazz != maxClasses) {
            leftMargin = margin
        }
        if (clazz != 0) {
            rightMargin = margin
        }
    }
    //endregion

    private fun takeScreenShot(view: View, width: Int, height: Int): Bitmap {
        return getBitmapFromView(view, height, width)
    }

    fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)

        view.measure(
                View.MeasureSpec.makeMeasureSpec(totalWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(totalHeight, View.MeasureSpec.EXACTLY))

        view.layout(0, 0, totalWidth, totalHeight)
        view.draw(canvas)
        return returnedBitmap
    }
}