package com.example.appchat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.appchat.adapter.ChatAdapter;
import com.example.appchat.databinding.ActivityChatBinding;
import com.example.appchat.models.ChatMessageModel;
import com.example.appchat.models.UserModel;
import com.example.appchat.network.ApiClient;
import com.example.appchat.network.ApiService;
import com.example.appchat.utilities.Constant;
import com.example.appchat.utilities.PreferenceManger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private UserModel receiverUserModel;
    private List<ChatMessageModel> chatMessageModels;
    private ChatAdapter chatAdapter;
    private PreferenceManger preferenceManger;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void init() {
        preferenceManger = new PreferenceManger(getApplicationContext());
        chatMessageModels = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageModels,
                getBitmapFromEncodedString(receiverUserModel.image),
                preferenceManger.getString(Constant.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        if (!binding.inputMessage.getText().toString().isEmpty()) {
            message.put(Constant.KEY_SENDER_ID, preferenceManger.getString(Constant.KEY_USER_ID));
            message.put(Constant.KEY_RECEIVER_ID, receiverUserModel.id);
            message.put(Constant.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constant.KEY_TIMESTAMP, new Date());
            database.collection(Constant.KEY_COLLECTION_CHAT).add(message);
            if (conversationId!=null){
                updateConversation(binding.inputMessage.getText().toString());
            } else {
                HashMap<String , Object> conversation = new HashMap<>();
                conversation.put(Constant.KEY_SENDER_ID,preferenceManger.getString(Constant.KEY_USER_ID));
                conversation.put(Constant.KEY_SENDER_USERNAME,preferenceManger.getString(Constant.KEY_USERNAME));
                conversation.put(Constant.KEY_SENDER_IMAGE,preferenceManger.getString(Constant.KEY_IMAGE));
                conversation.put(Constant.KEY_RECEIVER_ID,receiverUserModel.id);
                conversation.put(Constant.KEY_RECEIVER_USERNAME,receiverUserModel.username);
                conversation.put(Constant.KEY_RECEIVER_IMAGE,receiverUserModel.image);
                conversation.put(Constant.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
                conversation.put(Constant.KEY_TIMESTAMP,new Date());
                addConversation(conversation);
            }
            if (isReceiverAvailable){
                try {
                    JSONArray tokens  = new JSONArray();
                    tokens.put(receiverUserModel.token);

                    JSONObject data = new JSONObject();
                    data.put(Constant.KEY_USER_ID , preferenceManger.getString(Constant.KEY_USER_ID));
                    data.put(Constant.KEY_USERNAME,preferenceManger.getString(Constant.KEY_USERNAME));
                    data.put(Constant.KEY_FCM_TOKEN , preferenceManger.getString(Constant.KEY_FCM_TOKEN));
                    data.put(Constant.KEY_MESSAGE , binding.inputMessage.getText().toString());

                    JSONObject body = new JSONObject();
                    body.put(Constant.REMOTE_MSG_DATA,data);
                    body.put(Constant.REMOTE_MSG_REGIDTRATION_IDS,tokens);

                    sendNotification(body.toString());
                } catch (Exception e){
                    showToast(e.getMessage());
                }
            }
            binding.inputMessage.setText(null);
        }
    }

//    send notification
    private void sendNotification(String messagebody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constant.getRemoteMsgHeader(),
                messagebody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray   results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("faliure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    showToast("Notification sent Successfully");
                } else{
                    showToast("Error "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


//    online or offline textview visibility occured according to the KEY_AVAILABILITY values(0 or 1) data stored in firebasefirestore
    private void listenAvailabilityOfReceiver(){
        database.collection(Constant.KEY_COLLECTION_USERS).document(
                receiverUserModel.id
        ).addSnapshotListener(ChatActivity.this,(value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constant.KEY_AVAILABILITY) != null) {
                    isReceiverAvailable = Objects.requireNonNull(value.getLong(Constant.KEY_AVAILABILITY)).intValue() == 1;
                }
                receiverUserModel.token = value.getString(Constant.KEY_FCM_TOKEN);
                if (receiverUserModel.image == null){
                    receiverUserModel.image = value.getString(Constant.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUserModel.image));
                    chatAdapter.notifyItemRangeChanged(0 , chatMessageModels.size());
                }
            }

            if (receiverUserModel.image == null){
                receiverUserModel.image = value.getString(Constant.KEY_IMAGE);
                chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUserModel.image));
                chatAdapter.notifyItemRangeChanged(0 , chatMessageModels.size());
            }

            if (isReceiverAvailable){
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessages() {
        database.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManger.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverUserModel.id)
                .addSnapshotListener(eventListener);
        database.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, receiverUserModel.id)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManger.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageModels.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessageModel chatMessageModel = new ChatMessageModel();
                    chatMessageModel.senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    chatMessageModel.receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    chatMessageModel.message = documentChange.getDocument().getString(Constant.KEY_MESSAGE);
                    chatMessageModel.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP));
                    chatMessageModel.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    chatMessageModels.add(chatMessageModel);
                }
            }
            chatMessageModels.sort(Comparator.comparing(obj -> obj.dateObject));
//            Collections.sort(chatMessageModels,(obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageModels.size(), chatMessageModels.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageModels.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.ProgressBar.setVisibility(View.GONE);
        if (conversationId==null){
            checkForConversation();
        }
    });

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUserModel = (UserModel) getIntent().getSerializableExtra(Constant.KEY_USER);
        binding.textUsername.setText(receiverUserModel.username);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation){
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message){
        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constant.KEY_LAST_MESSAGE, message,
                Constant.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation(){
        if (chatMessageModels.size() != 0){
            checkForConversationRemotely(
                    preferenceManger.getString(Constant.KEY_USER_ID),
                    receiverUserModel.id
            );
            checkForConversationRemotely(
                    receiverUserModel.id,
                    preferenceManger.getString(Constant.KEY_USER_ID)
            );
        }
    }

    private void checkForConversationRemotely(String senderId , String receiverId){
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constant.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}