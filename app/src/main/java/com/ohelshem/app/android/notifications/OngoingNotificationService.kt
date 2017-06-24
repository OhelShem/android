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
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.android.nameOriginalHour
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.getDay
import com.ohelshem.app.model.NumberedHour
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService
import java.util.*

class OngoingNotificationService : IntentService("OhelShemOngoingNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val timetableController: TimetableController by kodein.instance()

    private val with by lazy { " " + getString(R.string.with) + " " }

    override fun onHandleIntent(intent: Intent?) {
        val cal = Calendar.getInstance()
        val day = cal.getDay()
        if (storage.isSetup()) {
            val holiday = TimetableController.getHoliday()
            if (timetableController.hasData && storage.notificationsForTimetable && storage.ongoingNotificationDisableDate != Calendar.getInstance().clearTime().timeInMillis && holiday == null && day != Calendar.SATURDAY && ((cal[Calendar.HOUR_OF_DAY] >= 7 && cal[Calendar.MINUTE] >= 55) || cal[Calendar.HOUR_OF_DAY] >= 8)) {
                val data = timetableController.getHourData(day)
                if (data.hour.day != day) {
                    // Day has ended
                    notificationManager.cancel(NotificationId)
                } else {
                    var color: Int? = null
                    var nextColor: Int? = null
                    var orig: String? = null
                    var orig2: String? = null

                    var lessonName = ""
                    var lessonTeacherName = ""
                    var isChange = false
                    storage.changes?.forEach {
                        if (it.clazz == storage.userData.clazz && it.hour - 1 == data.hour.hourOfDay) {
                            lessonName = it.content
                            color = it.color
                            orig = data.hour.represent(false)
                            isChange = true
                        }
                    }
                    if (!isChange) {
                        lessonName = data.hour.represent()
                        lessonTeacherName = data.hour.teacher
                    }


                    var nextLesson = ""
                    var nextLessonTeacher = ""
                    val isEndOfDay = TimetableController.isEndOfDay(data.hour.hourOfDay, timetableController[data.hour.day - 1])
                    if (isEndOfDay) {
                        nextLesson = getString(R.string.end_of_day)
                    } else {
                        var isNextChange = false
                        storage.changes?.forEach {
                            if (it.clazz == storage.userData.clazz && it.hour - 1 == data.nextHour.hourOfDay) {
                                nextLesson = it.content
                                nextColor = it.color
                                orig2 = data.nextHour.represent(false)
                                isNextChange = true
                            }
                        }
                        if (!isNextChange) {
                            nextLesson = data.nextHour.represent()
                            nextLessonTeacher = data.nextHour.teacher
                        }
                    }


                    val hours = TimetableController.DayHours[data.hour.hourOfDay * 2] + " - " + TimetableController.DayHours[data.hour.hourOfDay * 2 + 1]

                    try {
                        notificationManager.notify(NotificationId, createNotification(lessonName, lessonTeacherName, hours, nextLesson, nextLessonTeacher, color, nextColor, orig, orig2))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else notificationManager.cancel(NotificationId)
        } else notificationManager.cancel(NotificationId)
        stopSelf()
    }

    private fun NumberedHour.represent(showRoom: Boolean = true) = if (isEmpty()) getString(R.string.window_lesson) else if (room != 0 && room != -1 && showRoom) "$name ($room)" else name

    fun toBold(text: String, orig: String? = null, teacherName: String? = null): SpannableString {
        val s: SpannableString?

        if (orig != null)
            s = SpannableString(text + " " + nameOriginalHour(text, orig))
        else {
            if (!storage.isStudent() && teacherName != null) {
                s = SpannableString(text + if (teacherName.isNotEmpty()) with + teacherName else "")
            } else {
                s = SpannableString(text)
            }
        }
        s.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        return s
    }

    private fun Context.createNotification(lesson: String, teacherName: String? = null, hours: String, nextLesson: String? = null, nextTeacherName: String? = null, color: Int? = null, nextColor: Int? = null, orig: String? = null, orig2: String? = null): Notification {

        val contentView: RemoteViews = RemoteViews(packageName, R.layout.notification_view)

        //reset colors
        contentView.setInt(R.id.currentLesson, "setBackgroundColor", Color.parseColor("#03A9F4"))
        contentView.setInt(R.id.next_lesson, "setBackgroundColor", Color.parseColor("#03A9F4"))

        contentView.setTextColor(R.id.timeLeft, Color.WHITE)
        contentView.setTextColor(R.id.lessonName, Color.WHITE)
        contentView.setTextColor(R.id.nextLessonName, Color.WHITE)

        contentView.setTextViewText(R.id.timeLeft, toBold(hours))
        contentView.setTextViewText(R.id.lessonName, if (teacherName != null) toBold(lesson, orig, teacherName) else toBold(lesson, orig))
        contentView.setTextViewText(R.id.nextLessonName, if (nextTeacherName != null) toBold(nextLesson!!, orig2, nextTeacherName) else toBold(nextLesson!!, orig2))

        contentView.setFloat(R.id.timeLeft, "setTextSize", 14.5f)
        contentView.setFloat(R.id.lessonName, "setTextSize", 14.5f)
        contentView.setFloat(R.id.nextLessonName, "setTextSize", 14.5f)

        contentView.setInt(R.id.mainNotifView, "setBackgroundColor", Color.parseColor("#03A9F4"))

        if (color != null)
            contentView.setInt(R.id.currentLesson, "setBackgroundColor", color)
        if (nextColor != null)
            contentView.setInt(R.id.next_lesson, "setBackgroundColor", nextColor)


        contentView.setImageViewResource(R.id.notifLogo, R.drawable.logo)
        contentView.setImageViewResource(R.id.hourIcon, R.drawable.ic_alarm)
        contentView.setImageViewResource(R.id.nextHourIcon, R.drawable.ic_arrow_forward)
        contentView.setImageViewResource(R.id.notif_dismiss, R.drawable.ic_clear)

        val cancelIntent = Intent(applicationContext, DismissNotificationReceiver::class.java)
        val cancelPIntent = PendingIntent.getBroadcast(applicationContext, 0, cancelIntent, 0)

        contentView.setOnClickPendingIntent(R.id.notif_dismiss, cancelPIntent)

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        return NotificationCompat.Builder(this)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle(lesson)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setCustomBigContentView(contentView)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
    }

    companion object {
        private const val NotificationId = 1342

        fun update(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                context.startForegroundService(context.intentFor<OngoingNotificationService>())
            else
                context.startService<OngoingNotificationService>()
        }

    }
}