/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.WindowManager;

import org.mozilla.focus.R;
import org.mozilla.focus.provider.SettingPreferenceWrapper;
import org.mozilla.focus.search.SearchEngine;

import java.util.Set;

/**
 * A simple wrapper for SharedPreferences that makes reading preference a little bit easier.
 */
public class Settings {

    public static final int STORAGE_MSG_TYPE_REMOVABLE_UNAVAILABLE = 0x9527; // beautiful random number
    public static final int STORAGE_MSG_TYPE_REMOVABLE_AVAILABLE = 0x5987;

    private static Settings instance;
    private static final boolean BLOCK_IMAGE_DEFAULT = false;
    private static final boolean TURBO_MODE_DEFAULT = true;
    private static final boolean NIGHT_MODE_DEFAULT = false;
    private static final boolean DID_SHOW_RATE_APP_DEFAULT = false;
    private static final boolean DID_SHOW_SHARE_APP_DEFAULT = false;

    public final static int PRIORITY_SYSTEM = 0;
    public final static int PRIORITY_FIREBASE = 1;
    public final static int PRIORITY_USER = 2;

    @IntDef({PRIORITY_SYSTEM, PRIORITY_FIREBASE, PRIORITY_USER})
    public @interface SettingPriority {

    }
    public synchronized static Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context.getApplicationContext());
        }
        return instance;
    }

    private final SharedPreferences preferences;
    private final Resources resources;
    private final EventHistory eventHistory;
    private final NewFeatureNotice newFeatureNotice;
    private final SettingPreferenceWrapper settingPreferenceWrapper;

    private Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        resources = context.getResources();
        eventHistory = new EventHistory(preferences);
        newFeatureNotice = NewFeatureNotice.getInstance(context);
        settingPreferenceWrapper = new SettingPreferenceWrapper(context.getContentResolver());
    }

    public boolean shouldBlockImages() {
        return preferences.getBoolean(
                resources.getString(R.string.pref_key_performance_block_images),
                BLOCK_IMAGE_DEFAULT);
    }

    public void setBlockImages(boolean blockImages) {
        final String key = getPreferenceKey(R.string.pref_key_performance_block_images);
        preferences.edit().putBoolean(key, blockImages).apply();
    }

    public boolean isNightModeEnable() {
        return settingPreferenceWrapper.getBoolean(resources.getString(R.string.pref_key_night_mode_enable),
                NIGHT_MODE_DEFAULT);
    }

    public void setNightMode(boolean enable) {
        final String key = getPreferenceKey(R.string.pref_key_night_mode_enable);
        preferences.edit().putBoolean(key, enable).apply();
    }

    public void setNightModeSpotlight(boolean enabled) {
        final String key = getPreferenceKey(R.string.pref_key_night_mode_brightness_dirty);
        preferences.edit().putBoolean(key, enabled).apply();
    }

    public boolean showNightModeSpotlight() {
        return settingPreferenceWrapper.getBoolean(resources.getString(R.string.pref_key_night_mode_brightness_dirty),
                false);
    }

    public boolean shouldShowFirstrun() {
        return newFeatureNotice.shouldShowLiteUpdate() || !newFeatureNotice.hasShownFirstRun();
    }

    public boolean shouldSaveToRemovableStorage() {
        // FIXME: rely on String-array-order is not a good idea
        final String[] defined = resources.getStringArray(R.array.data_saving_path_values);

        final String key = getPreferenceKey(R.string.pref_key_storage_save_downloads_to);
        final String value = preferences.getString(key, defined[0]);

        return defined[0].equals(value); // assume the first item is for removable storage
    }

    public boolean shouldUseTurboMode() {
        return preferences.getBoolean(
                resources.getString(R.string.pref_key_turbo_mode),
                TURBO_MODE_DEFAULT);
    }

    public void setTurboMode(boolean toEnable) {
        final String key = getPreferenceKey(R.string.pref_key_turbo_mode);
        preferences.edit().putBoolean(key, toEnable).apply();
    }


    public void setRemovableStorageStateOnCreate(boolean exist) {
        final String key = getPreferenceKey(R.string.pref_key_removable_storage_available_on_create);
        preferences.edit().putBoolean(key, exist).apply();
    }

    public boolean getRemovableStorageStateOnCreate() {
        final String key = getPreferenceKey(R.string.pref_key_removable_storage_available_on_create);
        return preferences.getBoolean(key, false);
    }

    public int getShowedStorageMessage() {
        final String key = getPreferenceKey(R.string.pref_key_showed_storage_message);
        return preferences.getInt(key, STORAGE_MSG_TYPE_REMOVABLE_UNAVAILABLE);
    }

    public void setShowedStorageMessage(final int type) {
        if (type != STORAGE_MSG_TYPE_REMOVABLE_AVAILABLE
                && type != STORAGE_MSG_TYPE_REMOVABLE_UNAVAILABLE) {
            throw new RuntimeException("Unknown message type");
        }

        final String key = getPreferenceKey(R.string.pref_key_showed_storage_message);
        preferences.edit()
                .putInt(key, type)
                .apply();
    }

    @Nullable
    public String getNewsSource() {
        return preferences.getString(getPreferenceKey(R.string.pref_s_news), null);
    }

    public void setNewsSource(String source) {
        preferences.edit()
                .putString(getPreferenceKey(R.string.pref_s_news), source)
                .apply();
    }

    @Nullable
    public String getDefaultSearchEngineName() {
        return preferences.getString(getPreferenceKey(R.string.pref_key_search_engine), null);
    }

    public boolean didShowRateAppDialog() {
        return preferences.getBoolean(getPreferenceKey(R.string.pref_key_did_show_rate_app_dialog), DID_SHOW_RATE_APP_DEFAULT);
    }

    public void setRateAppDialogDidShow() {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_key_did_show_rate_app_dialog), true)
                .apply();
    }

    public void setRateAppDialogDidDismiss() {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_key_did_dismiss_rate_app_dialog), true)
                .apply();
    }

    public void setRateAppNotificationDidShow() {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_key_did_show_rate_app_notification), true)
                .apply();
    }

    public int getMenuPreferenceClickCount() {
        return preferences.getInt(getPreferenceKey(R.string.pref_key_setting_click_counter), 0);
    }

    public void addMenuPreferenceClickCount() {
        preferences.edit().putInt(getPreferenceKey(R.string.pref_key_setting_click_counter), getMenuPreferenceClickCount() + 1).apply();
    }

    public boolean isDefaultBrowserSettingDidShow() {
        return preferences.getBoolean(getPreferenceKey(R.string.pref_key_did_show_default_browser_setting), false);
    }

    public void setDefaultBrowserSettingDidShow() {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_key_did_show_default_browser_setting), true)
                .apply();
    }


    public boolean hasUnreadMyShot() {
        return preferences.getBoolean(getPreferenceKey(R.string.pref_has_unread_my_shot), false);
    }

    public void setHasUnreadMyShot(boolean hasUnreadMyShot) {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_has_unread_my_shot), hasUnreadMyShot)
                .apply();
    }

    public boolean didShowShareAppDialog() {
        return preferences.getBoolean(getPreferenceKey(R.string.pref_key_did_show_share_app_dialog), DID_SHOW_SHARE_APP_DEFAULT);
    }

    public void setShareAppDialogDidShow() {
        preferences.edit()
                .putBoolean(getPreferenceKey(R.string.pref_key_did_show_share_app_dialog), true)
                .apply();
    }

    public void increaseAppCreateCounter() {
        int count = getAppCreateCount();
        preferences.edit()
                .putInt(getPreferenceKey(R.string.pref_key_app_create_counter), ++count)
                .apply();
    }

    public int getAppCreateCount() {
        return preferences.getInt(getPreferenceKey(R.string.pref_key_app_create_counter), 0);
    }

    public void setDefaultSearchEngine(SearchEngine searchEngine) {
        preferences.edit()
                .putString(getPreferenceKey(R.string.pref_key_search_engine), searchEngine.getName())
                .apply();
    }

    public float getNightModeBrightnessValue() {
        return settingPreferenceWrapper.getFloat(getPreferenceKey(R.string.pref_key_brightness), WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    public void setNightModeBrightnessValue(float value) {
        preferences.edit().putFloat(getPreferenceKey(R.string.pref_key_brightness), value).apply();
    }

    public static void updatePrefDefaultBrowserIfNeeded(Context context, boolean isDefaultBrowser) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final Set<String> keySet = sharedPreferences.getAll().keySet();
        final String prefDefaultBrowser = context.getResources().getString(R.string.pref_key_default_browser);
        //  Update current default browser value, keep null if never set as true
        if (keySet.contains(prefDefaultBrowser) || isDefaultBrowser) {
            sharedPreferences.edit().putBoolean(prefDefaultBrowser, isDefaultBrowser).apply();
        }
    }

    public static void updatePrefString(Context context, String key, String value) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key, value).apply();
    }

    /* package */ String getPreferenceKey(int resourceId) {
        return resources.getString(resourceId);
    }

    public boolean canOverride(String prefKey, @Settings.SettingPriority int priority) {
        final int currPriority = preferences.getInt(prefKey, Integer.MAX_VALUE);
        return priority > currPriority;
    }

    public void setPriority(String prefKey, @Settings.SettingPriority int priority) {
        preferences.edit().putInt(prefKey, priority).apply();
    }


    public EventHistory getEventHistory() {
        return eventHistory;
    }

    public static class Event {
        public static final String AppCreate = "app_create";

        public static final String ShowShareAppDialog = "show_share_app_dialog";
        public static final String ShowRateAppDialog = "show_rate_app_dialog";
        public static final String DismissRateAppDialog = "dismiss_rate_app_dialog";
        public static final String ShowRateAppNotification = "show_rate_app_notification";

        public static final String PostSurveyNotification = "post_survey_notification";
        public static final String ShowMyShotOnBoardingDialog = "show_my_shot_on_boarding_dialog";
        public static final String FeatureSurveyWifiFinding = "feature_survey_wifi_finding";
        public static final String FeatureSurveyVpn = "feature_survey_vpn";

        // Indicate the express vpn app was downloaded before, it may or may not in the device right now
        public static final String VpnAppWasDownloaded = "vpn_app_was_downloaded";
        public static final String VpnRecommenderIgnore = "vpn_recommender_ignore";

        public static final String ShowDownloadIndicatorIntro = "dl_indicator_intro";
    }

    public static class EventHistory {
        private SharedPreferences preferences;

        private EventHistory(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        public int getCount(String eventName) {
            String key = "pref_" + eventName + "_counter";
            return preferences.getInt(key, 0);
        }

        public boolean contains(String eventName) {
            String oldKey = "pref_did_" + eventName;
            if (preferences.contains(oldKey)) {
                return preferences.getBoolean(oldKey, false);
            }

            String newKey = "pref_" + eventName + "_counter";
            return preferences.getInt(newKey, 0) > 0;
        }

        public void add(String eventName) {
            setCount(eventName, getCount(eventName) + 1);
        }

        @VisibleForTesting
        public void removeCount(String eventName) {
            String key = "pref_" + eventName + "_counter";
            preferences.edit().remove(key).apply();
        }

        @VisibleForTesting
        public void setCount(String eventName, int value) {
            String key = "pref_" + eventName + "_counter";
            preferences.edit().putInt(key, value).apply();
        }

        @VisibleForTesting
        public void clear() {
            preferences.edit().clear().apply();
        }
    }
}
