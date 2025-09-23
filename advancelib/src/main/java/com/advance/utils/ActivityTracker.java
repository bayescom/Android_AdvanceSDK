package com.advance.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActivityTracker implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "【ActivityTracker】";

    private static volatile ActivityTracker instance;

    private WeakReference<Activity> currentActivity;
    private List<String> activityStack = new CopyOnWriteArrayList<>();
    private List<LogListener> listeners = new ArrayList<>();

    private ActivityTracker() {
    }

    public static ActivityTracker getInstance() {
        if (instance == null) {
            synchronized (ActivityTracker.class) {
                if (instance == null) {
                    instance = new ActivityTracker();
                }
            }
        }
        return instance;
    }

    public void initialize(Context context) {
        try {
            Application application = (Application) context.getApplicationContext();
            application.registerActivityLifecycleCallbacks(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Activity getCurrentActivity() {
        return currentActivity != null ? currentActivity.get() : null;
    }

    public List<String> getActivityStack() {
        return new ArrayList<>(activityStack);
    }

    public void addLogListener(LogListener listener) {
        listeners.add(listener);
    }

    public void removeLogListener(LogListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logMessage = "[" + timestamp + "] " + message;

        LogUtil.d(TAG+ logMessage);
        for (LogListener listener : listeners) {
            listener.onLogReceived(logMessage);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String activityName = activity.getClass().getSimpleName();
        activityStack.add(activityName);
        notifyListeners(activityName + " - onCreate()");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        notifyListeners(activityName + " - onStart()");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        currentActivity = new WeakReference<>(activity);
        notifyListeners(activityName + " - onResume() - 当前Activity");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        notifyListeners(activityName + " - onPause()");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        notifyListeners(activityName + " - onStop()");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        String activityName = activity.getClass().getSimpleName();
        notifyListeners(activityName + " - onSaveInstanceState()");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        activityStack.remove(activityName);

        // 如果销毁的是当前Activity，清除引用
        if (currentActivity != null && currentActivity.get() == activity) {
            currentActivity = null;
        }

        notifyListeners(activityName + " - onDestroy()");
    }

    public interface LogListener {
        void onLogReceived(String message);
    }
}