package com.ko.efarmingclient.home.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.chat.ChatContract;
import com.ko.efarmingclient.home.chat.ChatPresenter;
import com.ko.efarmingclient.home.adapters.ChatRecyclerAdapter;
import com.ko.efarmingclient.model.Chat;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.DeviceUtils;


import java.util.ArrayList;

import static com.ko.efarmingclient.EFApp.getApp;


public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener {
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;

    private String receiverUid, receiver, receiverFirebaseToken;
    private ProductInfo productId;
    private ArrayList<Chat> chatArrayList;

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firebaseToken) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mRecyclerViewChat.setLayoutManager(new LinearLayoutManager(getActivity()));
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
        view.findViewById(R.id.fab_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceUtils.hideSoftKeyboard(getActivity());
                sendMessage();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        chatArrayList = new ArrayList<Chat>();
        mETxtMessage.setOnEditorActionListener(this);

        getReceiverID();
        productId = (ProductInfo) getArguments().getSerializable("Product_id");

        mChatPresenter = new ChatPresenter(this);

        mChatRecyclerAdapter = new ChatRecyclerAdapter(getActivity(),chatArrayList);
        mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);


    }

    private void getInfo() {


    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString();
//        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
//        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(),
                chat,
                receiverFirebaseToken, productId);
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (chat == null) {
            return;
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
        mChatRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

//    @Subscribe
//    public void onPushNotificationEvent(PushNotificationEvent pushNotificationEvent) {
//        if (mChatRecyclerAdapter == null || mChatRecyclerAdapter.getItemCount() == 0) {
//            mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
//                    pushNotificationEvent.getUid());
//        }
//    }

    public void getReceiverID() {
        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ProductInfo productInfo = snapshot.getValue(ProductInfo.class);
                        receiverUid = productInfo.user_info.uid;
                        receiver = productInfo.user_info.email;
                        receiverFirebaseToken = productInfo.user_info.firebaseToken;
                        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(), receiverUid, productId.productID);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(mChatRecyclerAdapter != null)
//            mChatRecyclerAdapter.notifyDataSetChanged();
//    }
}
