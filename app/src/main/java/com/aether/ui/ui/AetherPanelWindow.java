package com.aether.ui.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;
import java.util.WeakHashMap;

public final class AetherPanelWindow {
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private static final Map<ViewGroup, FrameLayout> ACTIVE =
        new WeakHashMap<>();

    private AetherPanelWindow() {}

    private static int dp(Context c, float value) {
        return (int) (value *
            c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable shape(
            int color, int stroke, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        bg.setStroke(1, stroke);
        return bg;
    }

    private static TextView text(
            Context c, String value, float size, int color, boolean bold) {
        TextView v = new TextView(c);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private static void close(ViewGroup host, FrameLayout panel) {
        host.removeView(panel);
        ACTIVE.remove(host);
    }

    private static void openSettings(
            Context c, String action, String fallback) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intent);
        } catch (Throwable error) {
            try {
                Intent intent = new Intent(fallback);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(intent);
            } catch (Throwable ignored) {
            }
        }
    }

    private static LinearLayout tile(
            Context c, String title, String subtitle,
            String action, String fallback) {

        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(c, 14), dp(c, 10),
            dp(c, 14), dp(c, 10));
        box.setBackground(shape(
            Color.argb(210, 48, 57, 76),
            Color.argb(55, 255, 255, 255),
            dp(c, 18)
        ));
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(v ->
            openSettings(c, action, fallback));

        box.addView(text(c, title, 15,
            Color.WHITE, true));

        TextView sub = text(c, subtitle, 11,
            Color.rgb(190, 200, 216), false);
        box.addView(sub);

        return box;
    }

    private static void addTileRow(
            Context c, LinearLayout parent,
            String a, String as, String aa, String af,
            String b, String bs, String ba, String bf) {

        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(0, dp(c, 76), 1f);
        p.setMargins(0, 0, dp(c, 7), dp(c, 7));

        row.addView(tile(c, a, as, aa, af), p);

        LinearLayout.LayoutParams p2 =
            new LinearLayout.LayoutParams(0, dp(c, 76), 1f);
        p2.setMargins(dp(c, 7), 0, 0, dp(c, 7));

        row.addView(tile(c, b, bs, ba, bf), p2);
        parent.addView(row);
    }

    public static void show(View anchor, int side) {
        if (!(anchor instanceof ViewGroup)) return;

        if (android.os.Looper.myLooper() !=
                android.os.Looper.getMainLooper()) {
            anchor.post(() -> show(anchor, side));
            return;
        }

        ViewGroup host = (ViewGroup) anchor;
        Context c = host.getContext();

        FrameLayout previous = ACTIVE.get(host);
        if (previous != null) close(host, previous);

        boolean control = side == RIGHT;

        FrameLayout overlay = new FrameLayout(c);
        overlay.setTag("AETHER_PANEL");
        overlay.setClickable(control);
        overlay.setFocusable(control);
        overlay.setBackgroundColor(control
            ? Color.argb(135, 4, 8, 18)
            : Color.TRANSPARENT);

        if (control) {
            overlay.setOnClickListener(v ->
                close(host, overlay));
        }

        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(c, 17), dp(c, 15),
            dp(c, 17), dp(c, 15));
        card.setBackground(shape(
            Color.argb(242, 24, 31, 46),
            Color.argb(65, 255, 255, 255),
            dp(c, 26)
        ));
        card.setElevation(dp(c, 12));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout header = new LinearLayout(c);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout heading = new LinearLayout(c);
        heading.setOrientation(LinearLayout.VERTICAL);

        heading.addView(text(c, "AETHER",
            19, Color.WHITE, true));

        heading.addView(text(c,
            control ? "CONTROL CENTER" : "NOTIFICATION CENTER",
            10, Color.rgb(160, 180, 205), true));

        header.addView(heading,
            new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView close = text(c, "×", 27,
            Color.WHITE, false);
        close.setGravity(Gravity.CENTER);
        close.setBackground(shape(
            Color.argb(65, 255, 255, 255),
            Color.TRANSPARENT, dp(c, 30)));
        header.addView(close,
            new LinearLayout.LayoutParams(dp(c, 38), dp(c, 38)));

        close.setOnClickListener(v -> close(host, overlay));
        card.addView(header);

        View separator = new View(c);
        separator.setBackgroundColor(
            Color.argb(45, 255, 255, 255));
        LinearLayout.LayoutParams sep =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 1));
        sep.setMargins(0, dp(c, 13), 0, dp(c, 13));
        card.addView(separator, sep);

        if (control) {
            card.addView(text(c, "QUICK ACCESS",
                11, Color.rgb(175, 190, 210), true));

            View space = new View(c);
            card.addView(space,
                new LinearLayout.LayoutParams(1, dp(c, 9)));

            addTileRow(c, card,
                "Internet", "Network settings",
                Settings.Panel.ACTION_INTERNET_CONNECTIVITY,
                Settings.ACTION_WIFI_SETTINGS,
                "Bluetooth", "Bluetooth settings",
                Settings.Panel.ACTION_BLUETOOTH,
                Settings.ACTION_BLUETOOTH_SETTINGS);

            addTileRow(c, card,
                "Sound", "Volume controls",
                Settings.Panel.ACTION_VOLUME,
                Settings.ACTION_SOUND_SETTINGS,
                "Display", "Screen settings",
                Settings.ACTION_DISPLAY_SETTINGS,
                Settings.ACTION_DISPLAY_SETTINGS);

            addTileRow(c, card,
                "Battery", "Power settings",
                Settings.ACTION_BATTERY_SAVER_SETTINGS,
                Settings.ACTION_BATTERY_SAVER_SETTINGS,
                "Settings", "System settings",
                Settings.ACTION_SETTINGS,
                Settings.ACTION_SETTINGS);

        } else {
            card.addView(text(c, "NOTIFICATIONS",
                11, Color.rgb(175, 190, 210), true));

            View space = new View(c);
            card.addView(space,
                new LinearLayout.LayoutParams(1, dp(c, 8)));

            card.addView(text(c,
                "Native Android notifications remain active below.",
                13, Color.WHITE, false));
        }

        FrameLayout.LayoutParams cardParams =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | (control ? Gravity.END : Gravity.START)
            );

        cardParams.setMargins(
            dp(c, 12), dp(c, 28), dp(c, 12), dp(c, 12));

        overlay.addView(card, cardParams);

        host.addView(overlay,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ACTIVE.put(host, overlay);
    }
}
