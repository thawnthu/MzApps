package com.mzchat
import android.os.Bundle
import android.app.Activity
import android.widget.*
import android.view.*
import android.content.*
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.app.PendingIntent
import org.json.JSONObject

class MainActivity : Activity() {
 var contacts = mutableListOf<Pair<String,String>>() // name, number
 var curIdx = -1
 lateinit var listView: ListView
 lateinit var chatView: LinearLayout
 lateinit var listMain: LinearLayout
 lateinit var msgsBox: LinearLayout
 lateinit var input: EditText
 lateinit var cName: TextView

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)
  // UI in Code
  val root = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setBackgroundColor(0xFF111B21.toInt())}
  val header = TextView(this).apply{text="MzChat - Offline Fast"; setPadding(30,40,30,30); setTextColor(0xFFFFFFFF.toInt()); textSize=18f; setBackgroundColor(0xFF202C33.toInt())}
  val btn = Button(this).apply{text="+ Contact Thlang"; setOnClickListener{loadContacts()}}
  header.setOnClickListener{btn.performClick()}
  listView = ListView(this)
  listMain = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; addView(header); addView(btn); addView(listView)}

  // Chat View
  chatView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; visibility=View.GONE; setBackgroundColor(0xFF0B141A.toInt())}
  val chatH = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(10,30,10,10)}
  val back = TextView(this).apply{text="← "; textSize=24f; setTextColor(0xFFFFFFFF.toInt()); setPadding(20,20,20,20); setOnClickListener{showList()}}
  cName = TextView(this).apply{textSize=18f; setTextColor(0xFFFFFFFF.toInt())}
  chatH.addView(back); chatH.addView(cName)
  msgsBox = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  val scroll = ScrollView(this).apply{addView(msgsBox); layoutParams=LinearLayout.LayoutParams(-1,0,1f)}
  val inputRow = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(10,10,10,10)}
  input = EditText(this).apply{hint="Message"; setBackgroundColor(0xFF2A3942.toInt()); setTextColor(0xFFFFFFFF.toInt())}
  val send = Button(this).apply{text="➤"; setOnClickListener{sendMsg()}}
  inputRow.addView(input, LinearLayout.LayoutParams(0,-2,1f)); inputRow.addView(send)
  chatView.addView(chatH); chatView.addView(scroll); chatView.addView(inputRow)

  root.addView(listMain, LinearLayout.LayoutParams(-1,-1)); root.addView(chatView, LinearLayout.LayoutParams(-1,-1))
  setContentView(root)

  loadSavedContacts()
  handleIntent(intent)
 }

 fun showList(){chatView.visibility=View.GONE; listMain.visibility=View.VISIBLE; renderList()}
 fun openChat(i:Int){curIdx=i; cName.text=contacts[i].first; listMain.visibility=View.GONE; chatView.visibility=View.VISIBLE; renderMsgs()}

 fun loadContacts(){
  val c = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
  contacts.clear()
  while(c!!.moveToNext()){
   val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
   val num = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
   contacts.add(name to num)
  }
  c.close()
  saveContacts(); renderList()
 }
 fun saveContacts(){getSharedPreferences("mz", MODE_PRIVATE).edit().putString("cons", contacts.joinToString("|"){it.first+":"+it.second}).apply()}
 fun loadSavedContacts(){
  val s = getSharedPreferences("mz", MODE_PRIVATE).getString("cons","")?: ""
  if(s.isNotEmpty()) contacts = s.split("|").map{val p=it.split(":"); p[0] to p[1]}.toMutableList()
  renderList()
 }
 fun renderList(){
  listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, contacts.map{it.first+"\n"+it.second})
  listView.setOnItemClickListener{_,_,i,_ -> openChat(i)}
 }
 fun renderMsgs(){
  msgsBox.removeAllViews()
  val chats = getChats()
  val key = contacts[curIdx].second
  val arr = chats.optJSONArray(key)?: return
  for(i in 0 until arr.length()){
   val o = arr.getJSONObject(i)
   val tv = TextView(this).apply{
    text = o.getString("t")+" "+(if(o.getBoolean("me"))"✓✓" else "")
    setPadding(20,15,20,15); setTextColor(0xFFFFFFFF.toInt())
    setBackgroundColor(if(o.getBoolean("me")) 0xFF005C4B.toInt() else 0xFF202C33.toInt())
   }
   msgsBox.addView(tv)
  }
 }
 fun getChats(): JSONObject {
  val s = getSharedPreferences("mz", MODE_PRIVATE).getString("chats","{}")!!
  return JSONObject(s)
 }
 fun saveChat(fromOrTo:String, msg:String, me:Boolean){
  val prefs = getSharedPreferences("mz", MODE_PRIVATE)
  val jo = JSONObject(prefs.getString("chats","{}")!!)
  val arr = jo.optJSONArray(fromOrTo)?: org.json.JSONArray()
  val o = JSONObject().put("t",msg).put("me",me).put("time", System.currentTimeMillis())
  arr.put(o); jo.put(fromOrTo, arr)
  prefs.edit().putString("chats", jo.toString()).apply()
 }

 fun sendMsg(){
  val t = input.text.toString().trim(); if(t.isEmpty()||curIdx==-1) return
  val num = contacts[curIdx].second
  val sms = SmsManager.getDefault()
  sms.sendTextMessage(num, null, "MZMSG:$t", null, null)
  saveChat(num, t, true)
  input.setText(""); renderMsgs()
 }

 override fun onNewIntent(i: Intent){super.onNewIntent(i); handleIntent(i)}
 fun handleIntent(i: Intent){
  val from = i.getStringExtra("sms_from")?: return
  val body = i.getStringExtra("sms_body")?: return
  saveChat(from, body, false)
  // Find idx
  val idx = contacts.indexOfFirst{from.contains(it.second.takeLast(10))}
  if(idx!=-1 && curIdx==idx) renderMsgs()
  Toast.makeText(this,"Thar: $body",Toast.LENGTH_SHORT).show()
 }
}
