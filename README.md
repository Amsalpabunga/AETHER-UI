# AETHER UI (starter project)

Starter project for the AETHER UI LSPosed module.

## Current scope
- LSPosed entry point for `com.android.systemui`
- Passive edge-swipe detector on `NotificationShadeWindowView.onInterceptTouchEvent`
- Logs `GESTURE_LEFT` and `GESTURE_RIGHT`
- Does not consume or replace SystemUI touch events

## Build
This project includes a GitHub Actions workflow at `.github/workflows/build.yml`.

1. Create a GitHub repository.
2. Upload the contents of this folder to the repository root.
3. Open the **Actions** tab.
4. Run **Build AETHER UI APK** (or push to `main`).
5. Download the `AETHER-UI-debug` artifact from the completed workflow.
6. Extract the artifact to get `app-debug.apk`.

## Important
This is only the initial detector build. It does not yet draw a Control Center or Notification Center.
Test the passive detector first. Do not enable touch interception until the gesture behavior is verified on-device.
