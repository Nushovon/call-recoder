package com.myapp.callrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapp.callrecorder.data.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val requiredPermissions = mutableListOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.widget.Button>(R.id.btnGrantPermissions).setOnClickListener {
            requestNeededPermissions()
        }

        if (!hasAllPermissions()) {
            requestNeededPermissions()
        }

        loadRecordings()
    }

    override fun onResume() {
        super.onResume()
        loadRecordings()
    }

    private fun hasAllPermissions(): Boolean = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNeededPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, 100)
    }

    // Pulls saved recordings (number + time + duration) from the local database
    // and shows them in the list - this is the only place the data is surfaced.
    private fun loadRecordings() {
        lifecycleScope.launch {
            val list = AppDatabase.getInstance(applicationContext).callRecordingDao().getAll()
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = RecordingAdapter(list)
        }
    }
}
