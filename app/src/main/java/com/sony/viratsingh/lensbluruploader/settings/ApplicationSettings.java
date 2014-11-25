package com.sony.viratsingh.lensbluruploader.settings;

import android.content.SharedPreferences;

import com.sony.viratsingh.lensbluruploader.MainActivity;

public class ApplicationSettings {
  public static final String SETTINGS_ENDPOINT = "endpoint";

  private final SharedPreferences sharedPreferences;
  private final MainActivity activity;

  public ApplicationSettings(SharedPreferences sharedPreferences, MainActivity activity) {
    this.sharedPreferences = sharedPreferences;
    this.activity = activity;
  }

  public void setEndpoint(String endpoint) {
    setStringPreference(SETTINGS_ENDPOINT, endpoint);
  }

  public String getEndpoint() {
    return sharedPreferences.getString(SETTINGS_ENDPOINT, activity.getEndpoint());
  }


  /**
   * Save a String Preference in the SharedPreferences
   * @param key
   * @param setting
   */
  private void setStringPreference(final String key, final String setting) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(key, setting);
    editor.commit();
  }

}
