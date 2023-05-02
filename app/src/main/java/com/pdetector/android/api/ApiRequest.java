package com.pdetector.android.api;

import com.pdetector.android.models.Mailer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiRequest {
    @Headers({"accept: application/json","api-key: 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","content-type: application/json"})
    @POST("smtp/email")
    Call<Mailer> sendMail(@Body Mailer mailer);
}
