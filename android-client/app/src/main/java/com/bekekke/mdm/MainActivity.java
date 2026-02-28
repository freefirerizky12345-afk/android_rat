package com.bekekke.mdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple Dynamic UI
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(0xFF0A0A0C); // Dark Background

        TextView title = new TextView(this);
        title.setText("BEKEKKE-MDM");
        title.setTextSize(32);
        title.setTextColor(0xFF3B82F6); // Blue Accent
        title.setPadding(0, 0, 0, 50);
        layout.addView(title);

        Button btnStart = new Button(this);
        btnStart.setText("AKTIFKAN MONITORING ⚡");
        btnStart.setBackgroundColor(0xFF1E1E1E);
        btnStart.setTextColor(0xFFFFFFFF);
        
        btnStart.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, RemoteService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            btnStart.setText("SERVICE RUNNING ✅");
            btnStart.setEnabled(false);
        });

        layout.addView(btnStart);
        setContentView(layout);
    }
}
