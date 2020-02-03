package uk.co.cardiff.council.morebike.localdb.tables.journey;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JourneyDao {

    @Query("SELECT * FROM journey")
    List<Journey> getAll();

    @Query("SELECT * FROM journey WHERE is_sent = 0")
    List<Journey> getAllUnsent();

    @Query("SELECT * FROM journey")
    LiveData<List<Journey>> getAllAsync();

    @Query("SELECT * FROM journey ORDER BY ID DESC LIMIT 1")
    Journey getLastEntry();

    @Query("SELECT * FROM journey WHERE ID = :id LIMIT 1")
    Journey getJourneyById(int id);

    @Query("UPDATE journey SET is_sent = 1 WHERE ID = :id")
    void setSentFor(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> createAll(Journey... journeys);

    @Insert
    List<Long> createAll(List<Journey> journeys);

    @Delete
    void delete(Journey journey);

    @Query("DELETE FROM journey WHERE ID = :id")
    void deleteById(int id);

    @Query("DELETE FROM journey")
    void deleteAll();
}
