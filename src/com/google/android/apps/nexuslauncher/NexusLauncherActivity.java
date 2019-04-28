package com.google.android.apps.nexuslauncher;

import android.animation.AnimatorSet;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.ViewOnDrawExecutor;
import com.google.android.libraries.launcherclient.GoogleNow;

import org.zimmob.zimlx.settings.ui.SettingsActivity;

import java.util.List;

public class NexusLauncherActivity extends Launcher {
    private final static String PREF_IS_RELOAD = "pref_reload_workspace";
    public NexusLauncher mLauncher;
    private boolean mIsReload;

    public NexusLauncherActivity() {
        mLauncher = new NexusLauncher(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FeatureFlags.QSB_ON_FIRST_SCREEN = showSmartspace();
        super.onCreate(savedInstanceState);
        mContext = this;
        SharedPreferences prefs = Utilities.getPrefs(this);
        if (mIsReload = prefs.getBoolean(PREF_IS_RELOAD, false)) {
            prefs.edit().remove(PREF_IS_RELOAD).apply();
            getWorkspace().setCurrentPage(0);
            showOverviewMode(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FeatureFlags.QSB_ON_FIRST_SCREEN != showSmartspace()) {
            Utilities.getPrefs(this).edit().putBoolean(PREF_IS_RELOAD, true).apply();
            if (Utilities.ATLEAST_NOUGAT) {
                recreate();
            } else {
                finish();
                startActivity(getIntent());
            }
        }
    }

    @Override
    public void clearPendingExecutor(ViewOnDrawExecutor executor) {
        super.clearPendingExecutor(executor);
        if (mIsReload) {
            mIsReload = false;
            showOverviewMode(false);
        }
    }

    private boolean showSmartspace() {
        return Utilities.getPrefs(this).getBoolean(SettingsActivity.SMARTSPACE_PREF, true);
    }

    public void overrideTheme(boolean isDark, boolean supportsDarkText) {
        int flags = Utilities.getDevicePrefs(this).getInt("pref_persistent_flags", 0);
        boolean forceDark = Utilities.getZimPrefs(this).getOverrideLauncherTheme();
        int orientFlag = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 16 : 8;
        boolean useGoogleInOrientation = (orientFlag & flags) != 0;
        if (forceDark) {
            setTheme(R.style.GoogleSearchLauncherThemeDark);
        } else {
            if ((useGoogleInOrientation && isDark)) {
                setTheme(R.style.GoogleSearchLauncherThemeDark);
            } else if (useGoogleInOrientation && supportsDarkText && Utilities.ATLEAST_NOUGAT) {
                setTheme(R.style.GoogleSearchLauncherThemeDarkText);
            } else if (useGoogleInOrientation) {
                setTheme(R.style.GoogleSearchLauncherTheme);
            } else {
                super.overrideTheme(isDark, supportsDarkText);
            }
        }
    }

    public GoogleNow getGoogleNow() {
        return mLauncher.fy;
    }

    public List<ComponentKeyMapper<AppInfo>> getPredictedApps() {
        return mLauncher.fA.getPredictedApps();
    }

    public void playQsbAnimation() {
        mLauncher.mQsbAnimationController.dZ();
    }

    public AnimatorSet openQsb() {
        return mLauncher.mQsbAnimationController.openQsb();
    }
}