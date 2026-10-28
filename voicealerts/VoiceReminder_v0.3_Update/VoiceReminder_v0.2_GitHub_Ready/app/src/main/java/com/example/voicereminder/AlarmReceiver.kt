package com.example.voicereminder

import android.app.*
import android.content.*
import android.os.Build

class AlarmReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,i:Intent){
  val id=i.getIntExtra("id",-1);val r=ReminderStore.get(c,id)?:return
  if(!r.active)return
  AlarmEngine.channel(c)
  val show=PendingIntent.getActivity(c,id,Intent(c,MainActivity::class.java).putExtra("alarm_action","show").putExtra("id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val builder=Notification.Builder(c,AlarmEngine.CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("⏰ ${r.title}").setContentText("Tap for large acknowledge and snooze buttons").setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_HIGH).setAutoCancel(false).setOngoing(true).setContentIntent(show).setFullScreenIntent(show,true)
  listOf(5,10,15,60).forEach{minutes->val pending=PendingIntent.getBroadcast(c,id*10+minutes,Intent(c,SnoozeReceiver::class.java).putExtra("id",id).putExtra("minutes",minutes),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);builder.addAction(Notification.Action.Builder(null,if(minutes==60)"1 hour" else "$minutes min",pending).build())}
  c.getSystemService(NotificationManager::class.java).notify(id,builder.build())
  try{val service=Intent(c,AlarmSoundService::class.java).putExtra("id",id);if(Build.VERSION.SDK_INT>=26)c.startForegroundService(service) else c.startService(service)}catch(_:Exception){}
  // Retain repeat behavior; user can snooze or acknowledge to stop it.
  AlarmEngine.schedule(c,r.copy(time=System.currentTimeMillis()+300000L))
 }
}
class SnoozeReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){val id=i.getIntExtra("id",-1);val r=ReminderStore.get(c,id)?:return;val minutes=i.getIntExtra("minutes",10).coerceIn(1,1440);c.stopService(Intent(c,AlarmSoundService::class.java));AlarmEngine.cancel(c,id);val next=r.copy(time=System.currentTimeMillis()+minutes*60000L,active=true,completedAt=0L);ReminderStore.update(c,next);AlarmEngine.schedule(c,next)}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){if(i.action==Intent.ACTION_BOOT_COMPLETED){ReminderStore.cleanup(c);ReminderStore.all(c).filter{it.active}.forEach{AlarmEngine.schedule(c,it.copy(time=maxOf(it.time,System.currentTimeMillis()+60000L)))}}}}
