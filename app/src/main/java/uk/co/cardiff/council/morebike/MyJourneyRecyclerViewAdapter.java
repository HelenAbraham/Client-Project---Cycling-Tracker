package uk.co.cardiff.council.morebike;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import uk.co.cardiff.council.morebike.JourneyFragment.OnListFragmentInteractionListener;
import uk.co.cardiff.council.morebike.localdb.tables.journey.Journey;
import uk.co.cardiff.council.morebike.utility.ImageHandler;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Journey} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyJourneyRecyclerViewAdapter extends RecyclerView.Adapter<MyJourneyRecyclerViewAdapter.ViewHolder> {

    private final List<Journey> mViews;
    private final OnListFragmentInteractionListener mListener;

    public MyJourneyRecyclerViewAdapter(List<Journey> items, OnListFragmentInteractionListener listener) {
        mViews = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_journey, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mViews.get(position);
        mViews.get(position).setStartDate(mViews.get(position).getStartTime());
        holder.mTitleView.setText(mViews.get(position).getStartAddress());
        holder.mEmissionsSavedView.setText(String.format("Emissions saved: %sg of CO2", mViews.get(position).calculateEmissionsSaved()));
        holder.mContentView.setText(String.format("%s - %s", mViews.get(position).getStartDate(), mViews.get(position).getEndDate()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mViews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mEmissionsSavedView;
        public final TextView mContentView;
        public final AppCompatImageView mapCardImage;
        public Journey mItem;

        private final int originalHeight;
        private boolean toggled = false;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.journey_address);
            mEmissionsSavedView = view.findViewById(R.id.emissions_saved);
            mContentView = view.findViewById(R.id.journey_date);
            mapCardImage = view.findViewById(R.id.map_image_card);

            View arrowExpand = view.findViewById(R.id.card_expand_arrow);
            originalHeight = mView.getLayoutParams().height;

            arrowExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim;

                    if (!toggled) {
                        anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.rotate_to_180);
                        if(mItem != null) {
                            ViewGroup.LayoutParams params = mView.getLayoutParams();
                            params.height = 1000;
                            mView.setLayoutParams(params);

                            mapCardImage.setVisibility(View.VISIBLE);
                            mapCardImage.setImageDrawable(
                                    ImageHandler.getImageFromInternalStorage(mView.getContext(), mItem.getId() + ".png"));

                        }
                    }
                    else {
                        if(mItem != null) {
                            mapCardImage.setVisibility(View.GONE);
                            mapCardImage.setImageDrawable(null);

                            ViewGroup.LayoutParams params = mView.getLayoutParams();
                            params.height = originalHeight;
                            mView.setLayoutParams(params);
                        }
                        anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.rotate_from_180_to_0);

                    }
                    v.startAnimation(anim);



                    toggle();
                }
            });
        }

        private void toggle() {
            toggled = !toggled;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
