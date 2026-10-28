package com.example.voicereminder

import android.app.*
import android.content.*
import android.media.*
import android.os.*

class AlarmSoundService:Service() {
 private var player:MediaPlayer?=null
 private val handler=Handler(Looper.getMainLooper())
 override fun onBind(i:Intent?):IBinder?=null
 override fun onStartCommand(i:Intent?,flags:Int,startId:Int):Int {
  AlarmEngine.channel(this)
  val n=Notification.Builder(this,AlarmEngine.CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Reminder alarm sounding").build()
  startForeground(999999,n)
  player?.release();player=null
  try { val uri=android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);player=MediaPlayer().apply { setDataSource(this@AlarmSoundService,uri);setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());isLooping=true;prepare();start() } }catch (_:Exception){}
  handler.removeCallbacksAndMessages(null);handler.postDelayed({stopSelf()},15000)
  return START_NOT_STICKY
 }
 override fun onDestroy(){handler.removeCallbacksAndMessages(null);player?.release();player=null;super.onDestroy()}
}
