package com.example.kotlinapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kotlinapp.R
import com.example.kotlinapp.view.MainActivity
import com.example.kotlinapp.view.PushActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

object NotificationCreation {

//    companion object {

    //private static
    private var notificationManager: NotificationManager? = null

    //private static final
    const val NOTIFY_ID = 1000
    private val VIBRATION = longArrayOf(300, 400, 500, 400, 300)
    private const val CHANNEL_ID = "KotlinPush_1"
    private const val CHANNEL_NAME = "KotlinPush - Push Channel 1"
    private const val CHANNEL_DESCRIPTION = "KotlinPush - Push Channel  - Description"


    fun create(context: Context, title: String, body: String) {
        create(context, title, body, false)
    }

    fun create(context: Context,
               title: String,
               body: String,
               imageUrl: String,
               imageLoadingType: ImageLoadingType,
               includeActions: Boolean = false) {

        doAsync {
            val bitmap = when(imageLoadingType) {
                ImageLoadingType.PICASSO -> Picasso.get().load(imageUrl).get()


                //devia carregar o Glide
                ImageLoadingType.GLIDE -> Picasso.get().load(imageUrl).get()
            }

            uiThread {
                create(context, title, body, false, bitmap, includeActions)
            }
        }
    }


    //member static
    fun create(
        context: Context,
        title: String,
        body: String,
        imageLoading: Boolean,
        bitmap: Bitmap? = null,
        includeActions: Boolean = false
    ) {

        if(notificationManager == null) {
            notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
        }

        notificationManager?.let { notificationManager ->

            //Channel
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var channel = notificationManager.getNotificationChannel(CHANNEL_ID)

                if (channel == null) {
                    channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    )

                    channel.description = CHANNEL_DESCRIPTION
                    channel.enableVibration(true)
                    channel.enableLights(true)
                    channel.vibrationPattern = VIBRATION

                    notificationManager.createNotificationChannel(channel)
                }
            }

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentText(body)
                .setTicker(title)
                .setContentIntent(pendingIntent)
                .setVibrate(VIBRATION)
                .setOnlyAlertOnce(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))

            //barra de progresso
            if(imageLoading) {
                notificationBuilder.setProgress(100, 0, true)
            } else {
                notificationBuilder.setProgress(0, 0, false)
            }

            //setando a imagem
            if(bitmap != null) {
                notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            }

            //actions
            if(includeActions) {
                val intentDislike = Intent(context, PushActivity::class.java)
                intentDislike.action = context.getString(R.string.disliked)

                val pendingIntentDislike = PendingIntent.getActivity(context, 0, intentDislike, 0)
                val actionDislike = NotificationCompat.Action(
                    R.drawable.notification_icon_background,
                    context.getString(R.string.disliked),
                    pendingIntentDislike
                )

                val intentLike = Intent(context, PushActivity::class.java)
                intentLike.action = context.getString(R.string.like)

                val pendingIntentLike = PendingIntent.getActivity(context, 0, intentLike, 0)
                val actionLike = NotificationCompat.Action(
                    R.drawable.notification_icon_background,
                    context.getString(R.string.like),
                    pendingIntentLike
                )

                notificationBuilder.addAction(actionDislike).addAction(actionLike)
            }

            val notificationApp = notificationBuilder.build()
            notificationManager.notify(NOTIFY_ID, notificationApp)
        }

    }

//    }

}
