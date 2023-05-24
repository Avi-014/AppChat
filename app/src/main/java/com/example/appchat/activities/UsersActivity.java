package com.example.appchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.appchat.adapter.UserAdapter;
import com.example.appchat.databinding.ActivityUsersBinding;
import com.example.appchat.listeners.UserListener;
import com.example.appchat.models.RecyclerView_ItemDecoration;
import com.example.appchat.models.UserModel;
import com.example.appchat.utilities.Constant;
import com.example.appchat.utilities.PreferenceManger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManger preferenceManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        setListener();
        getUsers();
        preferenceManger = new PreferenceManger(getApplicationContext());

    }
    private void setListener(){
        binding.imageback.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task-> {
                    loading(false);
                    String currentUserId = preferenceManger.getString(Constant.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<UserModel> userModels = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            UserModel userModel = new UserModel();
                            userModel.username = queryDocumentSnapshot.getString(Constant.KEY_USERNAME);
                            userModel.email = queryDocumentSnapshot.getString(Constant.KEY_EMAIL);
                            userModel.image = queryDocumentSnapshot.getString(Constant.KEY_IMAGE);
                            userModel.token = queryDocumentSnapshot.getString(Constant.KEY_FCM_TOKEN);
                            userModel.id = queryDocumentSnapshot.getId();
                            userModels.add(userModel);
                        }
                        if (userModels.size()>0){
                            UserAdapter userAdapter = new UserAdapter(userModels ,this);
                            binding.UserChatRecyclerView.setAdapter(userAdapter);
                            binding.UserChatRecyclerView.addItemDecoration(new RecyclerView_ItemDecoration(25));
                            binding.UserChatRecyclerView.smoothScrollToPosition(userModels.size() - 1);
                            binding.UserChatRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.TextErrorMessage.setText(String.format("%s","Users Not Available"));
        binding.TextErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.ChatProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.ChatProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER,userModel);
        startActivity(intent);
    }
}