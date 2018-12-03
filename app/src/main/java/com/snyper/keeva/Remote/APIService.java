package com.snyper.keeva.Remote;

import com.snyper.keeva.model.DataMessage;
import com.snyper.keeva.model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by stephen snyper on 9/28/2018.
 */

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAndNNJHg:APA91bHxUXg57kMQzVKCLP0Lv3uEo-IPu_DbjCWEKIuvbnxiGe_Q-LDRMFWOvOJcJ3uFkPqu40SWxhNvpsIesFRKLZ1krVhWjBn3I9URqRp8O6qDrpmacCiW-XRMFWfEK72ovcvAQlM6"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
