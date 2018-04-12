package com.ko.efarmingclient.home.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.login.LoginActivity;
import com.ko.efarmingclient.model.User;
import com.ko.efarmingclient.ui.EFProgressDialog;
import com.ko.efarmingclient.util.AlertUtils;
import com.ko.efarmingclient.util.CameraUtils;
import com.ko.efarmingclient.util.CompressImage;
import com.ko.efarmingclient.util.DeviceUtils;
import com.ko.efarmingclient.util.TempManager;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.ko.efarmingclient.EFApp.getApp;
import static com.ko.efarmingclient.util.Constants.PERMISSIONS;
import static com.ko.efarmingclient.util.Constants.REQUEST_PERMISSION_READ_STORAGE;
import static com.ko.efarmingclient.util.Constants.REQUEST_PICTURE_FROM_CAMERA;
import static com.ko.efarmingclient.util.Constants.REQUEST_PICTURE_FROM_GALLERY;
import static com.ko.efarmingclient.util.DeviceUtils.hideSoftKeyboard;
import static com.ko.efarmingclient.util.TextUtils.isValidEmail;

public class ProfileFragment extends Fragment {
    private String[] imagPaths = null;
    private ImageView imgPhoto;
    private TextView txtSignUp;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mName;
    private EditText mConfirmPass;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPassLayout;
    private Button mEmailSignInButton;
    private String imagerls = "";
    private String imagePathForFireBase = "";
    private CameraUtils cameraUtils;
    private RadioGroup radioGroup;
    private boolean isResetPassword = false;
    private EFProgressDialog efProgressDialog;
    private String email;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICTURE_FROM_GALLERY) {
                imagPaths = cameraUtils.getPaths(getActivity(), data, true);
                imagePathForFireBase = imagPaths[0];
                File file = new File(imagPaths[0]);
                if (file.exists()) {
                    cameraUtils.startCrop(file.getAbsolutePath());
                }

            } else if (requestCode == REQUEST_PICTURE_FROM_CAMERA) {
                File f = TempManager.getTempPictureFile(getActivity());
                if (f != null) {
                    String path = f.getAbsolutePath();


                    CompressImage compressImage = new CompressImage(getActivity());
                    path = compressImage.compressImage(path);


                    imagPaths = new String[]{path};
                    File file = new File(imagPaths[0]);
                    imagePathForFireBase = file.getAbsolutePath();
                    if (file.exists()) {
                        cameraUtils.startCrop(file.getAbsolutePath());
                    }
                }
            }
            if (requestCode == Crop.REQUEST_CROP) {
                handleCrop(resultCode, data);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        setupDefault();
        setupEvents();
        return view;
    }

    private void init(View view) {
        imgPhoto = view.findViewById(R.id.img_profile_photo);
        mEmailView = view.findViewById(R.id.email);
        mPasswordView = view.findViewById(R.id.password);
        nameLayout = view.findViewById(R.id.name_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        passwordLayout = view.findViewById(R.id.password_layout);
        confirmPassLayout = view.findViewById(R.id.confirm_pass_layout);
        mEmailSignInButton = view.findViewById(R.id.email_sign_up_button);
        mName = view.findViewById(R.id.name);
        mConfirmPass = view.findViewById(R.id.conf_password);
        cameraUtils = new CameraUtils(getActivity(), getActivity());
        radioGroup = view.findViewById(R.id.rg_reset_password);
        efProgressDialog = new EFProgressDialog(getActivity());
        view.findViewById(R.id.txt_log_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });
    }

    private void setupDefault() {
        getChatUserInfo();
    }

    private void setupEvents() {
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceUtils.hideSoftKeyboard(getActivity(), view);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (cameraUtils.checkAndRequestCameraPermissions()) {
                        cameraUtils.promptMediaOption();
                    }
                } else {
                    cameraUtils.promptMediaOption();
                }
            }
        });

        addTextChangeListener(mEmailView, emailLayout);
        addTextChangeListener(mPasswordView, passwordLayout);
        addTextChangeListener(mConfirmPass, confirmPassLayout);
        addTextChangeListener(mName, nameLayout);

        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptEditProfile();
            }
        });

        mConfirmPass.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                attemptEditProfile();
                return false;
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.rb_yes) {
                    isResetPassword = true;
                    passwordLayout.setVisibility(View.VISIBLE);
                    confirmPassLayout.setVisibility(View.VISIBLE);
                } else {
                    passwordLayout.setVisibility(View.GONE);
                    confirmPassLayout.setVisibility(View.GONE);
                    isResetPassword = false;
                }
            }
        });

        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptEditProfile();
            }
        });

        getApp().getFireBaseAuth().addAuthStateListener(authListener);

    }


    public void addTextChangeListener(EditText editText, final TextInputLayout textInputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textInputLayout.setError(null);
                textInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_STORAGE: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
//                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(PERMISSIONS, 5);
                            }
                        } else {
                            cameraUtils.promptMediaOption();
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
//                            showRequestDialog();
                            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.storage_permission_required), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    cameraUtils.checkAndRequestCameraPermissions();
                                }
                            }, false);
                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(getResources().getString(R.string.go_to_settings_enable_permission));
//                                builder.setMessage(String.format(getString(R.string.denied_msg), type));
                            builder.setPositiveButton(getResources().getString(R.string.go_to_appsettings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    goToSettings();
                                }
                            });
                            builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                            builder.setCancelable(false);
                            builder.show();

                        }
                    }
                }
            }

            break;
            case 5:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraUtils.promptMediaOption();
                } else {
//                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                        promptSettings(getResources().getString(R.string.camera));
                    } else {
                        //                            showRequestDialog();
                        AlertUtils.showAlert(getActivity(), getResources().getString(R.string.storage_permission_required), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cameraUtils.checkAndRequestCameraPermissions();
                            }
                        }, false);
                    }
                }
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri uri = Crop.getOutput(result);
            imagPaths = new String[]{uri.getPath()};
            File file = new File(imagPaths[0]);
            if (file.exists()) {
                imgPhoto.setImageURI(uri);
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptEditProfile() {

        hideSoftKeyboard(getActivity());

        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (TextUtils.isEmpty(mName.getText())) {
            nameLayout.setError("Enter your name");
            nameLayout.setErrorEnabled(true);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Enter your email address");
            emailLayout.setErrorEnabled(true);
            return;
        }

        if (!isValidEmail(email)) {
            emailLayout.setError("Enter a valid email address");
            emailLayout.setErrorEnabled(true);
            return;
        }

        if (isResetPassword & TextUtils.isEmpty(password)) {
            passwordLayout.setError("Enter a password");
            passwordLayout.setErrorEnabled(true);
            return;
        }

        if (isResetPassword & TextUtils.isEmpty(mConfirmPass.getText().toString())) {
            confirmPassLayout.setError("Enter a confirm password");
            confirmPassLayout.setErrorEnabled(true);
            return;
        }

        if (isResetPassword & password.length() < 6) {
            passwordLayout.setError("Enter minimum 6 characters");
            passwordLayout.setErrorEnabled(true);
            return;
        }

        if (isResetPassword & mConfirmPass.length() < 6) {
            confirmPassLayout.setError("Enter minimum 6 characters");
            confirmPassLayout.setErrorEnabled(true);
            return;
        }

        if (isResetPassword & !password.equals(mConfirmPass.getText().toString())) {
            AlertUtils.showAlert(getActivity(), "Please enter same password in both fields", null, false);
            return;
        }

        efProgressDialog.show();

        doEditProfile();
    }

    private void doEditProfile() {

        getApp().getFireBaseAuth().getCurrentUser().updateEmail(mEmailView.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Uri mImageUri = Uri.fromFile(new File(imagePathForFireBase));
                            if (!TextUtils.isEmpty(imagePathForFireBase)) {
                                StorageReference filepath = getApp().getFireBaseStorage().getReference().child("user_profile").child(imagePathForFireBase);
                                filepath.putFile(mImageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        if (progress == 100) {
//                                        hideProgressDialog();
                                            //upload();
                                        }
                                        System.out.println("Upload is " + progress + "% done");
                                    }
                                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                        System.out.println("Upload is paused");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        /** Get Image Download Path**/
                                        Uri downloadUri = taskSnapshot.getDownloadUrl();

                                        /** Converting Image Uri In String **/

                                        if (downloadUri != null) {
                                            imagerls = downloadUri.toString();
                                        }

                                        if (isResetPassword) {
                                            doResetPassword();
                                        }
                                        if (downloadUri != null) {
                                            updateUserToDatabase(getActivity(), getApp().getFireBaseAuth().getCurrentUser(), imagerls);
                                        }else{
                                            updateUserToDatabase(getActivity(), getApp().getFireBaseAuth().getCurrentUser(),"");
                                        }
                                        callUpdateName();
                                    }

                                });

                            } else {

                                if (isResetPassword) {
                                    doResetPassword();
                                }

                                updateUserToDatabase(getActivity(), getApp().getFireBaseAuth().getCurrentUser(),"");
                                callUpdateName();
                            }
                        }
                    }
                });
    }


    private void updateUserToDatabase(FragmentActivity activity, FirebaseUser currentUser, final String imageString) {
        final DatabaseReference objRef = FirebaseDatabase.getInstance()
                .getReference().child("client_users").child(getApp().getFireBaseAuth().getUid()).child("email");
        objRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                objRef.setValue(mEmailView.getText().toString());
                if(!TextUtils.isEmpty(imageString)){
                    uploadImageIntoProfile(imageString);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                efProgressDialog.dismiss();
            }
        });
    }

    private void uploadImageIntoProfile(final String imageString) {
        final DatabaseReference objRef = FirebaseDatabase.getInstance()
                .getReference().child("client_users").child(getApp().getFireBaseAuth().getUid()).child("userImage");
        objRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                objRef.setValue(imageString);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                efProgressDialog.dismiss();
            }
        });
    }

    private void callUpdateName(){
        final DatabaseReference objRef = FirebaseDatabase.getInstance()
                .getReference().child("client_users").child(getApp().getFireBaseAuth().getUid()).child("name");
        objRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                objRef.setValue(mName.getText().toString());
                if (isAdded()) {
                    Toast.makeText(getActivity(), "Profile updated  successful", Toast.LENGTH_LONG).show();
                }
                efProgressDialog.dismiss();
                if(!isResetPassword) {
                    doLogout();
                }else{
                    startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("email",mEmailView.getText().toString()));
                    getActivity().finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                efProgressDialog.dismiss();
            }
        });
    }

    private void doResetPassword() {

        FirebaseUser user = getApp().getFireBaseAuth().getCurrentUser();

        user.updatePassword(mPasswordView.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }

    public void promptSettings(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getResources().getString(R.string.denied_title), type));
        builder.setMessage(String.format(getString(R.string.denied_msg), type));
        builder.setPositiveButton(getString(R.string.go_to_appsettings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);
        builder.show();
    }

    public void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(myAppSettings);
    }

    private void getChatUserInfo() {
        FirebaseDatabase.getInstance()
                .getReference().child("client_users").child(getApp().getFireBaseAuth().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mName.setText(user.name);
                mEmailView.setText(user.email);
                if(!TextUtils.isEmpty(user.userImage) && isAdded()) {
                    Picasso.get().load(user.userImage).placeholder(R.drawable.ic_account_circle_black_48dp).into(imgPhoto);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (!isResetPassword && user == null) {
                startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("email",email));
                getActivity().finish();
            }
        }
    };

    public void doLogout() {
        email = mEmailView.getText().toString();
        getApp().getFireBaseAuth().signOut();
    }


}
