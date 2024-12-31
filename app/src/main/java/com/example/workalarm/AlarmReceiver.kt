package com.example.workalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.NotificationManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1) SharedPreferences에서 남은 횟수 읽기
        val pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
        var remaining = pref.getInt(MainActivity.KEY_REMAINING_COUNT, 0)

        if (remaining <= 0) {
            // 이미 0 이하라면 알람 해제 (안전장치)
            cancelAlarm(context)
            return
        }

        // 알람 동작: 진동 or Notification
        showNotification(context)

        // 2) 남은 횟수 1 감소
        remaining--
        pref.edit().putInt(MainActivity.KEY_REMAINING_COUNT, remaining).apply()

        // 3) 커스텀 브로드캐스트 전송
        //    (액티비티가 이 인텐트를 수신해 UI 갱신)
        val updateIntent = Intent("com.example.UPDATE_REMAINING_COUNT")
        // 필요하다면 updateIntent.putExtra("someKey", ...) 로 추가 데이터 넣을 수도 있음
        context.sendBroadcast(updateIntent)

        // 4) 만약 0이 되었다면 알람 취소
        if (remaining <= 0) {
            cancelAlarm(context)
        }
    }



    private fun showNotification(context: Context) {
        // 원하는 알림 표시: 진동, 사운드, 메시지 등
        val channelId = "alarm_channel_id"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("건강 알람")
            .setContentText("스트레칭 시간입니다!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        // (오레오 이상에서 channelId를 미리 createNotificationChannel()으로 등록했다 가정)
        notificationManager?.notify(1001, builder.build())
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
