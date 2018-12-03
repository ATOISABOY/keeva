package com.snyper.keeva.model;

/**
 * Created by stephen snyper on 9/28/2018.
 */

public class Token {

    public String token;
    public boolean isServerToken;

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
