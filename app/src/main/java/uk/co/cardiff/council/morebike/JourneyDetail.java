package uk.co.cardiff.council.morebike;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import uk.co.cardiff.council.morebike.localdb.tables.journey.Journey;
import uk.co.cardiff.council.morebike.utility.ImageHandler;
import uk.co.cardiff.council.morebike.utility.onSwipeTouchListener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class JourneyDetail extends Fragment {

    private static final String JOURNEY_ID_ARG = "journey-id";

    private JourneyDetailViewModel mViewModel;

    public static JourneyDetail newInstance(int journeyId) {
        JourneyDetail fragment = new JourneyDetail();
        Bundle args = new Bundle();

        args.putInt(JOURNEY_ID_ARG, journeyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journey_detail_fragment, container, false);
        v.setOnTouchListener(new onSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeBottom() {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_through_bottom).remove(JourneyDetail.this).commit();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(JourneyDetailViewModel.class);
        if (getArguments() != null) {
            Bundle args = getArguments();
            final int journeyId = args.getInt(JOURNEY_ID_ARG);
            mViewModel.getAllJourneys().observe(this, new Observer<List<Journey>>() {
                @Override
                public void onChanged(List<Journey> journeys) {
                    for (Journey journey : journeys) {
                        if (journey.getId() == journeyId) {
                            ((AppCompatTextView) getActivity().findViewById(R.id.location_placeholder)).setText(
                                    String.format("%s - %s", journey.getStartAddress(), journey.getEndAddress())
                            );

                            journey.setStartDate(journey.getStartTime());

                            ((AppCompatTextView) getActivity().findViewById(R.id.time_placeholder)).setText(
                                    String.format("%s - %s", journey.getStartDate(), journey.getEndDate())
                            );

                            ((AppCompatTextView) getActivity().findViewById(R.id.emission_placeholder)).setText(
                                    String.format("%.2f g", journey.calculateEmissionsSaved())
                            );

                            ((AppCompatTextView) getActivity().findViewById(R.id.distance_placeholder)).setText(
                                    String.format("%.2f km", journey.getKilometersTravelled())
                            );

                            ((AppCompatImageView) getActivity().findViewById(R.id.map_image_view))
                                    .setImageDrawable(ImageHandler.getImageFromInternalStorage(getActivity(), journey.getId() + ".png"));
                        }
                    }
                }
            });
        }
    }

}
