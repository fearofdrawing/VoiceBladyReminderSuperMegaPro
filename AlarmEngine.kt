package com.example.voicereminder

import android.app.*
import android.content.*
import android.os.Build
import android.provider.Settings

object AlarmEngine {
 const val CHANNEL="reminders"
 fun channel(c:Context) { val m=c.getSystemService(NotificationManager::class.java); m.createNotificationChannel(NotificationChannel(CHANNEL,"Voice reminder alarms",NotificationManager.IMPORTANCE_HIGH)) }
 fun pending(c:Context,id:Int):PendingIntent = PendingIntent.getBroadcast(c,id,Intent(c,AlarmReceiver::class.java).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun schedule(c:Context,r:Reminder):Boolean {
  val a=c.getSystemService(AlarmManager::class.java)
  if(Build.VERSION.SDK_INT>=31 && !a.canScheduleExactAlarms()) return false
  a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,r.time,pending(c,r.id));return true
 }
 fun cancel(c:Context,id:Int) { c.getSystemService(AlarmManager::class.java).cancel(pending(c,id));c.getSystemService(NotificationManager::class.java).cancel(id) }
}
