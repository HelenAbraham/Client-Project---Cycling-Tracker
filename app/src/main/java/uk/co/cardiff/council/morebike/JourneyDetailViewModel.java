package uk.co.cardiff.council.morebike;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import uk.co.cardiff.council.morebike.localdb.tables.journey.Journey;
import uk.co.cardiff.council.morebike.localdb.tables.journey.JourneyDatabase;

public class JourneyDetailViewModel extends AndroidViewModel {

    private LiveData<List<Journey>> journeys;
    private MutableLiveData<Journey> selected = new MutableLiveData<>();
    private JourneyDatabase db;


    public JourneyDetailViewModel(Application application) {
        super(application);
        db = JourneyDatabase.getDatabase(this.getApplication());

        journeys = db.journeyDao().getAllAsync();
    }

    public LiveData<List<Journey>> getAllJourneys() {
        return journeys;
    }

    public LiveData<Journey> getSelected() {
        return selected;
    }

    public void select(Journey journey) {
        selected.setValue(journey);
    }
}
