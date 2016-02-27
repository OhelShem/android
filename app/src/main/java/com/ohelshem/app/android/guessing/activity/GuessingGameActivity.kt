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

package com.ohelshem.app.android.guessing.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.plattysoft.leonids.ParticleSystem
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.guessing.adapter.GuessingChangesAdapter
import com.ohelshem.app.android.util.drawableRes
import com.ohelshem.app.android.util.show
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.toCalendar
import com.ohelshem.api.model.Change
import com.ohelshem.app.android.MainActivity
import uy.kohesive.injekt.injectLazy
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.guessing_activity_list.*
import java.util.*

class GuessingGameActivity : AppCompatActivity() {
    private val databaseController: DBController by injectLazy()
    private val timetableController: TimetableController by injectLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guessing_activity_list)
        val clazz = databaseController.userData.clazz
        val changes = databaseController.changes?.filter { it.clazz == clazz} ?: emptyList<Change>()
        if (changes.isEmpty()) finish()
        else {
            setSupportActionBar(toolbar)
            toolbar.navigationIcon = drawableRes(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            finishActivity.onClick { onBackPressed() }
            hoursTomorrow.text = changes.size.toString()
            changesTomorrow.text = changes.size.toString()
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = GuessingChangesAdapter(changes, timetableController[databaseController.changesDate.toCalendar()[Calendar.DAY_OF_WEEK] - 1], {
                ParticleSystem(this, 100, R.drawable.confeti2, 10000)
                        .setSpeedModuleAndAngleRange(0f, 0.3f, 180, 180)
                        .setRotationSpeed(144f)
                        .setAcceleration(0.00005f, 90)
                        .emit(topRight, 50, 2000)
                ParticleSystem(this, 100, R.drawable.confeti3, 10000)
                        .setSpeedModuleAndAngleRange(0f, 0.3f, 0, 0)
                        .setRotationSpeed(144f)
                        .setAcceleration(0.00005f, 90)
                        .emit(topLeft, 50, 2000)
                ParticleSystem(this, 100, R.drawable.confeti2, 10000)
                        .setSpeedModuleAndAngleRange(0f, 0.3f, 180, 180)
                        .setRotationSpeed(144f)
                        .setAcceleration(0.00005f, 90)
                        .emit(centerRight, 50, 2000)
                ParticleSystem(this, 100, R.drawable.confeti3, 10000)
                        .setSpeedModuleAndAngleRange(0f, 0.3f, 0, 0)
                        .setRotationSpeed(144f)
                        .setAcceleration(0.00005f, 90)
                        .emit(centerLeft, 50, 2000)
            }) {
                showEnding()
                Handler().apply {
                    postDelayed({ showEnding() }, 400)
                    postDelayed({ showEnding() }, 800)
                    postDelayed({ showEnding() }, 1200)
                    postDelayed({ showEnding() }, 1600)
                    postDelayed({ showEnding() }, 2000)
                    postDelayed({ showEnding() }, 2400)
                    postDelayed({ showEnding() }, 2800)
                    postDelayed({ showEnding() }, 3200)
                    postDelayed({ finishActivity.show() }, 3200)
                }
            }
        }
    }

    private fun showEnding() {
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(topLeft, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(topRight, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(centerLeft, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(centerRight, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(bottomLeft, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(bottomRight, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(centerTop, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(center, 100)
        ParticleSystem(this, 100, R.drawable.star_pink, 1200)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(centerBottom, 100)
    }

    override fun onBackPressed() {
        startActivity<MainActivity>()
        finish()
    }
}