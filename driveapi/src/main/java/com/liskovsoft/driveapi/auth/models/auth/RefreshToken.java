package com.liskovsoft.driveapi.auth.models.auth;

import com.liskovsoft.driveapi.common.converters.jsonpath.JsonPath;

public class RefreshToken extends AccessToken {
    @JsonPath("$.refresh_token")
    private String mRefreshToken;

    public String getRefreshToken() {
        return mRefreshToken;
    }
}