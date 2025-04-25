package com.example.clockdisplay;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceFragmentCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final long CLOCK_INTERVAL_MS = 1000;          // update every second
    private static final long SHIFT_INTERVAL_MS = 60_000;        // shift once a minute

    private DrawerLayout drawerLayout;
    private TextView clockText;

    private final Handler handler = new Handler();
    private final SimpleDateFormat fmtDay  = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final SimpleDateFormat fmtDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat fmtTime = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private SharedPreferences prefs;
    private final Random random = new Random();

    private final Runnable clockRunnable = new Runnable() {
        @Override public void run() {
            updateClock();
            handler.postDelayed(this, CLOCK_INTERVAL_MS);
        }
    };

    private final Runnable shiftRunnable = new Runnable() {
        @Override public void run() {
            applyRandomShift();
            handler.postDelayed(this, SHIFT_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goImmersive();
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        clockText    = findViewById(R.id.clock_text);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Inject the settings fragment into BOTH side‑drawers so either edge‑swipe works
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.left_drawer,  new SettingsFragment())
                .replace(R.id.right_drawer, new SettingsFragment())
                .commit();

        // Apply initial user prefs
        applyUserPrefs();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.post(clockRunnable);
        handler.post(shiftRunnable);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(clockRunnable);
        handler.removeCallbacks(shiftRunnable);
    }

    @Override public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Helper methods
    // ────────────────────────────────────────────────────────────────────────────

    private void updateClock() {
        Date now = new Date();
        String display = fmtDay.format(now) + "\n" + fmtDate.format(now) + "\n" + fmtTime.format(now);
        clockText.setText(display);
    }

    private void applyUserPrefs() {
        // Text size (sp) stored as float, default 48sp
        float sizeSp = prefs.getFloat("text_size_sp", 48f);
        clockText.setTextSize(sizeSp);

        // Immediate shift if enabled
        applyRandomShift();
    }

    private void applyRandomShift() {
        if (!prefs.getBoolean("random_shift_enabled", false)) {
            clockText.setTranslationX(0);
            clockText.setTranslationY(0);
            return;
        }

        int maxDx = prefs.getInt("shift_x", 0);
        int maxDy = prefs.getInt("shift_y", 0);

        float dx = random.nextInt(maxDx * 2 + 1) - maxDx;
        float dy = random.nextInt(maxDy * 2 + 1) - maxDy;

        clockText.setTranslationX(dx);
        clockText.setTranslationY(dy);
    }

    private void goImmersive() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}