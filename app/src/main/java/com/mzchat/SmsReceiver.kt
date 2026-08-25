package com.mzchat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.os.Build

class SmsReceiver : BroadcastReceiver() {
 override fun onReceive(c: Context, i: Intent) {
  val msgs = Telephony.Sms.Intents.getMessagesFromIntent(i)
  for(m in msgs){
   val body = m.messageBody
   if(body.startsWith("MZMSG:")){
    abortBroadcast()
    val realMsg = body.removePrefix("MZMSG:")
    val from = m.originatingAddress
    val prefs = c.getSharedPreferences("mz", Context.MODE_PRIVATE)
    val all = prefs.getString("chats","{}")
    // Save via MainActivity
    val intent = Intent(c, MainActivity::class.java)
    intent.putExtra("sms_from", from)
    intent.putExtra("sms_body", realMsg)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    c.startActivity(intent)
   }
  }
 }
}
