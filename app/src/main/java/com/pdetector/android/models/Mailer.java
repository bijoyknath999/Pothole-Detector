package com.pdetector.android.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mailer {

    @SerializedName("sender")
    @Expose
    private Sender sender;
    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("htmlContent")
    @Expose
    private String htmlContent;
    @SerializedName("messageVersions")
    @Expose
    private List<MessageVersion> messageVersions;

    public Mailer(Sender sender, String subject, String htmlContent, List<MessageVersion> messageVersions) {
        this.sender = sender;
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.messageVersions = messageVersions;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public List<MessageVersion> getMessageVersions() {
        return messageVersions;
    }

    public void setMessageVersions(List<MessageVersion> messageVersions) {
        this.messageVersions = messageVersions;
    }

}