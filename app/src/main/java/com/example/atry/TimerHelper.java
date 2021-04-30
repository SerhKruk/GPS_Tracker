package com.example.atry;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class TimerHelper {
    public Timer timer;
    public TimerTask timerTask;

    public Double time = 0.0;
    public boolean timerStarted = false;
    public Context context;
    public TextView timerView;

    public TimerHelper(Context _context) {
        context = _context;
        timer = new Timer();
        timerView = (TextView) ((Activity)context).findViewById(R.id.idTimer);
    }

//    public void init()
//    {
//        timer = new Timer();
//    }

    public void stopTimer()
    {
        if(timerStarted)
            timerTask.cancel();
        timerStarted = false;
        time = 0.0;
    }

    public void startTimer()
    {
        timerStarted = true;
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                ((Activity)context).runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        timerView.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }


    public String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    public String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }
}

