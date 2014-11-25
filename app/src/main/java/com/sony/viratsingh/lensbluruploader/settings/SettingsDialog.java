package com.sony.viratsingh.lensbluruploader.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.sony.viratsingh.lensbluruploader.MainActivity;
import com.sony.viratsingh.lensbluruploader.R;

public class SettingsDialog extends DialogFragment {
  ///////////////////////////////////////////////////////////////////
  // UI
  ///////////////////////////////////////////////////////////////////
  private EditText endpointEditText;
  ///////////////////////////////////////////////////////////////////
  // FIELDS
  ///////////////////////////////////////////////////////////////////
  private ApplicationSettings applicationSettings;
  private MainActivity activity;

  private String endpoint;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = (MainActivity) getActivity();
    applicationSettings = new ApplicationSettings(PreferenceManager.getDefaultSharedPreferences(getActivity()), activity);



    // Get the Settings values
    endpoint = applicationSettings.getEndpoint();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();

    View view = inflater.inflate(R.layout.dialog_settings, null);

    endpointEditText = (EditText)view.findViewById(R.id.settings_endpoint_editText);
    endpointEditText.setHint(getResources().getString(R.string.endpoint));

    alert.setView(view);

    alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        if (!endpointEditText.getText().toString().isEmpty()) {
          endpoint = endpointEditText.getText().toString();
          applicationSettings.setEndpoint(endpoint);
          activity.setEndpoint(endpoint);
        }

      }
    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        // Intentionally blank; close dialog
      }
    });

    return alert.create();
  }

}
