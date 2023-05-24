package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.UserItemRecentConversationBinding;
import com.example.appchat.listeners.ConversationListener;
import com.example.appchat.models.ChatMessageModel;
import com.example.appchat.models.UserModel;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewholder> {

    private final List<ChatMessageModel> chatMessageModels;
    private final ConversationListener conversationListener;

    public RecentConversationAdapter(List<ChatMessageModel> chatMessageModels, ConversationListener conversationListener) {
        this.chatMessageModels = chatMessageModels;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewholder(
                UserItemRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext())
                        ,parent
                        ,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewholder holder, int position) {
        holder.setData(chatMessageModels.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageModels.size();
    }


    class ConversationViewholder extends RecyclerView.ViewHolder{

        UserItemRecentConversationBinding binding;

        public ConversationViewholder(UserItemRecentConversationBinding itemRecentConversationBinding) {
            super(itemRecentConversationBinding.getRoot());
            binding = itemRecentConversationBinding;
        }
        void setData(ChatMessageModel chatMessageModel){
            binding.ImageChatProfile.setImageBitmap(getConversationImage(chatMessageModel.conversationImage));
            binding.TextChatUserName.setText(chatMessageModel.conversationUserName);
            binding.TextRecentMessage.setText("Last Seen");
            binding.getRoot().setOnClickListener(v -> {
                UserModel userModel = new UserModel();
                userModel.id = chatMessageModel.conversationId;
                userModel.username = chatMessageModel.conversationUserName;
                userModel.image = chatMessageModel.conversationImage;
                conversationListener.onConversationClicked(userModel);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}