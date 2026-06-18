package com.example.smartreview.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.smartreview.data.learning.StudyTimeManager

class StudyTimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start tracking when service is running
        // This can be used for background tracking
        return START_STICKY
    }

    override fun onDestroy() {
        StudyTimeManager.stopTracking()
        super.onDestroy()
    }
}