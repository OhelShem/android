package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.NotificationCompat
import android.widget.RemoteViews
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.android.primaryColor
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
            if (storage.notificationsForTimetable && day != Calendar.SATURDAY) {
                var data = timetableController.getHourData(day)
                if (data.hour.day != day) {
                    // Day has ended
                    notificationManager.cancel(NotificationId)
                } else {
                    if ((data.isBefore || data.timeToHour <= 5) && cal[Calendar.HOUR_OF_DAY] >= 8) {
                        cal.add(Calendar.MINUTE, data.timeToHour + 1)
                        data = timetableController.getHourData(day, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE])
                    }
                    if (data.hour.day != day) {
                        // Day has ended
                        notificationManager.cancel(NotificationId)
                    } else {
                        val hours = TimetableController.DayHours[data.hour.hourOfDay * 2] + " - " + TimetableController.DayHours[data.hour.hourOfDay * 2 + 1]
                        val name = if (data.hour.isEmpty()) getString(R.string.window_lesson) else data.hour.name
                        if (data.nextHour.day != day || TimetableController.isEndOfDay(data.hour.hourOfDay, timetableController[data.hour.day]))
                            notificationManager.notify(NotificationId, createNotification(name, hours, getString(R.string.end_of_day)))
                        else notificationManager.notify(NotificationId,
                                createNotification(name, hours, if (data.nextHour.isEmpty()) getString(R.string.window_lesson) else data.nextHour.name, if (data.nextHour.isEmpty()) 0 else data.progress))
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

        private fun Context.createNotification(lesson: String, hours: String, nextLesson: String? = null, progress: Int?=null): Notification {

            val contentView: RemoteViews = RemoteViews(packageName, R.layout.notification_view)
            contentView.setTextColor(R.id.timeLeft, Color.GRAY)
            contentView.setTextColor(R.id.lessonName, Color.BLACK)
            contentView.setTextColor(R.id.nextLessonName, Color.BLACK)

            contentView.setTextViewText(R.id.timeLeft, hours)
            contentView.setTextViewText(R.id.lessonName, lesson)
            contentView.setTextViewText(R.id.nextLessonName, nextLesson)

            contentView.setFloat(R.id.timeLeft, "setTextSize", 14f)
            contentView.setFloat(R.id.lessonName, "setTextSize", 14f)
            contentView.setFloat(R.id.nextLessonName, "setTextSize", 14f)

            contentView.setInt(R.id.progress, "setBackgroundColor", Color.GRAY)
            contentView.setInt(R.id.mainNotifView, "setBackgroundColor", primaryColor)

            contentView.setImageViewResource(R.id.hourIcon, R.drawable.ic_alarm)
            contentView.setImageViewResource(R.id.nextHourIcon, R.drawable.ic_arrow_forward)

            if (progress!=null)
                contentView.setProgressBar(R.id.progress, 45, progress, false)


            val intent = Intent(applicationContext, MainActivity::class.java)
            val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            return NotificationCompat.Builder(this)
                    .setAutoCancel(false)
                    .setContentTitle(lesson)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pIntent)
                    .setCustomBigContentView(contentView)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build()
        }
    }
}