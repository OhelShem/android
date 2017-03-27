package com.ohelshem.app.controller.analytics

import android.content.Context
import android.support.v7.app.AppCompatDelegate.MODE_NIGHT_NO
import android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.ohelshem.app.controller.storage.SharedStorage
import org.jetbrains.anko.bundleOf
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class FirebaseAnalyticsManager(val storage: SharedStorage, context: Context) : Analytics {
    val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()

    init {
        if (storage.isSetup())
            onLogin()
    }

    override fun onLogin() {
        firebaseAnalytics.setUserId(sha1(storage.id + Salt))
        firebaseAnalytics.setUserProperty(LayerProperty, storage.userData.layer.toString())
        firebaseAnalytics.setUserProperty(ClassProperty, storage.userData.clazz.toString())
        firebaseAnalytics.setUserProperty(NightModeProperty, storage.darkMode.let {
            if (it == MODE_NIGHT_NO) "MODE_NIGHT_NO" else if (it == MODE_NIGHT_YES) "MODE_NIGHT_YES" else "MODE_NIGHT_AUTO"
        })
        firebaseAnalytics.setUserProperty(ThemeColorProperty, storage.theme.toString())

        // for remote messages
        firebaseMessaging.unsubscribeFromTopic("layer9")
        firebaseMessaging.unsubscribeFromTopic("layer10")
        firebaseMessaging.unsubscribeFromTopic("layer11")
        firebaseMessaging.unsubscribeFromTopic("layer12")
        firebaseMessaging.unsubscribeFromTopic("layer13")
        firebaseMessaging.unsubscribeFromTopic("allstudents")
        firebaseMessaging.subscribeToTopic("layer" + storage.userData.layer.toString())
        if (storage.isStudent()) firebaseMessaging.subscribeToTopic("allstudents")

        if (storage.notificationsForChanges)
            subscribe()
    }

    override fun subscribe() {
        // for changes
        firebaseMessaging.subscribeToTopic("notifSub")
    }

    override fun unsubscribe() {
        firebaseMessaging.unsubscribeFromTopic("notifSub")
    }

    override fun unsubscribeMessages() {
        firebaseMessaging.unsubscribeFromTopic("layer" + storage.userData.layer.toString())
        firebaseMessaging.unsubscribeFromTopic("allstudents")
    }

    override fun onLogout() {
        firebaseAnalytics.setUserId(null)
        firebaseAnalytics.setUserProperty(LayerProperty, null)
        firebaseAnalytics.setUserProperty(ClassProperty, null)
        firebaseAnalytics.setUserProperty(ThemeColorProperty, null)
        firebaseAnalytics.setUserProperty(NightModeProperty, null)
        unsubscribe()
        unsubscribeMessages()
    }

    override fun logEvent(type: String, info: Map<String, Any>) {
        firebaseAnalytics.logEvent(type, bundleOf(*info.map { it.toPair() }.toTypedArray()))
    }

    companion object {
        private const val Salt = "dyIVuLoEih"

        private const val LayerProperty = "layer"
        private const val ClassProperty = "class"
        private const val ThemeColorProperty = "themeColor"
        private const val NightModeProperty = "themeMode"

        fun sha1(toHash: String): String? {
            var hash: String? = null
            try {
                val digest = MessageDigest.getInstance("SHA-1")
                var bytes = toHash.toByteArray(charset("UTF-8"))
                digest.update(bytes, 0, bytes.size)
                bytes = digest.digest()
                hash = bytesToHex(bytes)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            return hash
        }

        // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
        private val hexArray = "0123456789ABCDEF".toCharArray()

        fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v.ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}
