package uk.co.cardiff.council.morebike;

/* code adapted from :
https://www.javatips.net/api/android.location.gpsstatus.listener [accessed on 28/03/2019]
https://stackoverflow.com/questions/32860101/how-to-implement-gps-status-change-listener [accessed on 28/03/2019]
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.core.app.ActivityCompat;

public class GpsStatusProxy {
    private static volatile GpsStatusProxy proxy;
    private Context context;
    private LocationManager locationManager;
    private List<WeakReference<GpsStatusListener>> listenerList;
    private List<Satellite> satelliteList;
    private boolean isGpsLocated = false;

    public GpsStatusProxy(Context context) {
        this.context = context;
    }

    public static GpsStatusProxy getInstance(Context context) {
        if (proxy == null) {
            synchronized (GpsStatusProxy.class) {
                if (proxy == null) {
                    proxy = new GpsStatusProxy(context);
                }
            }
        }
        return proxy;
    }

    /**
     * Register GPS Status Listener, must check for permission of ACCESS_FINE_LOCATION first!
     */
    public void register() {
        unRegister();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(listener);
    }

    /**
     * Release GPS Status Listener
     */
    public synchronized void unRegister() {
        if (locationManager == null)
            return;
        locationManager.removeGpsStatusListener(listener);
        locationManager = null;
    }

    /**
     * Call this after location has changed to refresh status
     * @param location new location
     */
    public void notifyLocation(Location location) {
        isGpsLocated = location.getProvider().equals(LocationManager.GPS_PROVIDER);

        refreshStatus();
    }

    /**
     * @return List of {@link Satellite} found
     */
    public List<Satellite> getSatelliteList(){
        return satelliteList;
    }


    void refreshStatus(){
        if (isGpsLocated){
            for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                if (listenerWeakReference.get() != null) {
                    listenerWeakReference.get().onFixed();
                }
            }
        }else {
            if (checkOpenGps(context)){
                for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                    if (listenerWeakReference.get() != null) {
                        listenerWeakReference.get().onUnFixed();
                    }
                }
            }else {
                for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                    if (listenerWeakReference.get() != null) {
                        listenerWeakReference.get().onStop();
                    }
                }
            }
        }
    }

    void addListener(GpsStatusListener listener) {
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        } else {
            Iterator<WeakReference<GpsStatusListener>> iterator = listenerList.iterator();
            while (iterator.hasNext()) {
                WeakReference<GpsStatusListener> listenerWeakReference = iterator.next();
                if (listenerWeakReference.get() == null) {
                    iterator.remove();
                } else if (listenerWeakReference.get() == listener) {
                    return;
                }
            }
        }

        listenerList.add(new WeakReference<GpsStatusListener>(listener));
    }

    void removeListener(GpsStatusListener listener) {
        if (listenerList == null)
            return;
        Iterator<WeakReference<GpsStatusListener>> iterator = listenerList.iterator();
        while (iterator.hasNext()) {
            WeakReference<GpsStatusListener> listenerWeakReference = iterator.next();
            if (listenerWeakReference.get() == null || listenerWeakReference.get() == listener) {
                iterator.remove();
            }
        }
    }

    private GpsStatus.Listener listener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (listenerList == null || listenerList.size() == 0)
                return;

            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                        if (listenerWeakReference.get() != null) {
                            listenerWeakReference.get().onFixed();
                        }
                    }
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    GpsStatus gpsStatus = null;
                    synchronized (this){
                        if (locationManager != null){
                            gpsStatus = locationManager.getGpsStatus(null);
                        }
                    }
                    if (gpsStatus != null) {
//                        GpsStatus gpsStatus=mAMapLocationManager.getGpsStatus(null);
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                        int count = 0;
                        int inUse = 0;
                        satelliteList = new ArrayList<>();
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                            if (s.usedInFix()) {
                                inUse++;
                            }
                            if (s.getSnr() > 0) {
                                satelliteList.add(new Satellite(s));
                            }
                        }
                        Collections.sort(satelliteList);

                        for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                            if (listenerWeakReference.get() != null) {
                                listenerWeakReference.get().onSignalStrength(inUse, count);
                            }
                        }
                    }

                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                        if (listenerWeakReference.get() != null) {
                            listenerWeakReference.get().onStart();
                        }
                    }
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    for (WeakReference<GpsStatusListener> listenerWeakReference : listenerList) {
                        if (listenerWeakReference.get() != null) {
                            listenerWeakReference.get().onStop();
                        }
                    }
                    break;
            }


        }
    };

    private boolean checkOpenGps(final Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }
}
