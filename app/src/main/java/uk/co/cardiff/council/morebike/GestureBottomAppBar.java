package uk.co.cardiff.council.morebike;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import uk.co.cardiff.council.morebike.R;

/**
 * A {@link GestureBottomAppBar} inheriting from {@link BottomAppBar}
 */
public class GestureBottomAppBar extends BottomAppBar {

        private static final String COMPONENT_TAG = "GestureAppBar";
        private final GestureDetector gestureDetector;
        private List<FlingListener> listeners = new ArrayList<>();

    public GestureBottomAppBar(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        gestureDetector= new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > 0) {
                                flingUpNotifyListeners();
                            }
                        } catch (Exception e) {
                            Log.e(COMPONENT_TAG, e.getMessage());
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
    }

    public void setOnFlingUpListener(FlingListener listener) {
        listeners.add(listener);
    }

    private void flingUpNotifyListeners() {
        for (FlingListener listener : listeners) {
            listener.onFlingUp();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

}