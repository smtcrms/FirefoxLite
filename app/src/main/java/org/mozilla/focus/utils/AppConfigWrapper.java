/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.rocket.chrome.BottomBarItemAdapter;
import org.mozilla.rocket.content.ecommerce.data.Coupon;
import org.mozilla.rocket.content.ecommerce.data.CouponKey;
import org.mozilla.rocket.content.ecommerce.data.ShoppingLink;
import org.mozilla.rocket.content.ecommerce.data.ShoppingLinkKey;

import java.util.ArrayList;
import java.util.List;

public class AppConfigWrapper {
    static final int SURVEY_NOTIFICATION_POST_THRESHOLD = 3;
    static final boolean PRIVATE_MODE_ENABLED_DEFAULT = true;
    static final boolean LIFE_FEED_ENABLED_DEFAULT = false;
    static final String STR_NEWS_PROVIDERS_DEFAULT = "";
    static final String STR_E_COMMERCE_SHOPPINGLINKS_DEFAULT = "";
    static final String STR_E_COMMERCE_COUPONS_DEFAULT = "";
    static final String STR_E_COMMERCE_COUPON_BANNER_DEFAULT = "";


    /* Disabled since v1.0.4, keep related code in case we want to enable it again in the future */
    private static final boolean SURVEY_NOTIFICATION_ENABLED = false;
    static final int DRIVE_DEFAULT_BROWSER_FROM_MENU_SETTING_THRESHOLD = 2;

    public static long getRateAppNotificationLaunchTimeThreshold() {
        return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.RATE_APP_NOTIFICATION_THRESHOLD);
    }

    public static long getShareDialogLaunchTimeThreshold(final boolean needExtend) {
        if (needExtend) {
            return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.SHARE_APP_DIALOG_THRESHOLD) +
                    getRateAppNotificationLaunchTimeThreshold() -
                    getRateDialogLaunchTimeThreshold();
        }
        return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.SHARE_APP_DIALOG_THRESHOLD);
    }

    public static long getRateDialogLaunchTimeThreshold() {
        return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.RATE_APP_DIALOG_THRESHOLD);
    }

    public static int getSurveyNotificationLaunchTimeThreshold() {
        return SURVEY_NOTIFICATION_POST_THRESHOLD;
    }

    public static int getDriveDefaultBrowserFromMenuSettingThreshold() {
        return DRIVE_DEFAULT_BROWSER_FROM_MENU_SETTING_THRESHOLD;
    }

    public static boolean getMyshotUnreadEnabled() {
        return FirebaseHelper.getFirebase().getRcBoolean(FirebaseHelper.ENABLE_MY_SHOT_UNREAD);
    }

    public static boolean isSurveyNotificationEnabled() {
        return SURVEY_NOTIFICATION_ENABLED;
    }

    public static String getRateAppDialogTitle() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.RATE_APP_DIALOG_TEXT_TITLE);
    }

    public static String getRateAppDialogContent() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.RATE_APP_DIALOG_TEXT_CONTENT);
    }

    public static String getRateAppPositiveString() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.RATE_APP_DIALOG_TEXT_POSITIVE);
    }

    public static String getRateAppNegativeString() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.RATE_APP_DIALOG_TEXT_NEGATIVE);
    }

    public static String getBannerRootConfig() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.BANNER_MANIFEST);
    }

    public static long getFeatureSurvey() {
        return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.FEATURE_SURVEY);
    }

    public static String getScreenshotCategoryUrl() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.SCREENSHOT_CATEGORY_MANIFEST);
    }

    public static String getVpnRecommenderUrl() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.VPN_RECOMMENDER_URL);
    }

    public static String getVpnRecommenderPackage() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.VPN_RECOMMENDER_PACKAGE);
    }

    public static long getFirstLaunchWorkerTimer() {
        return FirebaseHelper.getFirebase().getRcLong(FirebaseHelper.FIRST_LAUNCH_TIMER_MINUTES);
    }

    public static String getFirstLaunchNotificationMessage() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.FIRST_LAUNCH_NOTIFICATION_MESSAGE);
    }

    /**
     * @return true if Content Portal News is enabled in Firebase Remote Config
     */
    public static boolean hasNewsPortal() {
        return FirebaseHelper.getFirebase().getRcBoolean(FirebaseHelper.ENABLE_LIFE_FEED);
    }

    public static boolean hasEcommerceShoppingLink() {
        return !getEcommerceShoppingLinks().isEmpty();
    }

    /**
     * Return a list of vouchers and shopping links for e-commerce content portal.
     * This is also used to determine if the user should see e-commerce or News in content portal.
     * In the future, the user may have both e-commerce and News. But now, let's make it simple.
     * @return ArrayList of shopping links or empty list if we encounter an error.
     */
    public static ArrayList<ShoppingLink> getEcommerceShoppingLinks() {
        ArrayList<ShoppingLink> shoppingLinks = new ArrayList<>();

        final String rcString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_E_COMMERCE_SHOPPINGLINKS);
        try {
            final JSONArray jsonArray = new JSONArray(rcString);
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject object = (JSONObject) jsonArray.get(i);
                shoppingLinks.add(toShoppingLink(object));
            }

        } catch (JSONException e) {
            // skip and do nothing
        }

        return shoppingLinks;
    }

    @NonNull
    private static ShoppingLink toShoppingLink(JSONObject object) {
        return new ShoppingLink(
                object.optString(ShoppingLinkKey.KEY_URL),
                object.optString(ShoppingLinkKey.KEY_NAME),
                object.optString(ShoppingLinkKey.KEY_IMAGE),
                object.optString(ShoppingLinkKey.KEY_SOURCE));
    }

    public static boolean hasEcommerceCoupons() {
        return !getEcommerceCoupons().isEmpty();
    }

    public static ArrayList<Coupon> getEcommerceCoupons() {
        ArrayList<Coupon> coupons = new ArrayList<>();

        final String rcString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_E_COMMERCE_COUPONS);
        try {
            final JSONArray jsonArray = new JSONArray(rcString);
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject object = (JSONObject) jsonArray.get(i);
                final ShoppingLink shoppingLink = toShoppingLink(object);
                final Coupon coupon = new Coupon(
                        object.optString(CouponKey.KEY_ID),
                        object.optString(CouponKey.KEY_CATEGORY),
                        object.optString(CouponKey.KEY_SUBCATEGORY),
                        object.optString(CouponKey.KEY_FEED),
                        object.optLong(CouponKey.KEY_START),
                        object.optLong(CouponKey.KEY_END),
                        object.optBoolean(CouponKey.KEY_ACTIVE),
                        shoppingLink
                );

                // Filter out the invalid coupons - NotStartYet, Expired and Inactive
                if (coupon.getStart() > System.currentTimeMillis()
                        || coupon.getEnd() < System.currentTimeMillis()
                        || !coupon.getActive()) {
                    continue;
                }

                coupons.add(coupon);
            }
        } catch (JSONException e) {
            // skip and do nothing
        }

        return coupons;
    }

    public static String getCouponBannerRootConfig() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_COUPON_BANNER_MANIFEST);
    }

    public static String getNewsProviderUrl(String provider) {
        String source = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_NEWS_PROVIDERS);
        String url = "";

        try {
            JSONArray rows = new JSONArray(source);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                if (row.getString("name").equalsIgnoreCase(provider)) {
                    url = row.getString("url");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return url;
    }

    static String getShareAppDialogTitle() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_SHARE_APP_DIALOG_TITLE);
    }

    // Only this field supports prettify
    static String getShareAppDialogContent() {
        final String rcString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_SHARE_APP_DIALOG_CONTENT);
        return FirebaseHelper.prettify(rcString);
    }

    static String getShareAppMessage() {
        return FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_SHARE_APP_DIALOG_MSG);
    }

    public static List<BottomBarItemAdapter.ItemData> getBottomBarItems() {
        List<BottomBarItemAdapter.ItemData> itemDataList = new ArrayList<>();
        String jsonString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_BOTTOM_BAR_ITEMS);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                int type = row.getInt("type");
                itemDataList.add(new BottomBarItemAdapter.ItemData(type));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return itemDataList;
    }

    public static List<BottomBarItemAdapter.ItemData> getMenuBottomBarItems() {
        List<BottomBarItemAdapter.ItemData> itemDataList = new ArrayList<>();
        String jsonString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_MENU_BOTTOM_BAR_ITEMS);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                int type = row.getInt("type");
                itemDataList.add(new BottomBarItemAdapter.ItemData(type));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return itemDataList;
    }

    public static List<BottomBarItemAdapter.ItemData> getPrivateBottomBarItems() {
        List<BottomBarItemAdapter.ItemData> itemDataList = new ArrayList<>();
        String jsonString = FirebaseHelper.getFirebase().getRcString(FirebaseHelper.STR_PRIVATE_BOTTOM_BAR_ITEMS);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                int type = row.getInt("type");
                itemDataList.add(new BottomBarItemAdapter.ItemData(type));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return itemDataList;
    }
}
