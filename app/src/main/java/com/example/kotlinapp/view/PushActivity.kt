package com.example.kotlinapp.view

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinapp.R
import com.example.kotlinapp.notification.NotificationCreation
import kotlinx.android.synthetic.main.activity_push.*
import java.text.MessageFormat

class PushActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationCreation.NOTIFY_ID)

        intent.action?.let {
            txtView.text = MessageFormat.format(
                "{0}: {1}",
                getString(R.string.action),
                intent.action)
        } ?: run {
            txtView.text = getString(R.string.no_action)
        }
    }
}
