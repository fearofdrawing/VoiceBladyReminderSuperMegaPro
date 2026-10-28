package com.example.voicereminder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Reminder(val id:Int,val title:String,val time:Long,val recording:String,val active:Boolean=true,val completedAt:Long=0L)

object ReminderStore {
 fun all(c:Context):List<Reminder> = try {
  val a=JSONArray(c.getSharedPreferences("reminders",0).getString("items","[]"))
  (0 until a.length()).map{a.getJSONObject(it)}.map{Reminder(it.getInt("id"),it.getString("title"),it.getLong("time"),it.optString("recording"),it.optBoolean("active",true),it.optLong("completedAt",0L))}
 }catch(_:Exception){emptyList()}
 fun save(c:Context,items:List<Reminder>){val a=JSONArray();items.forEach{a.put(JSONObject().put("id",it.id).put("title",it.title).put("time",it.time).put("recording",it.recording).put("active",it.active).put("completedAt",it.completedAt))};c.getSharedPreferences("reminders",0).edit().putString("items",a.toString()).apply()}
 fun get(c:Context,id:Int)=all(c).firstOrNull{it.id==id}
 fun update(c:Context,r:Reminder){save(c,all(c).filterNot{it.id==r.id}+r)}
 fun delete(c:Context,id:Int){save(c,all(c).filterNot{it.id==id})}
 fun deleteWithAudio(c:Context,id:Int){get(c,id)?.recording?.takeIf{it.isNotBlank()}?.let{try{File(it).delete()}catch(_:Exception){}};delete(c,id)}
 @Synchronized fun cleanup(c:Context):Int {val now=System.currentTimeMillis();val cutoff=now-14L*24*60*60*1000;val items=all(c);val expired=items.filter{!it.active && (if(it.completedAt>0)it.completedAt else it.time)<cutoff};expired.forEach{it.recording.takeIf{path->path.isNotBlank()}?.let{path->try{File(path).delete()}catch(_:Exception){}}};if(expired.isNotEmpty())save(c,items.filterNot{r->expired.any{it.id==r.id}});return expired.size}
}
