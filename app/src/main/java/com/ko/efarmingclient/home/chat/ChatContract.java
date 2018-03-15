package com.ko.efarmingclient.home.chat;

import android.content.Context;

import com.ko.efarmingclient.model.Chat;
import com.ko.efarmingclient.model.ProductInfo;


public interface ChatContract {
    interface View {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);

        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);

        void onGetOnlineStatus(boolean isOnline , long timeStamp);

    }

    interface Presenter {
        void sendMessage(Context context, Chat chat, String receiverFirebaseToken, ProductInfo key);

        void getMessage(String senderUid, String receiverUid, String key);

        void getOnlineStatus(String receiverUid);
    }

    interface Interactor {
        void sendMessageToFirebaseUser(Context context, Chat chat, String receiverFirebaseToken, ProductInfo key);

        void getMessageFromFirebaseUser(String senderUid, String receiverUid, String key);
    }

    interface OnSendMessageListener {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);
    }

    interface OnGetMessagesListener {
        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }

    interface OnOnlineStatusListener {
        void onSendOnlineStatus(boolean isOnline , long timeStamp);
    }
}
