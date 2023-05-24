package com.example.appchat.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.adapter.RecentConversationAdapter;
import com.example.appchat.databinding.ActivityMainBinding;
import com.example.appchat.listeners.ConversationListener;
import com.example.appchat.models.ChatMessageModel;
import com.example.appchat.models.RecyclerView_ItemDecoration;
import com.example.appchat.models.UserModel;
import com.example.appchat.utilities.Constant;
import com.example.appchat.utilities.PreferenceManger;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {

    private ActivityMainBinding binding;
    private PreferenceManger preferenceManger;
    private List<ChatMessageModel> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        preferenceManger = new PreferenceManger(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListener();
        listenerConversation();

    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //    Toast Function
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setListener() {
        binding.ImageSignOut.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.Dialog);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.dialog_layout);
            dialog.show();

            Button YesBtn, NoBtn;
            YesBtn = dialog.findViewById(R.id.YesBtn);
            NoBtn = dialog.findViewById(R.id.NoBtn);

            YesBtn.setOnClickListener(v1 -> {
                signOut();
                dialog.dismiss();
            });
            NoBtn.setOnClickListener(v1 -> dialog.dismiss());
        });
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UsersActivity.class)));
    }

    //    Recent Coversation users
    private void init() {
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    //    Display Details And ProfilePicture On MainScreen
    public void loadUserDetails() {
        binding.TextUserName.setText(preferenceManger.getString(Constant.KEY_USERNAME));
        byte[] bytes = Base64.decode(preferenceManger.getString(Constant.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.ProfilePic.setImageBitmap(bitmap);
    }

    private void listenerConversation() {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManger.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManger.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    ChatMessageModel chatMessageModel = new ChatMessageModel();
                    chatMessageModel.senderId = senderId;
                     chatMessageModel.receiverId = receiverId;
                    if (preferenceManger.getString(Constant.KEY_USER_ID).equals(senderId)) {
                        chatMessageModel.conversationImage = documentChange.getDocument().getString(Constant.KEY_RECEIVER_IMAGE);
                        chatMessageModel.conversationUserName = documentChange.getDocument().getString(Constant.KEY_RECEIVER_USERNAME);
                        chatMessageModel.conversationId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    } else {
                        chatMessageModel.conversationImage = documentChange.getDocument().getString(Constant.KEY_SENDER_IMAGE);
                        chatMessageModel.conversationUserName = documentChange.getDocument().getString(Constant.KEY_SENDER_USERNAME);
                        chatMessageModel.conversationId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    }
                    chatMessageModel.message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                    chatMessageModel.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    conversations.add(chatMessageModel);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderid = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                        String receiverid = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderid) && conversations.get(i).receiverId.equals(receiverid)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
//            Collections.sort(conversations,(obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.addItemDecoration(new RecyclerView_ItemDecoration(25));
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.ProgressBar.setVisibility(View.GONE);
        }
    };

    //        Token for SignOut Implementation
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManger.putString(Constant.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManger.getString(Constant.KEY_USER_ID));
        documentReference.update(Constant.KEY_FCM_TOKEN, token).addOnFailureListener(e -> showToast("Unable to Update Token"));
    }

    //    SignOut Function
    private void signOut() {
        showToast("Signing Out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constant.KEY_COLLECTION_USERS).document(
                preferenceManger.getString(Constant.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManger.clear();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("Unable to SignOut"));
    }

    @Override
    public void onConversationClicked(UserModel userModel) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_USER, userModel);
        startActivity(intent);
    }
}