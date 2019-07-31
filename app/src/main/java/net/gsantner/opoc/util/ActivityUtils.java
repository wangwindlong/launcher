/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: Apache 2.0 (Commercial upon request)
 *     https://www.apache.org/licenses/LICENSE-2.0
 *     https://github.com/gsantner/opoc/#licensing
 *
#########################################################*/
package net.gsantner.opoc.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ScrollView;


@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection"})
public class ActivityUtils extends net.gsantner.opoc.util.ContextUtils {
    //########################
    //## Members, Constructors
    //########################
    protected Activity _activity;

    public ActivityUtils(final Activity activity) {
        super(activity);
        _activity = activity;
    }

    @Override
    public void freeContextRef() {
        super.freeContextRef();
        _activity = null;
    }

    //########################
    //##     Methods
    //########################

    /**
     * Animate to specified Activity
     *
     * @param to                 The class of the activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public void animateToActivity(Class to, Boolean finishFromActivity, Integer requestCode) {
        animateToActivity(new Intent(_activity, to), finishFromActivity, requestCode);
    }

    /**
     * Animate to Activity specified in intent
     * Requires animation resources
     *
     * @param intent             Intent to open start an activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public void animateToActivity(Intent intent, Boolean finishFromActivity, Integer requestCode) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (requestCode != null) {
            _activity.startActivityForResult(intent, requestCode);
        } else {
            _activity.startActivity(intent);

        }
        _activity.overridePendingTransition(getResId(ResType.DIMEN, "fadein"), getResId(ResType.DIMEN, "fadeout"));
        if (finishFromActivity != null && finishFromActivity) {
            _activity.finish();
        }
    }


    public Snackbar showSnackBar(@StringRes int stringResId, boolean showLong) {
        Snackbar s = Snackbar.make(_activity.findViewById(android.R.id.content), stringResId,
                showLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT);
        s.show();
        return s;
    }

    public void showSnackBar(@StringRes int stringResId, boolean showLong, @StringRes int actionResId, View.OnClickListener listener) {
        Snackbar.make(_activity.findViewById(android.R.id.content), stringResId,
                showLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)
                .setAction(actionResId, listener)
                .show();
    }

    public ActivityUtils hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) _activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null && _activity.getCurrentFocus() != null && _activity.getCurrentFocus().getWindowToken() != null) {
            imm.hideSoftInputFromWindow(_activity.getCurrentFocus().getWindowToken(), 0);
        }
        return this;
    }

    public ActivityUtils showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) _activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null && _activity.getCurrentFocus() != null && _activity.getCurrentFocus().getWindowToken() != null) {
            imm.showSoftInput(_activity.getCurrentFocus(), InputMethodManager.SHOW_FORCED);
        }
        return this;
    }

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String html) {
        showDialogWithHtmlTextView(resTitleId, html, true, null);
    }

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String text, boolean isHtml, DialogInterface.OnDismissListener dismissedListener) {
        ScrollView scroll = new ScrollView(_context);
        AppCompatTextView textView = new AppCompatTextView(_context);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, _context.getResources().getDisplayMetrics());

        scroll.setPadding(padding, 0, padding, 0);
        scroll.addView(textView);
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setText(isHtml ? new SpannableString(Html.fromHtml(text)) : text);

        AlertDialog.Builder dialog = new AlertDialog.Builder(_context)
                .setPositiveButton(android.R.string.ok, null).setOnDismissListener(dismissedListener)
                .setTitle(resTitleId).setView(scroll);
        dialog.show();
    }

    public void showDialogWithRawFileInWebView(String fileInRaw, @StringRes int resTitleId) {
        WebView wv = new WebView(_context);
        wv.loadUrl("file:///android_res/raw/" + fileInRaw);
        AlertDialog.Builder dialog = new AlertDialog.Builder(_context)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(resTitleId)
                .setView(wv);
        dialog.show();
    }

    // Toggle with no param, else set visibility according to first bool
    public ActivityUtils toggleStatusbarVisibility(boolean... optionalForceVisible) {
        WindowManager.LayoutParams attrs = _activity.getWindow().getAttributes();
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (optionalForceVisible.length == 0) {
            attrs.flags ^= flag;
        } else if (optionalForceVisible.length == 1 && optionalForceVisible[0]) {
            attrs.flags &= ~flag;
        } else {
            attrs.flags |= flag;
        }
        _activity.getWindow().setAttributes(attrs);
        return this;
    }

    public ActivityUtils showGooglePlayEntryForThisApp() {
        String pkgId = "details?id=" + _activity.getPackageName();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + pkgId));
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                (Build.VERSION.SDK_INT >= 21 ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            _activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            _activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/" + pkgId)));
        }
        return this;
    }

    public ActivityUtils setStatusbarColor(int color, boolean... fromRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (fromRes != null && fromRes.length > 0 && fromRes[0]) {
                color = ContextCompat.getColor(_context, color);
            }

            _activity.getWindow().setStatusBarColor(color);
        }
        return this;
    }

    public ActivityUtils setLauncherActivityEnabled(Class activityClass, boolean enable) {
        Context context = _context.getApplicationContext();
        PackageManager pkg = context.getPackageManager();
        ComponentName component = new ComponentName(context, activityClass);
        pkg.setComponentEnabledSetting(component, enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        return this;
    }


    @ColorInt
    public Integer getCurrentPrimaryColor() {
        TypedValue typedValue = new TypedValue();
        _context.getTheme().resolveAttribute(getResId(ResType.ATTR, "colorPrimary"), typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public Integer getCurrentPrimaryDarkColor() {
        TypedValue typedValue = new TypedValue();
        _context.getTheme().resolveAttribute(getResId(ResType.ATTR, "colorPrimaryDark"), typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public Integer getCurrentAccentColor() {
        TypedValue typedValue = new TypedValue();
        _context.getTheme().resolveAttribute(getResId(ResType.ATTR, "colorAccent"), typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public Integer getActivityBackgroundColor() {
        TypedArray array = _activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
        });
        int c = array.getColor(0, 0xFF0000);
        array.recycle();
        return c;
    }

    public ActivityUtils startCalendarApp() {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        _activity.startActivity(intent);
        return this;
    }
}
