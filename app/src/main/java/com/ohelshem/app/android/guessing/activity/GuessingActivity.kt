package com.ohelshem.app.android.guessing.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ohelshem.app.android.MainActivity
import com.ohelshem.app.android.main.ScreenType
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.util.setMargins
import com.ohelshem.app.controller.DBController
import kotlinx.android.synthetic.main.guessing_activity.*
import org.jetbrains.anko.*
import uy.kohesive.injekt.injectLazy

class GuessingActivity : AppCompatActivity() {
    private val databaseController: DBController by injectLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guessing_activity)
        initLayout()
        notThisTimeButton.onClick {
            startMain()
        }
        neverButton.onClick {
            databaseController.guessingGameEnabled = false
            startMain()
        }
        startPlayingButton.onClick {
            startActivity<GuessingGameActivity>()
            finish()
        }
    }

    private fun startMain() {
        startActivity<MainActivity>(MainActivity.Key_Fragment to ScreenType.Changes.ordinal)
        finish()
    }

    private fun initLayout() {
        // add the padding from bottom to the layout
        if (Build.VERSION.SDK_INT >= 19) {
            val config = SystemBarTintManager(this).config
            if (Build.VERSION.SDK_INT >= 21) {
                mainLayout.setMargins(top = config.statusBarHeight + dip(40))
                if (config.isNavigationAtBottom)
                    buttonsLayout.setMargins(bottom = dip(16) + config.navigationBarHeight)
            } else {
                mainLayout.setMargins(top = config.statusBarHeight + dip(40))
                if (config.isNavigationAtBottom)
                    buttonsLayout.setMargins(bottom = dip(16) + config.navigationBarHeight)
            }
        }
        // set background
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val bmOptions = BitmapFactory.Options()
        val scaleFactor = Math.min(1080 / size.x, 1920 / size.y)
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.entrance_background, bmOptions)
        layout.background = BitmapDrawable(resources, bitmap)
        // init the spinner
    }
}