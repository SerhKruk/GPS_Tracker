package com.example.atry;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;


public class VibrationHelper {
    private Context context;
    private Vibrator vibrator;

    public VibrationHelper(Context _context)
    {
        context = _context;
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    public void vibrate()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(500);
        }
    }
}
