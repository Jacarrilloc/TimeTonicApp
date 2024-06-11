package com.jacarrilloc.timetonicapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.jacarrilloc.timetonicapp.data.repository.AuthRepository;
import com.jacarrilloc.timetonicapp.data.repository.AuthRepositoryImpl;
import com.jacarrilloc.timetonicapp.util.NetworkUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_login);
        passwordInput = findViewById(R.id.password_login);
        loginButton = findViewById(R.id.login_button);

        authRepository = new AuthRepositoryImpl(this);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = emailInput.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (NetworkUtil.isNetworkConnected(this)) {
                    authRepository.login(email, password, new AuthRepository.LoginCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Login OK", Toast.LENGTH_SHORT).show();
                                // Intent intent = new Intent(LoginActivity.this, LandingPageActivity.class);
                                // startActivity(intent);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Please, enter Email and password to continue", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
