package com.luck.pictureselector

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SimpleActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other)
        val btnActivity = findViewById<Button>(R.id.btn_activity)
        val btnInjectFragment = findViewById<Button>(R.id.btn_inject_fragment)
        val btnOnlyQueryData = findViewById<Button>(R.id.btn_only_query_data)
        btnActivity.setOnClickListener(this)
        btnInjectFragment.setOnClickListener(this)
        btnOnlyQueryData.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_activity -> startActivity(Intent(this@SimpleActivity, MainActivity::class.java))
            R.id.btn_inject_fragment -> startActivity(Intent(this@SimpleActivity, InjectFragmentActivity::class.java))
            R.id.btn_only_query_data -> startActivity(Intent(this@SimpleActivity, OnlyQueryDataActivity::class.java))
        }
    }
}

