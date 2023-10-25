package com.ignacio.godrive.actividades.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitCliente {

    private static Retrofit retrofit = null;
    public static Retrofit obtenerCliente(String url){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
