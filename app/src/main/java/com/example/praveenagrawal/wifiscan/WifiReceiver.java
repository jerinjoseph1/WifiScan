package com.example.praveenagrawal.wifiscan;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by praveen.agrawal on 30/01/17.
 */

public class WifiReceiver extends BroadcastReceiver
{

    public void onReceive(Context c, Intent intent)
    {
        Log.w("Scan","receive Scan");
        AudioManager audioManager = (AudioManager) c.getSystemService(AUDIO_SERVICE);
        WifiManager wifiManager = (WifiManager) c.getSystemService (c.WIFI_SERVICE);
        WifiScanDbHelper mDbHelper = new WifiScanDbHelper(c);
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL &&  audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
        {
            return;
        }
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
        {
            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted()) {

                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            {
                return;
            }
        }
        if (wifiManager.isWifiEnabled() == false)
        {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mDbHelper.updateOutTime();
            return;
        }
        WifiInfo info = wifiManager.getConnectionInfo ();
        if (info == null)
        {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mDbHelper.updateOutTime();
            return;
        }
        if (info.getSSID() == null)
        {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mDbHelper.updateOutTime();
            return;
        }
        String ssid  = info.getSSID().trim().replaceAll("\"","");
        ArrayList<FeedEntryData> selectList = mDbHelper.getSavedList();
        Boolean didFindSSID = false;
        for (int i = 0; i < selectList.size(); i++)
        {
            if (ssid.equals(selectList.get(i).ssid))
            {
                didFindSSID = true;
                if (selectList.get(i).isTime.equals("true"))
                {
                    mDbHelper.addInTime(selectList.get(i).ssid);
                }
                List<TimeEntryData> abc = mDbHelper.getTimeEntryList(selectList.get(i).ssid);
                switch (selectList.get(i).type)
                {
                    case "1":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        Log.w("Scan","RINGER_MODE_SILENT");
                        return;
                    case "2":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        Log.w("Scan","RINGER_MODE_VIBRATE");
                        return;
                    case "3":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        Log.w("Scan","RINGER_MODE_NORMAL");
                        return;
                }

            }
        }
        if (!didFindSSID)
        {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mDbHelper.updateOutTime();
            return;
        }
    }
}
