package com.example.workalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

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

    // (1) 수신할 리시버 정의
    private val uiUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 여기서 SharedPreferences를 다시 읽어와서 UI 갱신
            val pref = getSharedPreferences("alarm_pref", Context.MODE_PRIVATE)
            val remaining = pref.getInt("remaining_count", 0)
            textViewRemaining.text = "남은 알람 횟수: $remaining"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTotalMinutes = findViewById(R.id.editTextTotalMinutes)
        editTextInterval = findViewById(R.id.editTextInterval)
        buttonSetAlarm = findViewById(R.id.buttonSetAlarm)
        buttonStopAlarm = findViewById(R.id.buttonStopAlarm)
        textViewRemaining = findViewById(R.id.textViewRemaining)

        // "알람 설정하기" 버튼
        buttonSetAlarm.setOnClickListener {
            val totalStr = editTextTotalMinutes.text.toString()
            val intervalStr = editTextInterval.text.toString()
            if (totalStr.isNotEmpty() && intervalStr.isNotEmpty()) {
                val totalMinutes = totalStr.toLongOrNull()
                val intervalMinutes = intervalStr.toLongOrNull()
                if (totalMinutes != null && intervalMinutes != null &&
                    totalMinutes > 0 && intervalMinutes > 0
                ) {
                    // 총 몇 번 울릴지 계산
                    val repeatCount = (totalMinutes / intervalMinutes).toInt()
                    if (repeatCount <= 0) {
                        Toast.makeText(this, "총 시간 / 간격의 결과가 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // 남은 횟수를 SharedPreferences에 저장
                    val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    pref.edit().putInt(KEY_REMAINING_COUNT, repeatCount).apply()

                    // 알람 설정
                    setAlarm(intervalMinutes)

                    // UI 전환
                    editTextTotalMinutes.visibility = View.GONE
                    editTextInterval.visibility = View.GONE
                    buttonSetAlarm.visibility = View.GONE
                    buttonStopAlarm.visibility = View.VISIBLE

                    // 남은 횟수 표시
                    textViewRemaining.text = "총 $repeatCount 회 알람이 남았습니다."
                } else {
                    Toast.makeText(this, "양의 정수를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "총 시간과 간격(분)을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // "알람 중지하기" 버튼
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // (2) 액티비티가 화면에 표시될 때, 브로드캐스트 수신 등록
        val filter = IntentFilter("com.example.UPDATE_REMAINING_COUNT")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(uiUpdateReceiver, filter, RECEIVER_EXPORTED)
        }

        // onResume() 시점에 혹시 값이 바뀌었을 수도 있으니 즉시 한번 UI 갱신
        val pref = getSharedPreferences("alarm_pref", Context.MODE_PRIVATE)
        val remaining = pref.getInt("remaining_count", 0)
        textViewRemaining.text = "남은 알람 횟수: $remaining"
    }

    override fun onPause() {
        super.onPause()
        // (3) 액티비티가 화면에서 사라지면 브로드캐스트 수신 해제
        unregisterReceiver(uiUpdateReceiver)
    }


    private fun setAlarm(intervalMinutes: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)

        // 남은 횟수 초기화
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putInt(KEY_REMAINING_COUNT, 0).apply()

        Toast.makeText(this, "알람 중지됨", Toast.LENGTH_SHORT).show()
    }



}
