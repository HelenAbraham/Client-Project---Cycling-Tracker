package uk.co.cardiff.council.morebike;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
//Code below adapted from https://developer.android.com/guide/topics/ui/dialogs [Accessed 25/03/2019]
public class EndOfRideInfoDialog extends DialogFragment {

    private static final String DISTANCE = "distance";
    private static final String EMISSIONS = "emissions";

    // TODO: Rename and change types of parameters
    private double mD;
    private double mEs;

    public EndOfRideInfoDialog() {
    }

    public static EndOfRideInfoDialog newInstance(double distanceTravelled, double emissionsSaved){
        EndOfRideInfoDialog endOfRideDialog = new EndOfRideInfoDialog();
        Bundle args = new Bundle();
        args.putDouble(DISTANCE, distanceTravelled);
        args.putDouble(EMISSIONS, emissionsSaved);
        endOfRideDialog.setArguments(args);
        return endOfRideDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            mD = getArguments().getDouble(DISTANCE);
            mEs = getArguments().getDouble(EMISSIONS);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.you_have_travelled) + mD + getString(R.string.km_and_saved) + mEs + getString(R.string.g_of_CO2_emissions))
                .setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
}
