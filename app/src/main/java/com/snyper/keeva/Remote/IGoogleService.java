package com.snyper.keeva.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by stephen snyper on 10/13/2018.
 */

public interface IGoogleService {
    @GET
    Call<String> getAddressName(@Url String url);
}
