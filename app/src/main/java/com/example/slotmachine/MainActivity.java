package com.example.slotmachine;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public TextView[] counterTexts;
    public ImageView[] imageViews;
    public Drawable[] drawables;
    public Button spinButton;

    UpdateSlots[] updateSlots;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        Objects.requireNonNull(this.getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        counterTexts = new TextView[3];
        counterTexts[0] = findViewById(R.id.counterText1);
        counterTexts[1] = findViewById(R.id.counterText2);
        counterTexts[2] = findViewById(R.id.counterText3);

        imageViews = new ImageView[3];
        imageViews[0] = findViewById(R.id.imageView1);
        imageViews[1] = findViewById(R.id.imageView2);
        imageViews[2] = findViewById(R.id.imageView3);

        drawables = new Drawable[4];
        for (int i = 1; i <= 4; i++)
            drawables[i - 1] = getDrawable(getResources().getIdentifier("drawable/icon" + i, null, getPackageName()));

        spinButton = findViewById(R.id.spinButton);

        handler = new Handler();
        updateSlots = new UpdateSlots[3];
        for (int i = 0; i < 3; i++)
            updateSlots[i] = new UpdateSlots(counterTexts[i], imageViews[i]);

        randomizeSlotsDelay();

        spinButton.setOnClickListener(this::onClickSpinButton);
    }

    private void onClickSpinButton(View view) {
        boolean isCountingUp = spinButton.getText().equals("STOP");

        if (isCountingUp) {
            for (int i = 0; i < 3; i++)
                handler.removeCallbacks(updateSlots[i]);

            randomizeSlotsDelay();
            spinButton.setText("SPIN");

            if (
                    counterTexts[0].getText().equals(counterTexts[1].getText()) &&
                            counterTexts[1].getText().equals(counterTexts[2].getText())
            ) {
                vibrate(500, VibrationEffect.DEFAULT_AMPLITUDE, 3);
                notifyJackpot("Jackpot!", android.R.drawable.star_big_on);
            } else
                // check if 2 out of 3 are the same
                if (
                        counterTexts[0].getText().equals(counterTexts[1].getText()) ||
                                counterTexts[1].getText().equals(counterTexts[2].getText()) ||
                                counterTexts[0].getText().equals(counterTexts[2].getText())
                ) {
                    vibrate(250, VibrationEffect.DEFAULT_AMPLITUDE, 2);
                    notifyJackpot("Semi-Jackpot!", android.R.drawable.star_big_off);
                }

            return;
        }

        vibrate(100);
        for (int i = 0; i < 3; i++)
            handler.postDelayed(updateSlots[i], 0);


        spinButton.setText("STOP");
    }

    private void randomizeSlotsDelay() {
        for (int i = 0; i < 3; i++) {
            // randomize delay
            updateSlots[i].randomDelay = (int) (Math.random() * 500) + 300;
            // randomize starting time (thus the starting image)
            updateSlots[i].time = (int) (Math.random() * 3);
        }
    }

    private void notifyJackpot(String message, int icon) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage(message)
                .setNegativeButton("OK", null)
                .setIcon(icon)
                .setCancelable(false)
                .show();
    }

    public class UpdateSlots implements Runnable {
        public TextView view;
        public ImageView imageView;
        public int randomDelay;
        public int time;

        UpdateSlots(TextView view, ImageView imageView) {
            this.view = view;
            this.imageView = imageView;
            this.randomDelay = 500;
            this.time = 0;
        }

        @Override
        public void run() {
            if (time == 3) {
                time = 0;
            } else {
                time++;
            }

            view.setText(String.valueOf(time));
            imageView.setImageDrawable(drawables[time]);
            handler.postDelayed(this, randomDelay);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isSpinning", spinButton.getText().equals("STOP"));

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean("isSpinning"))
            onClickSpinButton(spinButton);

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void vibrate(int duration) {
        vibrate(duration, VibrationEffect.DEFAULT_AMPLITUDE);
    }

    private void vibrate(int duration, int vibrationEffect, int repeat) {
        for (int i = 0; i < repeat; i++) {
            vibrate(duration, vibrationEffect);
            // wait before next vibration
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void vibrate(int duration, int vibrationEffect) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for duration amount of milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duration, vibrationEffect));
        } else {
            //deprecated in API 26
            v.vibrate(duration);
        }
    }
}