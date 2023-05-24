package com.example.appchat.utilities;

import java.util.HashMap;

public class Constant {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_USERNAME = "senderName";
    public static final String KEY_RECEIVER_USERNAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availabilty";
    public static final String REMOTE_MSG_AUTHORZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE ="Content-Type";
    public static final String REMOTE_MSG_DATA ="data";
    public static final String REMOTE_MSG_REGIDTRATION_IDS ="registration_ids";


    public static HashMap<String , String> remoteMsgHeaders = null;
    public static HashMap<String , String> getRemoteMsgHeader() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORZATION,
                    "AAAAj-XzwFw:APA91bG8oMIL-PFxVb4pab6ErKc8SlYuBZR6-5_JqZYNIjBGq7bQ544b87LqMbcbx3945h3WoLv_PjluZ7voDswn4w5PR5Y5j7NqowLiqQjzWk8UaxJzR2XBemcXMuwbKu02fqCzsYNe"
            );
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
 }
