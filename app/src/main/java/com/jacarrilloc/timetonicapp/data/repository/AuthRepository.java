package com.jacarrilloc.timetonicapp.data.repository;

public interface AuthRepository {
    void login(String email, String password, LoginCallback callback);

    interface LoginCallback {
        void onSuccess(String result);
        void onFailure(String errorMessage);
    }
}
