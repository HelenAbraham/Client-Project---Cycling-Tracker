package uk.co.cardiff.council.morebike;

public interface GpsStatusListener {
    void onStart();
    void onStop();
    void onFixed();
    void onUnFixed();
    void onSignalStrength(int inUse, int count);
}
