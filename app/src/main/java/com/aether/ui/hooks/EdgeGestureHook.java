package com.aether.ui.hooks;

import android.view.MotionEvent;
import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;

import com.aether.ui.ui.AetherPanelWindow;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class EdgeGestureHook {
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private static final Map<View, Track> TRACKS =
        new WeakHashMap<>();

    private static final class Track {
        float startX;
        float startY;
        int side;
        boolean reported;
    }

    private static float dp(View view, float value) {
        return value * view.getResources()
            .getDisplayMetrics().density;
    }

    private EdgeGestureHook() {}

    private static void observe(
            View view, MotionEvent event, String source) {

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if ("onTouchEvent".equals(source) &&
                    TRACKS.containsKey(view)) return;

            float x = event.getX();
            float y = event.getY();

            Track track = new Track();
            float edge = dp(view, 112f);
            float top = dp(view, 72f);

            if (y <= top && x <= edge) {
                track.side = LEFT;
            } else if (y <= top &&
                    x >= view.getWidth() - edge) {
                track.side = RIGHT;
            }

            if (track.side != 0) {
                track.startX = x;
                track.startY = y;
                TRACKS.put(view, track);

                XposedBridge.log("AETHER-EDGE: TRACK " +
                    (track.side == LEFT ? "LEFT" : "RIGHT") +
                    " source=" + source);
            } else {
                TRACKS.remove(view);
            }

        } else if (action == MotionEvent.ACTION_MOVE) {
            Track track = TRACKS.get(view);

            if (track == null || track.reported ||
                    event.getPointerCount() != 1) return;

            float dx = event.getX() - track.startX;
            float dy = event.getY() - track.startY;

            if (dy >= dp(view, 64f) &&
                    dy > Math.abs(dx) * 1.15f) {
                track.reported = true;

                XposedBridge.log("AETHER-EDGE: GESTURE_" +
                    (track.side == LEFT ? "LEFT" : "RIGHT") +
                    " source=" + source);
            }

        } else if (action == MotionEvent.ACTION_UP) {
            Track track = TRACKS.get(view);

            if (track != null) {
                XposedBridge.log("AETHER-EDGE: END side=" +
                    (track.side == LEFT ? "LEFT" : "RIGHT") +
                    " reported=" + track.reported +
                    " source=" + source);

                if (track.reported) {
                    AetherPanelWindow.show(view, track.side);
                }
            }

            TRACKS.remove(view);

        } else if (action == MotionEvent.ACTION_CANCEL ||
                action == MotionEvent.ACTION_POINTER_DOWN) {
            TRACKS.remove(view);
        }
    }

    public static void install(ClassLoader loader) {
        XC_MethodHook observer = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                observe(
                    (View) param.thisObject,
                    (MotionEvent) param.args[0],
                    param.method.getName()
                );
            }
        };

        XposedHelpers.findAndHookMethod(
            "com.android.systemui.shade.NotificationShadeWindowView",
            loader, "onInterceptTouchEvent",
            MotionEvent.class, observer
        );

        XposedHelpers.findAndHookMethod(
            "com.android.systemui.shade.NotificationShadeWindowView",
            loader, "onTouchEvent",
            MotionEvent.class, observer
        );

        XposedBridge.log(
            "AETHER UI: gesture and panel hooks installed"
        );
    }
}
