package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerReceivedMessageBinding;
import com.example.appchat.databinding.ItemContainerSentMessageBinding;
import com.example.appchat.models.ChatMessageModel;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessageModel> chatMessageModels;
    private Bitmap receiverProfileImage;
    private final String senderId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessageModel> chatMessageModels, Bitmap receiverProfileImage, String senderId) {
        this.chatMessageModels = chatMessageModels;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new sentMessageViewholder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext())
                            , parent
                            , false
                    )
            );
        } else {
            return new receivedMessageViewholder(
                    ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext())
                            , parent
                            , false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((sentMessageViewholder) holder).setData(chatMessageModels.get(position));
        } else {
            ((receivedMessageViewholder) holder).setData(chatMessageModels.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageModels.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class sentMessageViewholder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        sentMessageViewholder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessageModel chatMessageModel) {
            binding.textMessage.setText(chatMessageModel.message);
            binding.textDateTime.setText(chatMessageModel.dateTime);
        }
    }

    static class receivedMessageViewholder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        public receivedMessageViewholder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessageModel chatMessageModel, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessageModel.message);
            binding.textDateTime.setText(chatMessageModel.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }

}
