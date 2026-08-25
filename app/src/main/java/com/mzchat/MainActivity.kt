package com.mzchat
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tv = TextView(this)
        tv.text = "MzApps - A Tlang Fel Ta!\n\nWelcome Boss!"
        tv.textSize = 24f
        tv.gravity = Gravity.CENTER
        tv.setPadding(50, 200, 50, 50)
        
        setContentView(tv)
    }
}
