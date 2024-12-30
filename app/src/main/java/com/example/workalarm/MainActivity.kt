package com.example.workalarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var editTextTotalMinutes: EditText
    private lateinit var editTextInterval: EditText
    private lateinit var buttonSetAlarm: Button
    private lateinit var buttonStopAlarm: Button
    private lateinit var textViewRemaining: TextView

    companion object {
        const val PREF_NAME = "alarm_pref"
        const val KEY_REMAINING_COUNT = "remaining_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel_id",
                "Alarm Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Alarm"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }


        editTextTotalMinutes = findViewById(R.id.editTextTotalMinutes)
        editTextInterval = findViewById(R.id.editTextInterval)
        buttonSetAlarm = findViewById(R.id.buttonSetAlarm)
        buttonStopAlarm = findViewById(R.id.buttonStopAlarm)
        textViewRemaining = findViewById(R.id.textViewRemaining)

        // 알람 설정하기
        buttonSetAlarm.setOnClickListener {
            val totalStr = editTextTotalMinutes.text.toString()
            val intervalStr = editTextInterval.text.toString()
            val totalMinutes = totalStr.toLongOrNull()
            val intervalMinutes = intervalStr.toLongOrNull()

            if (totalMinutes != null && intervalMinutes != null && totalMinutes > 0 && intervalMinutes > 0) {
                // 총 반복 횟수: 총시간 / 간격
                val repeatCount = (totalMinutes / intervalMinutes).toInt()
                if (repeatCount <= 0) {
                    Toast.makeText(this, "총 시간 / 간격 결과가 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 남은 횟수를 SharedPreferences에 저장
                val pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                pref.edit().putInt(KEY_REMAINING_COUNT, repeatCount).apply()

                // 알람 설정
                setAlarm(intervalMinutes)

                // UI 전환
                editTextTotalMinutes.visibility = View.GONE
                editTextInterval.visibility = View.GONE
                buttonSetAlarm.visibility = View.GONE
                buttonStopAlarm.visibility = View.VISIBLE
                textViewRemaining.text = "총 $repeatCount 회 알람이 남았습니다."

            } else {
                Toast.makeText(this, "양의 정수를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 알람 중지하기
        buttonStopAlarm.setOnClickListener {
            stopAlarm()

            // UI 복원
            editTextTotalMinutes.visibility = View.VISIBLE
            editTextInterval.visibility = View.VISIBLE
            buttonSetAlarm.visibility = View.VISIBLE
            buttonStopAlarm.visibility = View.GONE
            textViewRemaining.text = "남은 횟수: -"
        }
    }

    private fun setAlarm(intervalMinutes: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = System.currentTimeMillis()
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMinutes * 60_000,
            pendingIntent
        )

        Toast.makeText(this, "$intervalMinutes 분 간격으로 알람 설정 완료", Toast.LENGTH_SHORT).show()
    }

    private fun stopAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)

        // 남은 횟수 초기화
        val pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        pref.edit().putInt(KEY_REMAINING_COUNT, 0).apply()

        Toast.makeText(this, "알람 중지됨", Toast.LENGTH_SHORT).show()
    }
}
