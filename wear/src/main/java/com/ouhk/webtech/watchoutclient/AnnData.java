package com.ouhk.webtech.watchoutclient;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.Serializable;

/**
 * Created by Jacky Li on 20/4/2015.
 */
class AnnData implements Serializable {
    private String aData;
    private boolean wearConnectionState;
    private boolean sentaDataJSON;
    private transient Location LastLocation;
    private transient Context annUtilityContext;
    private boolean callEmergency;

    public boolean getCallEmergency() {
        return callEmergency;
    }

    public void setCallEmergency(boolean callEmergency) {
        this.callEmergency = callEmergency;
    }

    public Context getAnnUtilityContext() {
        return annUtilityContext;
    }

    public void setAnnUtilityContext(Context annUtilityContext) {
        this.annUtilityContext = annUtilityContext;
    }

    public Location getLastLocation() {
        return LastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        LastLocation = lastLocation;
    }

    public void setGMS(GoogleApiClient GMS) {
    }

    public boolean getSentaDataJSON() {
        return sentaDataJSON;
    }

    public void setSentaDataJSON(boolean sentaDataJSON) {
        this.sentaDataJSON = sentaDataJSON;
    }

    public void setFallDataState(String fallDataState) {
    }

    public boolean getWearConnectionState() {
        return wearConnectionState;
    }

    public void setWearConnectionState() {
        this.wearConnectionState = true;
    }

    public String getaData() {
        return aData;
    }

    public void setaData(String aData) {
        this.aData = aData;
    }


}
