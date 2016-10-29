/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.changes.layer

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.changes.BaseChangesFragment
import com.ohelshem.app.android.changes.LayerChangesGenerator
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.android.stringArrayRes
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.list_fragment.*
import org.jetbrains.anko.padding
import org.jetbrains.anko.support.v4.toast
import java.io.File

class LayerChangesFragment : BaseChangesFragment<LayerChangesPresenter>() {
    override val layoutId: Int = R.layout.layer_changes_fragment
    override var menuId: Int = R.menu.changes

    override fun createPresenter(): LayerChangesPresenter = with(kodein()) { LayerChangesPresenter(instance(), instance(), instance()) }

    override fun init() {
        recyclerView.padding = 0
        recyclerView.layoutManager = GridLayoutManager(activity, MaxChangesHours + 1, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL).apply { setDrawable(drawableRes(R.drawable.divider_white)!!) })
        recyclerView.setHasFixedSize(true)
    }

    override fun showData(changes: List<Change>) {
        recyclerView.adapter = LayerChangesAdapter(context, changes, presenter.classesAtLayer, MaxChangesHours, stringArrayRes(R.array.layers)[presenter.userLayer - 9])
        recyclerView.scrollToPosition(0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.share).setOnMenuItemClickListener {
            if (isShowingData) {
                if (!isSharing) {
                    isSharing = true
                    val file = File(File(context.filesDir, MainActivity.SharingFolder).apply { mkdirs() }, SharingFilename)
                    val uri = Uri.parse("content://${context.packageName}/${MainActivity.SharingFolder}/$SharingFilename")
                    toast(R.string.generating_data)
                    LayerChangesGenerator.generateLayerChanges(context, (recyclerView.adapter as LayerChangesAdapter).changes, presenter.classesAtLayer, presenter.userLayer, file) {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                .setDataAndType(uri, "image/png")
                                .putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                        isSharing = false
                    }
                }
            } else {
                toast(R.string.no_changes)
            }
            true
        }
    }

    private var isSharing: Boolean = false

    companion object {
        private const val SharingFilename = "layer_changes.png"
        private const val MaxChangesHours = 11
    }
}