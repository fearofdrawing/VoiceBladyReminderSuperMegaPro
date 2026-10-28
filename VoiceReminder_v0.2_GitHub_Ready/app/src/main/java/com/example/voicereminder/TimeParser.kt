package com.example.voicereminder

import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

object TimeParser {
 private fun norm(s:String)=Normalizer.normalize(s.lowercase(Locale.ROOT),Normalizer.Form.NFD).replace(Regex("""\p{M}+"""),"")
 private val units=mapOf("minute" to 1,"minutes" to 1,"min" to 1,"minuti" to 1,"minutes" to 1,"minutem" to 1,"hour" to 60,"hours" to 60,"stunda" to 60,"stundas" to 60,"stundam" to 60,"day" to 1440,"days" to 1440,"diena" to 1440,"dienas" to 1440)
 fun parse(input:String,now:Long=System.currentTimeMillis()):Long? {
  val s=norm(input).replace(Regex("[^a-z0-9: ]")," ").replace(Regex(" +")," ").trim()
  val relative=Regex("""(?:in|after|pec)?\s*(\d+)\s*(minutes?|min|minuti|minutem|hours?|stundas?|stundam|days?|dienas?)""").find(s)
  if(relative!=null && (s.contains("in ")||s.contains("after ")||s.contains("pec ")||s.contains("pēc ")||s.contains("remind")||s.contains("atgadini"))) {
   val count=relative.groupValues[1].toLongOrNull()?:return null
   val factor=units[relative.groupValues[2]]?:return null
   if(count in 1..100000) return now+count*factor*60000L
  }
  val c=Calendar.getInstance().apply{timeInMillis=now}
  val tomorrow=s.contains("tomorrow")||s.contains("rit ")||s.endsWith(" rit")
  val today=s.contains("today")||s.contains("sodien")
  val weekday=mapOf("monday" to Calendar.MONDAY,"tuesday" to Calendar.TUESDAY,"wednesday" to Calendar.WEDNESDAY,"thursday" to Calendar.THURSDAY,"friday" to Calendar.FRIDAY,"saturday" to Calendar.SATURDAY,"sunday" to Calendar.SUNDAY,"pirmdien" to Calendar.MONDAY,"otrdien" to Calendar.TUESDAY,"tresdien" to Calendar.WEDNESDAY,"ceturtdien" to Calendar.THURSDAY,"piektdien" to Calendar.FRIDAY,"sestdien" to Calendar.SATURDAY,"svetdien" to Calendar.SUNDAY).entries.firstOrNull{s.contains(it.key)}?.value
  if(tomorrow)c.add(Calendar.DAY_OF_YEAR,1)
  if(weekday!=null){val d=(weekday-c.get(Calendar.DAY_OF_WEEK)+7)%7;c.add(Calendar.DAY_OF_YEAR,if(d==0)7 else d)}
  val clock=Regex("""(?:at|plkst|pulksten|pulksteni)\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""").find(s)
   ?:Regex("""\b(\d{1,2}):(\d{2})\b""").find(s)
  if(clock==null)return null
  var hour=clock.groupValues[1].toIntOrNull()?:return null
  val minute=clock.groupValues[2].toIntOrNull()?:0
  val meridiem=clock.groupValues.getOrElse(3){""}
  if(meridiem=="pm"&&hour<12)hour+=12
  if(meridiem=="am"&&hour==12)hour=0
  if(hour !in 0..23||minute !in 0..59)return null
  c.set(Calendar.HOUR_OF_DAY,hour);c.set(Calendar.MINUTE,minute);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0)
  if(c.timeInMillis<=now && !tomorrow && weekday==null && !today)c.add(Calendar.DAY_OF_YEAR,1)
  return c.timeInMillis.takeIf{it>now}
 }
}
