package uk.co.cardiff.council.morebike.localdb.tables.journey;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import uk.co.cardiff.council.morebike.utility.DateTypeConverter;

@Database(entities = {Journey.class}, version = 6, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class JourneyDatabase extends RoomDatabase {
    private static JourneyDatabase INSTANCE;
    public static final String DB_NAME = "Journeys";

    public static JourneyDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), JourneyDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public abstract JourneyDao journeyDao();
}
