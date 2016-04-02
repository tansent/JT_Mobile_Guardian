package com.jingtian.mobileguardian.engine;

import java.util.ArrayList;
import java.util.List;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jingtian.mobileguardian.R;
import com.jingtian.mobileguardian.domain.ProcessInfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ProcessInfoProvider2 {

	/**
     * work and only work above 5.0
     * need to import "com.jaredrummler.android.processes" and "com.jaredrummler.android.processes.models"
     * and all their classes to make it work
     * 
     * @param context
     * @return
     */
    public static List<ProcessInfo> getTaskInfos1(Context context) {
        // app manager
        ActivityManager am = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);

        // package manager
        PackageManager pm = context.getPackageManager();

          // obtain all processes that are currently running
        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();

        List<ProcessInfo> taskinfos = new ArrayList<ProcessInfo>();
        // traverse all processInfo, and extract only what we need
        for (AndroidAppProcess processInfo : processInfos) {
        	ProcessInfo processinfo = new ProcessInfo();
            // package name(ID)
            String packname = processInfo.name;
            processinfo.setPackageName(packname);
            
            android.os.Debug.MemoryInfo[] memoryInfos = am
                    .getProcessMemoryInfo(new int[] { processInfo.pid });
            long memsize = memoryInfos[0].getTotalPrivateDirty() * 1024L;
            processinfo.setMemSize(memsize);
            try {
                // app info
                ApplicationInfo applicationInfo = pm.getApplicationInfo(
                        packname, 0);
                Drawable icon = applicationInfo.loadIcon(pm);
                processinfo.setIcon(icon);
                String name = applicationInfo.loadLabel(pm).toString();
                processinfo.setProcessName(name);
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    // user process
                	processinfo.setUserProcess(true);
                } else {
                    // system process
                	processinfo.setUserProcess(false);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                // system kernel processes, no package name
                processinfo.setProcessName(packname);
                Drawable icon = context.getResources().getDrawable(
                        R.drawable.ic_default);
                processinfo.setIcon(icon);
            }
            if (processinfo != null) {
                taskinfos.add(processinfo);
            }
        }
        return taskinfos;
    }

}
