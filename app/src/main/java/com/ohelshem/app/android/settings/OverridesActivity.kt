/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.android.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MotionEvent
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.android.primaryDarkColor
import com.ohelshem.app.android.utils.AppThemedActivity
import com.ohelshem.app.android.utils.adapter.SimpleHeaderAdapter
import com.ohelshem.app.controller.timetable.OverridableUserTimetableController
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.VisibleItem
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.overrides_activity.*
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.onClick
import org.jetbrains.anko.toast
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class OverridesActivity : AppThemedActivity() {
    private val timetableController: TimetableController by instance()
    private val weekDays by lazy { resources.getStringArray(R.array.week_days) }
    private val day by lazy { getString(R.string.day) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overrides_activity)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.save) {
                backupData()
            } else if (it.itemId == R.id.restore) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), RequestStorageReadPermission)
                else {
                    launchFileChooser()
                }
            }
            true
        }
        clearAll.onClick {
            storage.overrides = emptyList()
            updateUI()
            updateNotification()
        }
        recycler.layoutManager = LinearLayoutManager(this)
        updateUI()
    }

    fun launchFileChooser() {
        MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(RequestFileCode)
                .withFilter(Pattern.compile(".*\\.backup$"))
                .withHiddenFiles(true)
                .start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestStoragePermission) {
            if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED)
                backupData()
            else toast(R.string.permission_denied)
        } else if (requestCode == RequestStorageReadPermission) {
            if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                launchFileChooser()
            } else toast(R.string.permission_denied)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestFileCode && resultCode == Activity.RESULT_OK && data != null) {
            val path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            processFile(path)
        }
    }

    fun processFile(file: String) {
        File(file).inputStream().use {
            tempFile.createNewFile()
            FileOutputStream(tempFile).use { file ->
                it.copyTo(file)
            }
        }
        try {
            storage.importOverrideFile(tempFile)
            updateUI()
            toast(R.string.restored)
        } catch (e: Exception) {
            toast(R.string.invalid_backup_file)
        }
        tempFile.delete()

    }

    private fun backupData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RequestStoragePermission)
        } else {
            if (storage.overrides.isNotEmpty()) {
                val name = BackupFileName()
                val file = File(Environment.getExternalStorageDirectory(), name)
                if (storage.exportOverrideFile(file))
                    Snackbar.make(coordinatorLayout, file.absolutePath, Snackbar.LENGTH_LONG).setAction(R.string.share) {
                        val intent = Intent(Intent.ACTION_SEND)
                                .setType("application/octet-stream")
                                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.backup_file_name))
                                .putExtra(Intent.EXTRA_TEXT, "")
                        startActivity(Intent.createChooser(intent, getString(R.string.share)))

                    }.show()
                else
                    toast(R.string.error)
            } else {
                toast(R.string.no_overrides)
            }
        }
    }

    private fun updateUI() {
        val overrides = storage.overrides.sortedBy { it.day * 100 + it.hour }.toMutableList()
        if (overrides.isEmpty()) {
            recycler.adapter = null
            progressActivity.showEmpty(drawableRes(R.drawable.ic_list), getString(R.string.overrides), "")
        } else {
            progressActivity.showContent()
            var lastDay = -1
            val items = ArrayList<VisibleItem<Pair<OverrideData, Hour>>>(overrides.size + 6)
            for (override in overrides) {
                if (override.day != lastDay) {
                    items += VisibleItem(title = day + " " + weekDays[override.day])
                    lastDay = override.day
                }
                items += VisibleItem(override to (timetableController as OverridableUserTimetableController).timetableController[override.day, override.hour])

            }
            recycler.adapter = OverridesAdapter(this, items) {}
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder?, target: ViewHolder?): Boolean = false
                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    @Suppress("UNCHECKED_CAST")
                    val adapterItems = (recycler.adapter as OverridesAdapter).items
                    if (adapterItems.size == 2)
                        clearAll.callOnClick()
                    else {
                        overrides.removeAtIfPositive(overrides.indexOf(adapterItems[position].data!!.first))
                        storage.overrides = overrides
                        if (!adapterItems[position - 1].title.isNullOrEmpty() && (adapterItems.size <= position + 1 || !adapterItems[position + 1].title.isNullOrEmpty())) {
                            adapterItems.removeAt(viewHolder.adapterPosition - 1) // remove title
                            adapterItems.removeAt(viewHolder.adapterPosition - 1) // remove the item itself, that was pushed one back
                            recycler.adapter.notifyItemRangeRemoved(viewHolder.adapterPosition - 1, 2)
                        } else {
                            adapterItems.removeAt(viewHolder.adapterPosition)
                            recycler.adapter.notifyItemRemoved(position)
                        }
                    }
                    updateNotification()
                }

                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
                    if (viewHolder !is SimpleHeaderAdapter.HeaderViewHolder) return super.getMovementFlags(recyclerView, viewHolder)
                    else return 0
                }
            })
            itemTouchHelper.attachToRecyclerView(recycler)
        }

    }

    private fun MutableList<*>.removeAtIfPositive(position: Int) {
        if (position >= 0)
            removeAt(position)
    }

    private fun updateNotification() {
        OngoingNotificationService.update(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.overrides, menu)
        if (storage.overrides.isNotEmpty()) {
            val menuView = toolbar.getChildAt(2) as? ViewGroup
            menuView?.post {
                if (storage.firstTimeInOverridesManager) {
                    val title = getString(R.string.save_backup)
                    val view = menuView.childrenSequence().firstOrNull { it.contentDescription == title }
                    if (view != null) {
                        MaterialTapTargetPrompt.Builder(this)
                                .setPrimaryText(R.string.intro_overrides_primary_text)
                                .setSecondaryText(R.string.intro_overrides_secondary_text)
                                .setIcon(R.drawable.ic_export2)
                                .setBackgroundColour(primaryDarkColor)
                                .setIconDrawableColourFilter(primaryDarkColor)
                                .setTarget(view)
                                .setAutoFinish(true)
                                .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {
                                    override fun onHidePromptComplete() {

                                    }

                                    override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                                        storage.firstTimeInOverridesManager = false
                                    }

                                }).show()
                    }
                }
            }
        } else {
            menu.findItem(R.id.save).isVisible = false
        }
        return true
    }

    private val tempFile by lazy { File(filesDir, "temp") }


    companion object {
        private const val RequestStoragePermission = 20
        private const val RequestStorageReadPermission = 21
        private const val RequestFileCode = 21
        private const val PERMISSION_GRANTED = 0
        private fun BackupFileName() = "ohelshem_overrides_backup_${DateFormat.format(Date())}.backup"
        private val DateFormat = SimpleDateFormat("dd_MM_hh_mm")

    }
}