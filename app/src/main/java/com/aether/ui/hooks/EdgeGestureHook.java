package com.aether.ui.hooks;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class EdgeGestureHook {
    private static final String TAG = "AETHER-EDGE";
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private static final Map<View, Track> TRACKS = new WeakHashMap<>();

    private static final class Track {
        float startX;
        float startY;
        int side;
        boolean reported;
        boolean debugMoved;
    }

    private static float dp(View view, float value) {
        return value * view.getResources().getDisplayMetrics().density;
    }

    private EdgeGestureHook() {}

    public static void install(ClassLoader loader) {
        XposedHelpers.findAndHookMethod(
            "com.android.systemui.shade.NotificationShadeWindowView",
            loader,
            "onInterceptTouchEvent",
            MotionEvent.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    MotionEvent event = (MotionEvent) param.args[0];
                    int action = event.getActionMasked();

                    if (action == MotionEvent.ACTION_DOWN) {
                        float x = event.getX();
                        float y = event.getY();

                        if (y <= dp(view, 180f)) {
                            XposedBridge.log("AETHER-EDGE: DOWN x=" + x +
                                " y=" + y + " width=" + view.getWidth());
                        }

                        float edge = dp(view, 112f);
                        float top = dp(view, 72f);

                        Track track = new Track();

                        if (y <= top && x <= edge) {
                            track.side = LEFT;
                        } else if (y <= top && x >= view.getWidth() - edge) {
                            track.side = RIGHT;
                        }

                        if (track.side != 0) {
                            track.startX = x;
                            track.startY = y;
                            TRACKS.put(view, track);

                            XposedBridge.log("AETHER-EDGE: TRACK side=" +
                                (track.side == LEFT ? "LEFT" : "RIGHT"));
                        } else {
                            TRACKS.remove(view);
                        }

                    } else if (action == MotionEvent.ACTION_MOVE) {
                        Track track = TRACKS.get(view);

                        if (track == null || track.reported ||
                                event.getPointerCount() != 1) {
                            return;
                        }

                        if (!track.debugMoved) {
                            track.debugMoved = true;
                            XposedBridge.log("AETHER-EDGE: MOVE received");
                        }

                        float dx = event.getX() - track.startX;
                        float dy = event.getY() - track.startY;

                        if (dy >= dp(view, 64f) &&
                                dy > Math.abs(dx) * 1.15f) {
                            track.reported = true;
                            String message = track.side == LEFT
                                ? "GESTURE_LEFT"
                                : "GESTURE_RIGHT";
                            Log.i(TAG, message);
                            XposedBridge.log("AETHER-EDGE: " + message);
                        }

                    } else if (action == MotionEvent.ACTION_UP ||
                            action == MotionEvent.ACTION_CANCEL ||
                            action == MotionEvent.ACTION_POINTER_DOWN) {
                        TRACKS.remove(view);
                    }
                }
            }
        );

        XposedBridge.log("AETHER UI: edge gesture detector installed");
    }
}
