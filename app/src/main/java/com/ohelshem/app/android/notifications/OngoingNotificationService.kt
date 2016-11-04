package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.NotificationCompat
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.RemoteViews
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.getDay
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService
import java.util.*

class OngoingNotificationService : IntentService("OhelShemOngoingNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val timetableController: TimetableController by kodein.instance()

    override fun onHandleIntent(intent: Intent?) {
        val cal = Calendar.getInstance()
        val day = cal.getDay()
        if (storage.isSetup()) {
            if (storage.notificationsForTimetable && day != Calendar.SATURDAY && cal[Calendar.HOUR_OF_DAY] >= 8) {
                val data = timetableController.getHourData(day)
                if (data.hour.day != day) {
                    // Day has ended
                    notificationManager.cancel(NotificationId)
                } else {
                    if (data.hour.day != day) {
                        // Day has ended
                        notificationManager.cancel(NotificationId)
                    } else {

                        var color: Int? = null
                        var nextColor: Int? = null

                        var lessonName = ""
                        var isChange = false
                        storage.changes?.forEach {
                            if (it.clazz==storage.userData.clazz && it.hour-1 == data.hour.hourOfDay) {
                                lessonName = it.content
                                color = it.color
                                isChange = true
                            }
                        }
                        if (!isChange)
                            lessonName = if (data.hour.isEmpty()) getString(R.string.window_lesson) else data.hour.name



                        var nextLesson = ""
                        val isEndOfDay = TimetableController.isEndOfDay(data.hour.hourOfDay, timetableController[data.hour.day - 1])
                        if (isEndOfDay) {
                            nextLesson = getString(R.string.end_of_day)
                        } else {
                            var isNextChange = false
                            storage.changes?.forEach {
                                if (it.clazz == storage.userData.clazz && it.hour - 1 == data.nextHour.hourOfDay) {
                                    nextLesson = it.content
                                    nextColor = it.color
                                    isNextChange = true
                                }
                            }
                            if (!isNextChange)
                                nextLesson = if (data.nextHour.isEmpty()) getString(R.string.window_lesson) else data.nextHour.name
                        }


                        val hours = TimetableController.DayHours[data.hour.hourOfDay*2] + " - " + TimetableController.DayHours[data.hour.hourOfDay*2+1]

                        try {
                            notificationManager.notify(NotificationId, createNotification(lessonName, hours, nextLesson, color, nextColor))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
            } else notificationManager.cancel(NotificationId)
        } else notificationManager.cancel(NotificationId)
    }

    companion object {
        private const val NotificationId = 1342

        fun update(context: Context) {
            context.startService<OngoingNotificationService>()
        }

        fun toBold(text: String): SpannableString {
            val s = SpannableString(text)
            s.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
            return s
        }

        private fun Context.createNotification(lesson: String, hours: String, nextLesson: String? = null, color: Int? = null, nextColor: Int? = null): Notification {

            val contentView: RemoteViews = RemoteViews(packageName, R.layout.notification_view)

            contentView.setTextColor(R.id.timeLeft, Color.WHITE)
            contentView.setTextColor(R.id.lessonName, Color.WHITE)
            contentView.setTextColor(R.id.nextLessonName, Color.WHITE)

            contentView.setTextViewText(R.id.timeLeft, toBold(hours))
            contentView.setTextViewText(R.id.lessonName, toBold(lesson))
            contentView.setTextViewText(R.id.nextLessonName, toBold(nextLesson!!))

            contentView.setFloat(R.id.timeLeft, "setTextSize", 14f)
            contentView.setFloat(R.id.lessonName, "setTextSize", 14f)
            contentView.setFloat(R.id.nextLessonName, "setTextSize", 14f)

            contentView.setInt(R.id.mainNotifView, "setBackgroundColor", Color.parseColor("#03A9F4"))

            if (color!=null)
                contentView.setInt(R.id.lessonName, "setBackgroundColor", color)
            if (nextColor!=null)
                contentView.setInt(R.id.nextLessonName, "setBackgroundColor", nextColor)


            contentView.setImageViewResource(R.id.notifLogo, R.drawable.logo)
            contentView.setImageViewResource(R.id.hourIcon, R.drawable.ic_alarm)
            contentView.setImageViewResource(R.id.nextHourIcon, R.drawable.ic_arrow_forward)

            val intent = Intent(applicationContext, MainActivity::class.java)
            val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            return NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentTitle(lesson)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pIntent)
                    .setCustomBigContentView(contentView)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build()
        }
    }
}