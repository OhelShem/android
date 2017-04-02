/*
 * Copyright 2010-2015 Yoav Sternberg.
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

package com.ohelshem.app.android.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ohelshem.app.android.main.MainActivity
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.help_activity.*
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView
import org.jetbrains.anko.sdk15.listeners.onClick
import java.io.File
import java.io.FileOutputStream

class HelpActivity : AppCompatActivity() {
    val usedPadding by lazy { dip(8) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_activity)
        initLayout()
        initViews()
    }

    private fun initLayout() {
        questionsList.apply {
            val questions = resources.getStringArray(R.array.questions)
            val answers = resources.getStringArray(R.array.answers)
            questions.forEachIndexed { i, s ->
                questionItem(s, answers[i])
            }
        }
    }

    private fun initViews() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        contactButton.onClick {
            email("yoav.sternberg@gmail.com", subject = getString(R.string.email_title))
        }
        regulationButton.onClick {
            openRegulations()
        }
        eranButton.onClick {
            browse("http://www.eran.org.il/")
        }
        councilButton.onClick {
            browse("http://www.children.org.il/centers.asp?id=14")
        }
        eliButton.onClick {
            browse("http://www.eli.org.il")
        }
        mashovButton.onClick {
            launchPlayStore("com.yoavst.mashov")
        }
        quickAppsButton.onClick {
            launchPlayStore("com.yoavst.quickapps")
        }
        yoavCard.onClick {
            browse("http://yoavst.com")
        }
        eranCard.onClick {
            browse("https://play.google.com/store/apps/dev?id=6478089169543445792")
        }
        layerCounselorButton.onClick {
            toast(R.string.not_yet_implemented)
        }
    }

    fun LinearLayout.questionItem(title: String, text: String): CardView {
        return customView<CardView> {
            setContentPadding(usedPadding, usedPadding, usedPadding, usedPadding)
            useCompatPadding = true
            linearLayout {
                orientation = LinearLayout.VERTICAL
                include<TextView>(R.layout.item_header) {
                    setText(title + "?")
                    textSize = 16f
                }

                textView(text) {

                }.lparams(width = matchParent, height = wrapContent)
            }
        }.apply {
            (layoutParams as ViewGroup.MarginLayoutParams).apply { setMargins(usedPadding, usedPadding, usedPadding, 0) }.let { layoutParams = it }
        }
    }

    private fun launchPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
        }
    }

    private
    val RegulationFile by lazy { File(File(filesDir, MainActivity.SharingFolder).apply { mkdirs() }, RegulationFilename) }
    private val RegulationFilename = "regulation.pdf"

    private fun openRegulations() {
        if (!RegulationFile.exists()) {
            resources.openRawResource(R.raw.regulations).use { regulationStream ->
                RegulationFile.createNewFile()
                FileOutputStream(RegulationFile).use {
                    regulationStream.copyTo(it)
                }
            }
        }
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse("content://$packageName/${MainActivity.SharingFolder}/$RegulationFilename"), "application/pdf").setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            toast("No PDF reader installed")
        } else startActivity(Intent.createChooser(intent, getString(R.string.choose_opener)))
    }
}