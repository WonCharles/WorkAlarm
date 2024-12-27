package com.example.workalarm

import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.os.VibratorManager
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1) 진동
        vibrateDevice(context)

        // 2) 알림(Notification) 띄우기
        showNotification(context)
    }

    private fun vibrateDevice(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12(API 31) 이상: VibratorManager
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            // 안드로이드 8.0 이상이면 VibrationEffect 사용
            val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            // 안드로이드 12 미만: 기존 Vibrator
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                // 구버전 호환
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }


    private fun showNotification(context: Context) {
        // (A) 알림 채널 생성 (오레오 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel_id"
            val channelName = "Alarm Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Channel for Alarm"

            val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
        // Notification 빌드
        val builder = NotificationCompat.Builder(context, "alarm_channel_id")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("건강 리마인더")
            .setContentText("지정된 시간이 되었습니다!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Notification 표시
        val notificationManager = getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(1001, builder.build())
    }
}
