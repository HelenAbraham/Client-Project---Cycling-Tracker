package uk.co.cardiff.council.morebike.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import uk.co.cardiff.council.morebike.R;

public class LocationInfoDialog extends DialogFragment {

    public LocationInfoDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This app requires your location in order to follow your journey and show" +
                " the emissions you have saved. Please go into the apps settings and enable location.")
                .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });
        return builder.create();
    }
}
