package com.pleiades.pleione.alcyone.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {
    // The names of the SharedPreferences groups that the application maintains.
    // These are the same strings that are passed to Context #getSharedPreferences(String, int).
    static final String PREFS_NAME = "prefs";

    // An arbitrary string used within the BackupAgentHelper implementation to
    // identify the SharedPreferenceBackupHelper's data.
    static final String MY_PREFS_BACKUP_KEY = "userPrefs";

    // Allocate a helper and install it.

    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS_NAME);
        addHelper(MY_PREFS_BACKUP_KEY, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
    }
}