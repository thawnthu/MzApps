package com.mzchat

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.*
import android.content.*
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.net.Uri
import android.provider.MediaStore
import org.json.JSONObject
import org.json.JSONArray

class MainActivity : Activity() {
    var contacts = mutableListOf<Pair<String,String>>()
    var curIdx = -1
    var isDark = true
    var fontSize = 16f
    var myNumber = ""
    lateinit var root: LinearLayout
    lateinit var loginView: LinearLayout
    lateinit var mainView: LinearLayout
    lateinit var chatView: LinearLayout
    lateinit var contactView: LinearLayout
    lateinit var statusView: LinearLayout
    lateinit var listView: ListView
    lateinit var msgsBox: LinearLayout
    lateinit var input: EditText
    lateinit var cName: TextView
    lateinit var lastSeen: TextView
    lateinit var searchInput: EditText
    var otpCode = "1234"

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        myNumber = getSharedPreferences("mz", MODE_PRIVATE).getString("myNumber","")?: ""
        isDark = getSharedPreferences("mz", MODE_PRIVATE).getBoolean("dark", true)
        fontSize = getSharedPreferences("mz", MODE_PRIVATE).getFloat("font", 16f)
        root = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        createLoginView()
        createMainView()
        createChatView()
        createContactView()
        createStatusView()
        root.addView(loginView)
        root.addView(mainView)
        root.addView(chatView)
        root.addView(contactView)
        root.addView(statusView)
        setContentView(root)
        if(myNumber.isNotEmpty()) showMain() else showLogin()
        handleIntent(intent)
    }

    fun createLoginView() {
        loginView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(60,200,60,60); setBackgroundColor(0xFF111B21.toInt())}
        val t1 = TextView(this).apply{text="MzApps"; textSize=32f; setTextColor(-1); gravity=Gravity.CENTER}
        val t2 = TextView(this).apply{text="Offline WhatsApp"; textSize=16f; setTextColor(0xFF8696A0.toInt()); gravity=Gravity.CENTER; setPadding(0,10,0,80)}
        val phoneInput = EditText(this).apply{hint="Phone Number"; setTextColor(-1); setHintTextColor(0xFF8696A0.toInt()); setBackgroundColor(0xFF202C33.toInt()); setPadding(30,30,30,30)}
        val otpInput = EditText(this).apply{hint="OTP 4 digit"; setTextColor(-1); setHintTextColor(0xFF8696A0.toInt()); setBackgroundColor(0xFF202C33.toInt()); setPadding(30,30,30,30); visibility=View.GONE}
        val btn = Button(this).apply{text="GET OTP"; setBackgroundColor(0xFF00A884.toInt()); setTextColor(-1)}
        val info = TextView(this).apply{text="Offline OTP - Tower hmangin"; setTextColor(0xFF8696A0.toInt()); textSize=12f; gravity=Gravity.CENTER; setPadding(0,20,0,0)}
        btn.setOnClickListener{
            if(phoneInput.text.isNotEmpty() && otpInput.visibility==View.GONE) {
                otpCode = (1000..9999).random().toString()
                myNumber = phoneInput.text.toString()
                try { SmsManager.getDefault().sendTextMessage(myNumber, null, "MzApps OTP: $otpCode", null, null) } catch(e: Exception){}
                Toast.makeText(this, "OTP: $otpCode (Demo)", Toast.LENGTH_LONG).show()
                otpInput.visibility=View.VISIBLE
                btn.text="CONFIRM OTP"
                info.text="OTP: $otpCode"
            } else if(otpInput.text.toString() == otpCode) {
                getSharedPreferences("mz", MODE_PRIVATE).edit().putString("myNumber", myNumber).apply()
                showMain()
            } else {
                Toast.makeText(this, "OTP dik lo! $otpCode", Toast.LENGTH_SHORT).show()
            }
        }
        loginView.addView(t1); loginView.addView(t2); loginView.addView(phoneInput); loginView.addView(otpInput); loginView.addView(btn); loginView.addView(info)
    }

    fun createMainView() {
        mainView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; visibility=View.GONE; setBackgroundColor(0xFF111B21.toInt())}
        val header = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(20,40,20,20); orientation=LinearLayout.HORIZONTAL}
        val title = TextView(this).apply{text="MzApps"; textSize=20f; setTextColor(-1); layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        val dot = TextView(this).apply{text="⋮"; textSize=24f; setTextColor(-1); setPadding(30,0,10,0)}
        dot.setOnClickListener{showMenu()}
        header.addView(title); header.addView(dot)
        val tab = LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setBackgroundColor(0xFF202C33.toInt())}
        val tabChat = TextView(this).apply{text="CHATS"; setTextColor(-1); setPadding(40,20,40,20); gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        val tabStatus = TextView(this).apply{text="STATUS"; setTextColor(0xFF8696A0.toInt()); setPadding(40,20,40,20); gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        tabChat.setOnClickListener{tabChat.setTextColor(-1); tabStatus.setTextColor(0xFF8696A0.toInt()); listView.visibility=View.VISIBLE; statusView.visibility=View.GONE}
        tabStatus.setOnClickListener{tabStatus.setTextColor(-1); tabChat.setTextColor(0xFF8696A0.toInt()); listView.visibility=View.GONE; statusView.visibility=View.VISIBLE; loadStatus()}
        tab.addView(tabChat); tab.addView(tabStatus)
        searchInput = EditText(this).apply{hint="Search chat..."; setTextColor(-1); setHintTextColor(0xFF8696A0.toInt()); setBackgroundColor(0xFF2A3942.toInt()); setPadding(30,20,30,20)}
        searchInput.addTextChangedListener(object: android.text.TextWatcher{override fun beforeTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){} override fun onTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){filterChats(s.toString())} override fun afterTextChanged(s: android.text.Editable?){}})
        listView = ListView(this).apply{setBackgroundColor(0xFF111B21.toInt())}
        val fab = Button(this).apply{text="+ Chat"; setBackgroundColor(0xFF00A884.toInt()); setTextColor(-1)}
        fab.setOnClickListener{showContacts()}
        mainView.addView(header); mainView.addView(tab); mainView.addView(searchInput); mainView.addView(listView, LinearLayout.LayoutParams(-1,0,1f)); mainView.addView(fab)
    }

    fun createChatView() {
        chatView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; visibility=View.GONE; setBackgroundColor(0xFF0B141A.toInt())}
        val chatH = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(10,30,10,10); orientation=LinearLayout.HORIZONTAL}
        val back = TextView(this).apply{text="← "; textSize=24f; setTextColor(-1); setPadding(20,20,20,20)}
        back.setOnClickListener{showMain()}
        val info = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        cName = TextView(this).apply{textSize=18f; setTextColor(-1)}
        lastSeen = TextView(this).apply{text="last seen today"; textSize=12f; setTextColor(0xFF8696A0.toInt())}
        info.addView(cName); info.addView(lastSeen)
        val imgBtn = TextView(this).apply{text="📷"; textSize=22f; setPadding(20,20,20,20)}
        imgBtn.setOnClickListener{pickImage()}
        chatH.addView(back); chatH.addView(info); chatH.addView(imgBtn)
        msgsBox = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(10,10,10,10)}
        val scroll = ScrollView(this).apply{addView(msgsBox); layoutParams=LinearLayout.LayoutParams(-1,0,1f)}
        val inputRow = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(10,10,10,10)}
        input = EditText(this).apply{hint="Message"; setBackgroundColor(0xFF2A3942.toInt()); setTextColor(-1); setHintTextColor(0xFF8696A0.toInt())}
        val send = Button(this).apply{text="➤"; setBackgroundColor(0xFF00A884.toInt())}
        send.setOnClickListener{sendMsg()}
        inputRow.addView(input, LinearLayout.LayoutParams(0,-2,1f)); inputRow.addView(send)
        chatView.addView(chatH); chatView.addView(scroll); chatView.addView(inputRow)
    }

        fun createContactView() {
        contactView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; visibility=View.GONE; setBackgroundColor(0xFF111B21.toInt())}
        val h = LinearLayout(this).apply{setBackgroundColor(0xFF202C33.toInt()); setPadding(20,40,20,20)}
        val b = TextView(this).apply{text="← Contacts"; textSize=18f; setTextColor(-1)}
        b.setOnClickListener{showMain()}
        h.addView(b)
        val searchC = EditText(this).apply{hint="Search..."; setTextColor(-1); setHintTextColor(0xFF8696A0.toInt()); setBackgroundColor(0xFF2A3942.toInt()); setPadding(30,20,30,20)}
        val lv = ListView(this)
        contactView.addView(h); contactView.addView(searchC); contactView.addView(lv, LinearLayout.LayoutParams(-1,0,1f))
        loadContacts(lv)
    }
    fun createStatusView() {
        statusView = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; visibility=View.GONE; setBackgroundColor(0xFF111B21.toInt())}
        val t = TextView(this).apply{text="Status 24h"; setPadding(30,30,30,30); setTextColor(-1)}
        val btn = Button(this).apply{text="+ Status"; setBackgroundColor(0xFF00A884.toInt())}
        btn.setOnClickListener{val e = EditText(this); android.app.AlertDialog.Builder(this).setView(e).setPositiveButton("Post"){_,_-> postStatus(e.text.toString())}.show()}
        statusView.addView(t); statusView.addView(btn)
    }
    fun showLogin(){loginView.visibility=View.VISIBLE; mainView.visibility=View.GONE; chatView.visibility=View.GONE; contactView.visibility=View.GONE}
    fun showMain(){loginView.visibility=View.GONE; mainView.visibility=View.VISIBLE; chatView.visibility=View.GONE; contactView.visibility=View.GONE; statusView.visibility=View.GONE; renderList()}
    fun showContacts(){mainView.visibility=View.GONE; contactView.visibility=View.VISIBLE}
    fun showMenu(){
        val opts = arrayOf("Profile: "+myNumber,"Settings Dark/Font","Logout")
        android.app.AlertDialog.Builder(this).setTitle("MzApps").setItems(opts){_,i-> when(i){0->showProfile();1->showSettings();2->{getSharedPreferences("mz", MODE_PRIVATE).edit().clear().apply(); showLogin()}}}.show()
    }
    fun showProfile(){
        val l = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(40,40,40,40)}
        val name = TextView(this).apply{text=myNumber; textSize=20f; setTextColor(-1); gravity=Gravity.CENTER}
        l.addView(name); android.app.AlertDialog.Builder(this).setTitle("Profile").setView(l).show()
    }
    fun showSettings(){
        val l = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(40,40,40,40)}
        val dark = CheckBox(this).apply{text="Dark Mode"; isChecked=isDark; setTextColor(-1)}
        dark.setOnCheckedChangeListener{_,b-> isDark=b; getSharedPreferences("mz", MODE_PRIVATE).edit().putBoolean("dark", b).apply()}
        l.addView(dark); android.app.AlertDialog.Builder(this).setTitle("Settings").setView(l).show()
    }
    fun loadContacts(lv: ListView? = null) {
        try {
            val c = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
            contacts.clear()
            while(c!!.moveToNext()){
                val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val num = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(name to num)
            }
            c!!.close()
            if(lv!=null) lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contacts.map{it.first})
            renderList()
        } catch(e: Exception){}
    }
    fun renderList(){ listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contacts.map{it.first+"\n"+it.second}) ; listView.setOnItemClickListener{_,_,i,_ -> openChat(i)} }
    fun filterChats(q: String){ val f = contacts.filter{it.first.contains(q, true)}; listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, f.map{it.first}) }
    fun openChat(i:Int){curIdx=i; cName.text=contacts[i].first; mainView.visibility=View.GONE; chatView.visibility=View.VISIBLE; renderMsgs()}
    fun renderMsgs(){
        msgsBox.removeAllViews()
        val key = if(curIdx>=0) contacts[curIdx].second else return
        val jo = JSONObject(getSharedPreferences("mz", MODE_PRIVATE).getString("chats","{}")!!)
        val arr = jo.optJSONArray(key)?: return
        for(i in 0 until arr.length()){
            val o = arr.getJSONObject(i)
            val row = LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; gravity=if(o.getBoolean("me")) Gravity.END else Gravity.START; setPadding(10,5,10,5)}
            val tick = if(o.getBoolean("me")) "✓✓ " else ""
            val tv = TextView(this).apply{text=o.getString("t")+" $tick"; setPadding(24,16,24,16); textSize=fontSize; setTextColor(-1); setBackgroundColor(if(o.getBoolean("me")) 0xFF005C4B.toInt() else 0xFF202C33.toInt())}
            row.addView(tv); msgsBox.addView(row)
        }
    }
    fun saveChat(num:String, msg:String, me:Boolean){
        val prefs = getSharedPreferences("mz", MODE_PRIVATE)
        val jo = JSONObject(prefs.getString("chats","{}")!!)
        val arr = jo.optJSONArray(num)?: JSONArray()
        val o = JSONObject().put("t",msg).put("me",me)
        arr.put(o); jo.put(num, arr); prefs.edit().putString("chats", jo.toString()).apply()
    }
    fun sendMsg(){
        val t = input.text.toString().trim(); if(t.isEmpty()||curIdx==-1) return
        val num = contacts[curIdx].second
        try { SmsManager.getDefault().sendTextMessage(num, null, "MZMSG:$t", null, null) } catch(e: Exception){}
        saveChat(num, t, true); input.setText(""); renderMsgs()
    }
    fun postStatus(txt: String){ for(c in contacts) { try{ SmsManager.getDefault().sendTextMessage(c.second, null, "MZSTATUS:$txt", null, null) } catch(e: Exception){} } Toast.makeText(this, "Status posted!", Toast.LENGTH_SHORT).show() }
    fun loadStatus(){}
    fun pickImage(){ val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(intent, 101) }
    override fun onActivityResult(r:Int, res:Int, d:Intent?){ super.onActivityResult(r,res,d); if(r==101 && res==RESULT_OK){ if(curIdx>=0){ val num = contacts[curIdx].second; saveChat(num, "[Image]", true); renderMsgs() } } }
    override fun onNewIntent(i: Intent){ super.onNewIntent(i); handleIntent(i) }
    fun handleIntent(i: Intent){
        val from = i.getStringExtra("sms_from")?: return
        val body = i.getStringExtra("sms_body")?: return
        saveChat(from, body, false)
        if(curIdx>=0 && contacts[curIdx].second.contains(from.takeLast(4))) renderMsgs()
    }
}
