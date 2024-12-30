package com.example.workalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.NotificationManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // SharedPreferences에서 남은 횟수 가져오기
        val pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
        var remaining = pref.getInt(MainActivity.KEY_REMAINING_COUNT, 0)

        if (remaining <= 0) {
            // 이미 0 이하이면 알람 해제
            cancelAlarm(context)
            return
        }

        // 알림 표시(진동 등 원하는 동작)
        showNotification(context)

        // 남은 횟수 감소
        remaining--
        pref.edit().putInt(MainActivity.KEY_REMAINING_COUNT, remaining).apply()

        // 0이 되었으면 알람 취소
        if (remaining <= 0) {
            cancelAlarm(context)
        }
    }

    private fun showNotification(context: Context) {
        // 임의의 채널 ID
        val channelId = "alarm_channel_id"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("건강 알람!")
            .setContentText("스트레칭 시간입니다!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // 알림 매니저
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(9999, builder.build())
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}
