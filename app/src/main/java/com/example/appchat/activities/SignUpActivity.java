package com.example.appchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchat.databinding.ActivitySignUpBinding;
import com.example.appchat.utilities.Constant;
import com.example.appchat.utilities.PreferenceManger;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManger preferenceManger;
    String EmailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        preferenceManger = new PreferenceManger(SignUpActivity.this);
        setListeners();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setListeners(){
        binding.BackToLoginButton.setOnClickListener(v -> onBackPressed());
        binding.CreateAccSigUpButton.setOnClickListener(v -> {
            if (isValidSignUpDetails()){
                signUp();
            }
        });

//      Profile Image Selection while Creating new account // Intent from layout to gallery
        binding.LayoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    // show Toast message
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


//  Details saved in firebase while creating account
    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constant.KEY_USERNAME,binding.CreateAccUserName.getText().toString());
        user.put(Constant.KEY_EMAIL,binding.CreateAccNewEmail.getText().toString());
        user.put(Constant.KEY_PASSWORD, Objects.requireNonNull(binding.CreateAccNewPassword.getText()).toString());
        user.put(Constant.KEY_IMAGE,encodedImage);
        database.collection(Constant.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManger.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                    preferenceManger.putString(Constant.KEY_USER_ID,documentReference.getId());
                    preferenceManger.putString(Constant.KEY_USERNAME,binding.CreateAccUserName.getText().toString());
                    preferenceManger.putString(Constant.KEY_IMAGE,encodedImage);
                    startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                    finish();
                }).addOnFailureListener(exception-> {
                    loading(false);
                    showToast(exception.getMessage());
                 });
    }

//    pick Image
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap , previewWidth , previewHeight , false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //set pictures
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result->{
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.CreateAccProfileImage.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
    }
    );


//    Checking details filled up correctly while creating new account
    private boolean isValidSignUpDetails() {
        if (binding.CreateAccUserName.getText().toString().trim().isEmpty()) {
            showToast("UserName Can't be Null");
            binding.CreateAccUserName.setError("");
            return false;
        } else if (binding.CreateAccNewEmail.getText().toString().trim().isEmpty()) {
            binding.CreateAccNewEmail.setError("");
            showToast("Email can't be Null");
            return false;
        } else if(!binding.CreateAccNewEmail.getText().toString().matches(EmailPattern)){
            binding.CreateAccNewEmail.setError("");
            showToast("Enter a Valid Email");
            return false;
        }else if (Objects.requireNonNull(binding.CreateAccNewPassword.getText()).toString().trim().isEmpty()) {
            binding.CreateAccNewPassword.setError("");
            showToast("Enter Password");
            return false;
        } else if (!binding.CreateAccNewPassword.getText().toString().trim()
                .equals(binding.CreateAccConfirmPassword.getText().toString().trim())) {
            binding.CreateAccConfirmPassword.setError("");
            showToast("New Password and Confirm Password must be same");
            return false;
        } else if(encodedImage == null){
            showToast("upload an Image");
            return false;
        }else {
            return true;
        }
    }

//    progress bar loading after clicking sign up button
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.CreateAccSigUpButton.setVisibility(View.INVISIBLE);
            binding.CreateAccProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.CreateAccSigUpButton.setVisibility(View.VISIBLE);
            binding.CreateAccProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}