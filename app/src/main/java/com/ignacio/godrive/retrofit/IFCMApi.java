package com.ignacio.godrive.retrofit;

import com.ignacio.godrive.modelo.FCMBody;
import com.ignacio.godrive.modelo.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAVwO-VwY:APA91bE7XJ43N4kmqBJToak4qOsr9EnJM-aWJjBQe_4UbhToX9LgYIgwBqvWw-M4VtYY0j2twpU4lOzD_nGa1BJrpn3SqtfarmPudZsBNey9w4s8w__VjZDdx-QL-V8RXNteruzce9w1"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody contenido);

}
