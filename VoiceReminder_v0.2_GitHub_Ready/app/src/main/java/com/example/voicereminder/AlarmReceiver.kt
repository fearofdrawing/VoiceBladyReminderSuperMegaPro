package com.example.voicereminder

import android.app.*
import android.content.*
import android.os.Build

class AlarmReceiver: android.content.BroadcastReceiver() {
 override fun onReceive(c:Context,i:Intent) {
  val id=i.getIntExtra("id",-1);val r=ReminderStore.get(c,id) ?:return
  if(!r.active)return
  AlarmEngine.channel(c)
  val open=PendingIntent.getActivity(c,id,Intent(c,MainActivity::class.java).putExtra("ack",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val snooze=PendingIntent.getBroadcast(c,id+100000,Intent(c,SnoozeReceiver::class.java).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val n=Notification.Builder(c,AlarmEngine.CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Reminder: ${r.title}").setContentText("Acknowledge to hear your recording").setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_HIGH).setAutoCancel(false).setOngoing(true).setContentIntent(open).addAction(Notification.Action.Builder(null,"Acknowledge",open).build()).addAction(Notification.Action.Builder(null,"Snooze 10 min",snooze).build()).build()
  c.getSystemService(NotificationManager::class.java).notify(id,n)
  val service=Intent(c,AlarmSoundService::class.java).putExtra("id",id)
  try { if(Build.VERSION.SDK_INT>=26)c.startForegroundService(service) else c.startService(service) }catch (_:Exception){}
  // Repeat at five-minute intervals until acknowledged or snoozed.
  AlarmEngine.schedule(c,r.copy(time=System.currentTimeMillis()+300000))
 }
}

class SnoozeReceiver:android.content.BroadcastReceiver() {
 override fun onReceive(c:Context,i:Intent) {
  val id=i.getIntExtra("id",-1);val r=ReminderStore.get(c,id)?:return
  c.stopService(Intent(c,AlarmSoundService::class.java));AlarmEngine.cancel(c,id)
  val next=r.copy(time=System.currentTimeMillis()+600000);ReminderStore.update(c,next);AlarmEngine.schedule(c,next)
 }
}
class BootReceiver:android.content.BroadcastReceiver() {
 override fun onReceive(c:Context,i:Intent) { if(i.action==Intent.ACTION_BOOT_COMPLETED) ReminderStore.all(c).filter{it.active}.forEach { AlarmEngine.schedule(c,it.copy(time=maxOf(it.time,System.currentTimeMillis()+60000))) } }
}
