package com.mzchat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
class SmsReceiver : BroadcastReceiver() {
 override fun onReceive(c: Context, i: Intent) {
  val msgs = Telephony.Sms.Intents.getMessagesFromIntent(i)
  for(m in msgs){
   val body = m.messageBody
   if(body.startsWith("MZMSG:")||body.startsWith("MZSTATUS:")){
    abortBroadcast()
    val real = body.substringAfter(":")
    val intent = Intent(c, MainActivity::class.java).apply{
     putExtra("sms_from", m.originatingAddress)
     putExtra("sms_body", real)
     flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    c.startActivity(intent)
   }
  }
 }
}
