package com.example.voicereminder

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:Activity(), TextToSpeech.OnInitListener {
 private lateinit var panel:LinearLayout
 private var recorder:MediaRecorder?=null
 private var recordingPath=""
 private var dueTime=0L
 private var spokenTitle=""
 private var language="en"
 private var tts:TextToSpeech?=null
 private var recording=false
 private var recordingStarted=0L
 private var lastSound=0L
 private val handler=Handler(Looper.getMainLooper())
 private val voiceRequest=41
 private val bg=Color.rgb(9,16,31)
 private val surface=Color.rgb(23,35,57)
 private val blue=Color.rgb(35,105,235)
 private val white=Color.WHITE
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);AlarmEngine.channel(this);tts=TextToSpeech(this,this);language=getPreferences(0).getString("language","en")?:"en";askPermissions();home();handleAck(intent)}
 override fun onInit(status:Int){if(status==TextToSpeech.SUCCESS)tts?.language=if(language=="lv")Locale("lv","LV") else Locale.US}
 private fun speak(s:String){tts?.speak(s,TextToSpeech.QUEUE_FLUSH,null,"voice-reminder")}
 private fun askPermissions(){val p=mutableListOf(Manifest.permission.RECORD_AUDIO);if(Build.VERSION.SDK_INT>=33)p.add(Manifest.permission.POST_NOTIFICATIONS);val needed=p.filter{checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED};if(needed.isNotEmpty())requestPermissions(needed.toTypedArray(),2)}
 private fun shape(color:Int,r:Int=22)=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat()}
 private fun label(s:String,size:Float=18f,muted:Boolean=false):TextView=TextView(this).apply{text=s;textSize=size;setTextColor(if(muted)Color.rgb(165,184,217) else white);setPadding(dp(6),dp(9),dp(6),dp(9))}
 private fun page(title:String,back:Boolean=true){val scroll=ScrollView(this).apply{setBackgroundColor(bg);isFillViewport=true};panel=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(22),dp(20),dp(30))};scroll.addView(panel);setContentView(scroll);if(back)button("‹  BACK",surface){home()};panel.addView(label(title,30f).apply{setTypeface(null,Typeface.BOLD)})}
 private fun button(s:String,color:Int=surface,action:()->Unit){val b=Button(this).apply{text=s;textSize=18f;isAllCaps=false;setTextColor(white);background=shape(color);minHeight=dp(70);setOnClickListener{action()}};panel.addView(b,LinearLayout.LayoutParams(-1,dp(76)).apply{topMargin=dp(10)})}
 private fun card(s:String){panel.addView(label(s,17f).apply{background=shape(surface);setPadding(dp(18),dp(17),dp(18),dp(17))},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)})}
 private fun home(){handler.removeCallbacksAndMessages(null);page("Voice Reminder",false);panel.addView(label("Just speak. I'll remind you.",17f,true));button("🌐  ${if(language=="lv")"LATVIEŠU" else "ENGLISH"}  ▾",surface){language=if(language=="en")"lv" else "en";getPreferences(0).edit().putString("language",language).apply();tts?.language=if(language=="lv")Locale("lv","LV") else Locale.US;home()};button("🎙  SET REMINDER",blue){listen()};card("Tap and speak: \"Remind me in 2 minutes\"\n\"Remind me tomorrow at 9\"\n\"Atgādini man pēc 2 minūtēm\"");button("TEST ALARM (1 minute)",surface){dueTime=System.currentTimeMillis()+60000;spokenTitle="Test alarm";recordPage()};button("MANUAL REMINDER",surface){manual()};panel.addView(label("Your reminders",23f).apply{setTypeface(null,Typeface.BOLD)});ReminderStore.all(this).sortedWith(compareBy<Reminder>{!it.active}.thenBy{it.time}).forEach{r->button("${if(r.active)"⏰" else "✓"} ${r.title.take(32)}  ·  ${SimpleDateFormat("dd MMM HH:mm",Locale.getDefault()).format(Date(r.time))}",surface){manage(r.id)}}}
 private fun listen(){try{val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,if(language=="lv")"lv-LV" else "en-US").putExtra(RecognizerIntent.EXTRA_PROMPT,if(language=="lv")"Kad atgādināt?" else "When should I remind you?");startActivityForResult(i,voiceRequest)}catch(_:Exception){Toast.makeText(this,"Speech unavailable; use manual entry",Toast.LENGTH_LONG).show();manual()}}
 @Deprecated("Activity result API") override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==voiceRequest&&resultCode==RESULT_OK){val phrase=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty();val parsed=TimeParser.parse(phrase);if(parsed==null){clarify(phrase)}else{dueTime=parsed;spokenTitle=phrase;recordPage()}}}
 private fun clarify(phrase:String){page("Check reminder time");card("Heard: $phrase");card("I couldn't confidently understand the date or time. Please choose it manually.");button("TRY SPEAKING AGAIN",blue){listen()};button("CHOOSE DATE & TIME"){manual(phrase)}}
 private fun manual(prefill:String=""){page("Choose reminder time");val input=EditText(this).apply{setText(prefill);hint="Reminder name";setTextColor(white);setHintTextColor(Color.LTGRAY)};panel.addView(input);val c=Calendar.getInstance().apply{add(Calendar.HOUR_OF_DAY,1)};button("CHOOSE DATE & TIME",blue){DatePickerDialog(this,{_,y,m,d->TimePickerDialog(this,{_,h,min->c.set(y,m,d,h,min,0);dueTime=c.timeInMillis;spokenTitle=input.text.toString().ifBlank{"Reminder"};recordPage()},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}}
 private fun recordPage(){page("Record your message");card("⏰ ${SimpleDateFormat("EEE dd MMM, HH:mm",Locale.getDefault()).format(Date(dueTime))}");card("Speak your message after recording starts. Recording stops after about 3 seconds of silence.");button("🎙  START RECORDING",blue){startRecording()};button("✓  DONE — SAVE",surface){finishRecordingAndSave()};button("CANCEL"){stopRecording(true);home()};speak(if(language=="lv")"Atgādinājuma laiks iestatīts. Ieraksti ziņojumu." else "Reminder time set. Record your message.");handler.postDelayed({if(!recording && !isFinishing)startRecording()},2800)}
 private fun startRecording(){if(recording)return;if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){askPermissions();return};recordingPath="${filesDir.absolutePath}/voice_${System.currentTimeMillis()}.m4a";try{recorder=if(Build.VERSION.SDK_INT>=31)MediaRecorder(this) else MediaRecorder();recorder!!.apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setOutputFile(recordingPath);prepare();start()};recording=true;recordingStarted=System.currentTimeMillis();lastSound=recordingStarted;card("🔴 Recording... speak now");handler.postDelayed(monitor,400)}catch(e:Exception){stopRecording(true);Toast.makeText(this,"Microphone unavailable",Toast.LENGTH_LONG).show()}}
 private val monitor=object:Runnable{override fun run(){if(!recording)return;val now=System.currentTimeMillis();val amplitude=try{recorder?.maxAmplitude?:0}catch(_:Exception){0};if(amplitude>900)lastSound=now;if(now-recordingStarted>2500 && now-lastSound>=3000 || now-recordingStarted>90000){finishRecordingAndSave()}else handler.postDelayed(this,350)}}
 private fun stopRecording(discard:Boolean=false){handler.removeCallbacksAndMessages(null);val wasRecording=recording;recording=false;try{recorder?.stop()}catch(_:Exception){if(wasRecording)recordingPath=""};try{recorder?.release()}catch(_:Exception){};recorder=null;if(discard&&recordingPath.isNotBlank()){java.io.File(recordingPath).delete();recordingPath=""}}
 private fun finishRecordingAndSave(){if(!recording){Toast.makeText(this,"Start recording first",Toast.LENGTH_SHORT).show();return};stopRecording();if(dueTime<=System.currentTimeMillis()){Toast.makeText(this,"Reminder time has passed; choose another",Toast.LENGTH_LONG).show();manual(spokenTitle);return};val r=Reminder((System.currentTimeMillis()%Int.MAX_VALUE).toInt(),spokenTitle.ifBlank{"Voice reminder"},dueTime,recordingPath);ReminderStore.update(this,r);val ok=AlarmEngine.schedule(this,r);if(ok){speak(if(language=="lv")"Atgādinājums saglabāts" else "Reminder saved");home()}else{Toast.makeText(this,"Allow exact alarms in settings, then reschedule",Toast.LENGTH_LONG).show();if(Build.VERSION.SDK_INT>=31)startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));home()}}
 private fun handleAck(i:Intent?){val id=i?.getIntExtra("ack",-1)?:-1;if(id<0)return;i?.removeExtra("ack");val r=ReminderStore.get(this,id)?:return;stopService(Intent(this,AlarmSoundService::class.java));AlarmEngine.cancel(this,id);manage(id,true)}
 override fun onNewIntent(intent:Intent){super.onNewIntent(intent);setIntent(intent);handleAck(intent)}
 private fun manage(id:Int,play:Boolean=false){val r=ReminderStore.get(this,id)?:return;page(r.title);card("${if(r.active)"Upcoming" else "Completed"} · ${SimpleDateFormat("dd MMM yyyy HH:mm",Locale.getDefault()).format(Date(r.time))}");if(play){card("Acknowledged — playing your recording");playAudio(r.recording)};button("▶  PLAY MESSAGE",blue){playAudio(r.recording)};button("✓  COMPLETE"){AlarmEngine.cancel(this,id);ReminderStore.update(this,r.copy(active=false));home()};listOf(5,10,15,60).forEach{m->button("SNOOZE ${if(m==60)"1 HOUR" else "$m MIN"}"){reschedule(r,System.currentTimeMillis()+m*60000L)}};button("RESET FOR TOMORROW"){val c=Calendar.getInstance().apply{timeInMillis=r.time;add(Calendar.DAY_OF_YEAR,1)};reschedule(r,maxOf(c.timeInMillis,System.currentTimeMillis()+60000))};button("DELETE",Color.rgb(116,37,64)){AlertDialog.Builder(this).setMessage("Delete reminder and recording?").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->AlarmEngine.cancel(this,id);if(r.recording.isNotBlank())java.io.File(r.recording).delete();ReminderStore.delete(this,id);home()}.show()}}
 private fun reschedule(r:Reminder,time:Long){AlarmEngine.cancel(this,r.id);val next=r.copy(time=time,active=true);ReminderStore.update(this,next);if(!AlarmEngine.schedule(this,next))Toast.makeText(this,"Exact alarm permission needed",Toast.LENGTH_LONG).show();home()}
 private fun playAudio(path:String){if(path.isBlank()){Toast.makeText(this,"No recording",Toast.LENGTH_SHORT).show();return};try{MediaPlayer().apply{setDataSource(path);prepare();setOnCompletionListener{it.release()};start()}}catch(_:Exception){Toast.makeText(this,"Cannot play recording",Toast.LENGTH_SHORT).show()}}
 override fun onDestroy(){stopRecording(false);tts?.shutdown();super.onDestroy()}
}
