package com.example.voicereminder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Reminder(val id: Int, val title: String, val time: Long, val recording: String, val active: Boolean = true)

object ReminderStore {
 fun all(c: Context): List<Reminder> = try {
  val a = JSONArray(c.getSharedPreferences("reminders",0).getString("items","[]"))
  (0 until a.length()).map { a.getJSONObject(it) }.map { Reminder(it.getInt("id"),it.getString("title"),it.getLong("time"),it.optString("recording"),it.optBoolean("active",true)) }
 } catch (_: Exception) { emptyList() }
 fun save(c: Context, items: List<Reminder>) {
  val a = JSONArray(); items.forEach { a.put(JSONObject().put("id",it.id).put("title",it.title).put("time",it.time).put("recording",it.recording).put("active",it.active)) }
  c.getSharedPreferences("reminders",0).edit().putString("items",a.toString()).apply()
 }
 fun get(c:Context,id:Int)=all(c).firstOrNull { it.id==id }
 fun update(c:Context,r:Reminder) { save(c,all(c).filterNot { it.id==r.id }+r) }
 fun delete(c:Context,id:Int) { save(c,all(c).filterNot { it.id==id }) }
}
