package com.benny.openlauncher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HideAppsActivity;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.activity.MoreInfoActivity;
import com.benny.openlauncher.activity.SettingsActivity;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.widget.AppDrawerController;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.util.PermissionChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsMasterFragment extends GsPreferenceFragmentCompat<AppSettings> {
    public static final String TAG = "com.benny.openlauncher.fragment.SettingsMasterFragment";
    protected AppSettings _as;
    private int activityRetVal;
    private Integer iconColor;

    @Override
    public int getPreferenceResourceForInflation() {
        return R.xml.preferences_master;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected AppSettings getAppSettings(Context context) {
        if (_as == null) {
            _as = new AppSettings(context);
        }
        return _as;
    }

    @Override
    protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
        if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
            SettingsActivity a = (SettingsActivity) getActivity();
            if (a != null) {
                a.toolbar.setTitle(preferenceScreen.getTitle());
            }
        }
    }

    private static final List<Integer> noRestart = new ArrayList<>(Arrays.asList(
            R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up,
            R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch,
            R.string.pref_key__gesture_unpinch));

    @Override
    public void doUpdatePreferences() {
        updateSummary(R.string.pref_key__cat_desktop, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDesktopColumnCount(), _as.getDesktopRowCount()));
        updateSummary(R.string.pref_key__cat_dock, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDockColumnCount(), _as.getDockRowCount()));
        updateSummary(R.string.pref_key__cat_appearance, String.format(Locale.ENGLISH, "Icons: %ddp", _as.getIconSize()));

        switch (_as.getDrawerStyle()) {
            case AppDrawerController.Mode.GRID:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.vertical_scroll_drawer)));
                break;
            case AppDrawerController.Mode.PAGE:
            default:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)));
                break;
        }

        for (int resId : new ArrayList<>(Arrays.asList(R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up, R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch, R.string.pref_key__gesture_unpinch))) {
            Preference preference = findPreference(getString(resId));
            Object gesture = AppSettings.get().getGesture(resId);
            if (gesture instanceof Intent) {
                updateSummary(resId, String.format(Locale.ENGLISH, "%s: %s", getString(R.string.app), AppManager.getInstance(getContext()).findApp((Intent) gesture)._label));
            } else if (gesture instanceof LauncherAction.ActionDisplayItem) {
                updateSummary(resId, String.format(Locale.ENGLISH, "%s: %s", getString(R.string.action), ((LauncherAction.ActionDisplayItem) gesture)._label));
            } else {
                updateSummary(resId, String.format(Locale.ENGLISH, "%s", getString(R.string.none)));
            }
        }
    }

    @Override
    protected void onPreferenceChanged(SharedPreferences prefs, String key) {
        super.onPreferenceChanged(prefs, key);
        activityRetVal = 1;
        if (!noRestart.contains(keyToStringResId(key))) {
            AppSettings.get().setAppRestartRequired(true);
        }
    }


    @Override
    @SuppressWarnings({"ConstantIfStatement"})
    public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
        HomeActivity homeActivity = HomeActivity._launcher;
        switch (keyResId) {
            case R.string.pref_key__about: {
                startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                return true;
            }
            case R.string.pref_key__backup: {
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_BACKUP);
                }
                return true;
            }
            case R.string.pref_key__restore: {
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                    getActivity().startActivityForResult(i, Definitions.INTENT_RESTORE);
                }
                return true;
            }
            case R.string.pref_key__reset_settings: {
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_settings), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            PackageInfo p = getActivity().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                            String dataDir = p.applicationInfo.dataDir;
                            new File(dataDir + "/shared_prefs/app.xml").delete();
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
            case R.string.pref_key__reset_database: {
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_database), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DatabaseHelper db = HomeActivity._db;
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        AppSettings.get().setAppFirstLaunch(true);
                        System.exit(0);
                    }
                });
                return true;
            }
            case R.string.pref_key__restart: {
                homeActivity.recreate();
                getActivity().finish();
                return true;
            }
            case R.string.pref_key__hidden_apps: {
                Intent intent = new Intent(getActivity(), HideAppsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            }
            case R.string.pref_key__minibar: {
                LauncherAction.RunAction(LauncherAction.Action.EditMinibar, getActivity());
                return true;
            }
            case R.string.pref_key__icon_pack: {
                DialogHelper.startPickIconPackIntent(getActivity());
                return true;
            }

            case R.string.pref_key__gesture_double_tap:
            case R.string.pref_key__gesture_swipe_up:
            case R.string.pref_key__gesture_swipe_down:
            case R.string.pref_key__gesture_pinch:
            case R.string.pref_key__gesture_unpinch: {
                DialogHelper.selectGestureDialog(getActivity(), preference.getTitle().toString(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 1) {
                            DialogHelper.selectActionDialog(getActivity(), new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    AppSettings.get().setString(key, LauncherAction.getActionItem(position)._action.toString());
                                }
                            });
                        } else if (position == 2) {
                            DialogHelper.selectAppDialog(getActivity(), new DialogHelper.OnAppSelectedListener() {
                                @Override
                                public void onAppSelected(App app) {
                                    AppSettings.get().setString(key, Tool.getIntentAsString(Tool.getIntentFromApp(app)));
                                }
                            });
                        } else {
                            AppSettings.get().setString(key, "");
                        }
                    }
                });
                return true;
            }
        }
        return null;
    }

    @Override
    public boolean isDividerVisible() {
        return true;

    }
}