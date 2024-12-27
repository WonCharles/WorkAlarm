package com.example.workalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var editTextMinutes: EditText
    private lateinit var buttonSetAlarm: Button
    private lateinit var buttonStopAlarm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레이아웃 요소 연결
        editTextMinutes = findViewById(R.id.editTextMinutes)
        buttonSetAlarm = findViewById(R.id.buttonSetAlarm)
        buttonStopAlarm = findViewById(R.id.buttonStopAlarm)

        // 알람 시작(설정) 버튼 클릭
        buttonSetAlarm.setOnClickListener {
            val minutesStr = editTextMinutes.text.toString()
            if (minutesStr.isNotEmpty()) {
                val minutes = minutesStr.toLongOrNull()
                if (minutes != null && minutes > 0) {
                    // 실제 알람 설정
                    setAlarm(minutes)

                    // UI 전환: 입력창, 시작 버튼 숨기고 -> 중지 버튼 보이기
                    editTextMinutes.visibility = View.GONE
                    buttonSetAlarm.visibility = View.GONE
                    buttonStopAlarm.visibility = View.VISIBLE

                } else {
                    Toast.makeText(this, "양의 정수를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "시간(분)을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 알람 중지 버튼 클릭
        buttonStopAlarm.setOnClickListener {
            // 알람 취소
            stopAlarm()

            // UI 복원: 중지 버튼 숨기고 -> 입력창, 시작 버튼 보이기
            editTextMinutes.visibility = View.VISIBLE
            buttonSetAlarm.visibility = View.VISIBLE
            buttonStopAlarm.visibility = View.GONE
        }
    }

    private fun setAlarm(minutes: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 예: 15분 간격으로 반복
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            minutes * 60 * 1000,
            pendingIntent
        )

        Toast.makeText(this, "$minutes 분 간격 알람 설정 완료", Toast.LENGTH_SHORT).show()
    }

    private fun stopAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 알람 취소
        alarmManager.cancel(pendingIntent)

        Toast.makeText(this, "알람이 중지되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
