package com.example.appchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appchat.databinding.ActivitySignInBinding;
import com.example.appchat.utilities.Constant;
import com.example.appchat.utilities.PreferenceManger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManger preferenceManger;
    String EmailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        preferenceManger = new PreferenceManger(getApplicationContext());
        if (preferenceManger.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners() {
        binding.SignUpButton.setOnClickListener(v ->
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.SignInButton.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    // Login Using Email and Password
    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, binding.InputEmail.getText().toString())
                .whereEqualTo(Constant.KEY_PASSWORD, binding.InputPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManger.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                        preferenceManger.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManger.putString(Constant.KEY_USERNAME, documentSnapshot.getString(Constant.KEY_USERNAME));
                        preferenceManger.putString(Constant.KEY_IMAGE, documentSnapshot.getString(Constant.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to Sign In");
                    }
                });
    }


//      Toast Function
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

//     Checking Data For Login
    private boolean isValidSignInDetails() {
        if (binding.InputEmail.getText().toString().trim().isEmpty()) {
            binding.InputEmail.setError("");
            showToast("Email can't be Null");
            return false;
        } else if (!binding.InputEmail.getText().toString().matches(EmailPattern)) {
            binding.InputEmail.setError("");
            showToast("Enter a Valid Email");
            return false;
        } else if (binding.InputPassword.getText().toString().trim().isEmpty()) {
            binding.InputPassword.setError("");
            showToast("Enter Password");
            return false;
       } else {
            return true;
        }
    }

//      progress bar
    private void loading(Boolean isloading) {
        if (isloading) {
            binding.SignInButton.setVisibility(View.INVISIBLE);
            binding.ProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.SignInButton.setVisibility(View.VISIBLE);
            binding.ProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}