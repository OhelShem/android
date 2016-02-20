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

package com.ohelshem.app.android.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.adapter.OverridesAdapter
import com.ohelshem.app.android.adapter.SimpleHeaderAdapter.HeaderViewHolder
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.service.OngoingNotificationService
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.controller.TimetableControllerWrapper
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.VisibleItem
import com.ohelshem.api.model.Hour
import kotlinx.android.synthetic.main.overrides_activity.clearAll
import kotlinx.android.synthetic.main.overrides_activity.progressActivity
import kotlinx.android.synthetic.main.overrides_activity.recycler
import kotlinx.android.synthetic.main.overrides_activity.toolbar
import org.jetbrains.anko.*
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class OverridesActivity : AppCompatActivity() {
    private val timetableController: TimetableController by injectLazy()
    private val databaseController: DBController by injectLazy()
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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), RequestStorageReadPermission)
                else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).setType("file/*")
                    if (packageManager.queryIntentActivities(intent, 0).size > 0)
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_opener)), RequestFileCode)
                    else toast(R.string.no_file_manager_installed)
                }
            }
            true
        }
        clearAll.onClick {
            databaseController.overrides = emptyArray()
            updateUI()
            updateNotification()
        }
        recycler.layoutManager = LinearLayoutManager(this)
        updateUI()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestStoragePermission) {
            if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED)
                backupData()
            else toast(R.string.permission_denied)
        } else if (requestCode == RequestStorageReadPermission) {
            if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).setType("file/*")
                if (packageManager.queryIntentActivities(intent, 0).size > 0)
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_opener)), RequestFileCode)
                else toast(R.string.no_file_manager_installed)
            } else toast(R.string.permission_denied)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestFileCode && resultCode == Activity.RESULT_OK && data != null) {
            contentResolver.openInputStream(data.data).use {
                tempFile.createNewFile()
                FileOutputStream(tempFile).use { file ->
                    it.copyTo(file)
                }
            }
            try {
                databaseController.importOverrideFile(tempFile)
                updateUI()
                toast(R.string.restored)
            } catch (e: Exception) {
                toast(R.string.invalid_backup_file)
            }
            tempFile.delete()
        }
    }

    private fun backupData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RequestStoragePermission)
        } else {
            val name = BackupFileName()
            if (databaseController.exportOverrideFile(File(Environment.getExternalStorageDirectory(), name)))
                toast(getString(R.string.backup_file_saved_on) + name)
            else
                toast(R.string.error)
        }
    }

    private fun updateUI() {
        val overrides = databaseController.overrides.sortedBy { it.day * 100 + it.hour }.toMutableList()
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
                items += VisibleItem(override to (timetableController as TimetableControllerWrapper).timetableController[override.day, override.hour])

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
                        databaseController.overrides = overrides.toTypedArray()
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
                    if (viewHolder !is HeaderViewHolder) return super.getMovementFlags(recyclerView, viewHolder)
                    else return 0
                }
            })
            itemTouchHelper.attachToRecyclerView(recycler)
        }

    }

    private fun MutableList<*>.removeAtIfPositive(position: Int) {
        if (position > 0)
            removeAt(position)
    }

    private fun updateNotification() {
        OngoingNotificationService.update(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.overrides, menu)
        return true
    }

    companion object {
        private const val RequestStoragePermission = 20
        private const val RequestStorageReadPermission = 21
        private const val RequestFileCode = 21
        private const val PERMISSION_GRANTED = 0
        private fun BackupFileName() = "ohelshem_overrides_backup_${DateFormat.format(Date())}.backup"
        private val tempFile = File("/data/data/com.yoavst.changesystemohelshem/files/temp")
        private val DateFormat = SimpleDateFormat("dd_MM_hh_mm")

    }
}