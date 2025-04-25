package com.example.daydate_gpto3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private SeekBar seekSize;
    private Switch  swShift;
    private SeekBar seekX;
    private SeekBar seekY;
    private RadioGroup radioInterval;
    private SharedPreferences prefs;
    private Spinner spinnerFontStyle;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());

        seekSize = v.findViewById(R.id.seek_text_size);
        swShift  = v.findViewById(R.id.switch_random_shift);
        seekX    = v.findViewById(R.id.seek_shift_x);
        seekY    = v.findViewById(R.id.seek_shift_y);
        radioInterval = v.findViewById(R.id.radio_shift_interval);
        spinnerFontStyle = v.findViewById(R.id.spinner_font_style);

        // ── initialise UI from prefs ──
        seekSize.setProgress((int) prefs.getFloat("text_size_sp", 144f));
        boolean enabled = prefs.getBoolean("random_shift_enabled", false);
        swShift.setChecked(enabled);
        seekX.setEnabled(enabled);
        seekY.setEnabled(enabled);
        seekX.setProgress(prefs.getInt("shift_x", 0));
        seekY.setProgress(prefs.getInt("shift_y", 0));

        // Set initial radio button state
        int intervalMs = prefs.getInt("shift_interval_ms", 60000); // default 1 minute
        int radioId = R.id.radio_1min;
        if (intervalMs == 3000) radioId = R.id.radio_3s;
        else if (intervalMs == 300000) radioId = R.id.radio_5min;
        else if (intervalMs == 600000) radioId = R.id.radio_10min;
        radioInterval.check(radioId);

        // Initialize Font Style Spinner
        String[] fontStyles = getResources().getStringArray(R.array.font_styles);

        // Create an ArrayAdapter using the string array and our custom spinner item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, fontStyles);
        // Specify the layout to use when the list of choices appears (using standard Android layout)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerFontStyle.setAdapter(adapter);

        // Set spinner initial selection
        String currentStyle = prefs.getString("font_style", "sans-serif"); // Default to sans-serif
        int currentPosition = 0;
        for (int i = 0; i < fontStyles.length; i++) {
            if (fontStyles[i].equalsIgnoreCase(currentStyle)) {
                currentPosition = i;
                break;
            }
        }
        spinnerFontStyle.setSelection(currentPosition);

        // ── listeners ──
        seekSize.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override public void onProgressChanged(SeekBar sb, int val, boolean fromUser) {
                prefs.edit().putFloat("text_size_sp", val).apply();
                // Notify main activity to update text size
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).applyUserPrefs();
                }
            }
        });

        swShift.setOnCheckedChangeListener((b, isChecked) -> {
            prefs.edit().putBoolean("random_shift_enabled", isChecked).apply();
            seekX.setEnabled(isChecked);
            seekY.setEnabled(isChecked);
        });

        seekX.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override public void onProgressChanged(SeekBar sb, int val, boolean fromUser) {
                prefs.edit().putInt("shift_x", val).apply();
            }
        });
        seekY.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override public void onProgressChanged(SeekBar sb, int val, boolean fromUser) {
                prefs.edit().putInt("shift_y", val).apply();
            }
        });

        radioInterval.setOnCheckedChangeListener((group, checkedId) -> {
            int newIntervalMs = 60000; // default 1 minute
            if (checkedId == R.id.radio_3s) newIntervalMs = 3000;
            else if (checkedId == R.id.radio_5min) newIntervalMs = 300000;
            else if (checkedId == R.id.radio_10min) newIntervalMs = 600000;
            prefs.edit().putInt("shift_interval_ms", newIntervalMs).apply();
        });

        // Listener for Font Style Spinner
        spinnerFontStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStyle = (String) parent.getItemAtPosition(position);
                prefs.edit().putString("font_style", selectedStyle.toLowerCase()).apply(); // Save lowercase name
                // Notify main activity to update font style
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).applyUserPrefs();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────────
    private abstract static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar s) {}
        @Override public void onStopTrackingTouch(SeekBar s) {}
    }
}