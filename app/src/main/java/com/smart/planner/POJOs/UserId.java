package com.smart.planner.POJOs;

import androidx.annotation.NonNull;

public class UserId {
    public String userKey ;

    public <T extends UserId> T withId(@NonNull final String id){
        this.userKey = id ;
        return (T)this;
    }
}
