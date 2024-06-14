package com.jacarrilloc.timetonicapp.data.repository;

import android.content.Context;

public class BookRepository {
    private String sessioToken;

    public BookRepository(Context context) {
        this.sessioToken = AuthRepositoryImpl.getAuthToken(context);
    }
}
