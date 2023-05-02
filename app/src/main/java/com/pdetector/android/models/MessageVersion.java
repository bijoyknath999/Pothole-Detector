package com.pdetector.android.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageVersion {

    @SerializedName("to")
    @Expose
    private List<To> to;

    public List<To> getTo() {
        return to;
    }

    public void setTo(List<To> to) {
        this.to = to;
    }

    public MessageVersion(List<To> to) {
        this.to = to;
    }
}