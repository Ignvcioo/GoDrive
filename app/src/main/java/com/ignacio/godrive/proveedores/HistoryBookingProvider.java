package com.ignacio.godrive.proveedores;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ignacio.godrive.modelo.ClientBooking;
import com.ignacio.godrive.modelo.HistoryBooking;

import java.util.HashMap;
import java.util.Map;

public class HistoryBookingProvider {
    private DatabaseReference mDatabase;
    public HistoryBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistoryBooking");
    }

    public Task<Void> create(HistoryBooking historyBooking) {
        return mDatabase.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    public Task<Void> updateCalifictionClient(String idHistoryBooking, float calificationClient) {
        Map<String, Object> map = new HashMap<>();
        map.put("calificationClient", calificationClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public Task<Void> updateCalifictionDriver(String idHistoryBooking, float calificationDriver) {
        Map<String, Object> map = new HashMap<>();
        map.put("calificationDriver", calificationDriver);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public DatabaseReference getHistoryBooking(String idHistoryBooking) {
        return mDatabase.child(idHistoryBooking);
    }
}
