package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.UserItemContainerBinding;
import com.example.appchat.listeners.UserListener;
import com.example.appchat.models.UserModel;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewholder> {

    private final List<UserModel> userModels;
    private final UserListener userListener;

    public UserAdapter(List<UserModel> userModels, UserListener userListener) {
        this.userModels = userModels;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemContainerBinding userItemContainerBinding = UserItemContainerBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewholder(userItemContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewholder holder, int position) {
        holder.setUserData(userModels.get(position));
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    class UserViewholder extends RecyclerView.ViewHolder {

        UserItemContainerBinding binding;

        UserViewholder(UserItemContainerBinding userItemContainerBinding) {
            super(userItemContainerBinding.getRoot());
            binding = userItemContainerBinding;
        }

        void setUserData(UserModel userModel) {
            binding.TextChatUserName.setText(userModel.username);
            binding.TextChatEmail.setText(userModel.email);
            binding.ImageChatProfile.setImageBitmap(getUserImage(userModel.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(userModel));
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}