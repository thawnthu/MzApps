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
