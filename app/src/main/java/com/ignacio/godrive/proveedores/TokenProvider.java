package com.ignacio.godrive.proveedores;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ignacio.godrive.modelo.Token;

public class TokenProvider {
    DatabaseReference mDataBase;

    public TokenProvider() {
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Token token = new Token(s);
                mDataBase.child(idUser).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUser) {
        return mDataBase.child(idUser);
    }
}
