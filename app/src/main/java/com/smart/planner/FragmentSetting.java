package com.smart.planner;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.smart.planner.Classes.NetworkUtils;
import com.smart.planner.POJOs.User;
import com.smart.planner.LocalDB.SQLiteHelper;

public class FragmentSetting extends PreferenceFragmentCompat {

    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private SharedPreferences sharedPreferences;

    private NavigationView navigationView;
    private ImageView user_profile;
    private TextView user_name;

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private EditTextPreference reset_name;
    private Preference reset_pw;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preference_setting, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init method
        init();

        findPreference("reset_profile").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                startActivity(new Intent(getActivity(), ProfileScreen.class));
                return true;
            }
        });

        reset_name.setText(user_name.getText().toString());
        reset_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newName) {
                final String name = newName.toString().trim();
                if (!name.equals(user_name.getText().toString())) {
                    if (name.length() == 0 | name == null) {
                        Toast.makeText(getActivity(), "Please enter name first", Toast.LENGTH_SHORT).show();
                    } else {
                        if (name.length() <= 20) {
                            // PERFECT NAME //FOUND
                            if (NetworkUtils.isNetworkAvailable(getActivity().getApplicationContext())) {
                                final DocumentReference ref = firestore.collection("Users").document(Main.CURRENT_USER_KEY)
                                        .collection("Profile").document("LoginData");
                                ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {
                                        final User user = snapshot.toObject(User.class);
                                        user.setName(name);
                                        ref.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sharedPreferences.edit().putString("current_user_object", new Gson().toJson(user)).apply();
                                                Toast.makeText(getActivity(), "Successfully Name changed !", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            } else {
                                Toast.makeText(getActivity(), "Please check your internet connection !", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "User name is too long.please enter another", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            }
        });

        reset_pw.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ResetPassword resetPassword = new ResetPassword();
                resetPassword.show(getParentFragmentManager(), "reset_pw");
                return true;
            }
        });

    }

    private void init() {
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        navigationView = getActivity().findViewById(R.id.nav_view);
        user_profile = navigationView.getHeaderView(0).findViewById(R.id.user_profile);
        user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        reset_name = (EditTextPreference) findPreference("reset_name");
        reset_pw = findPreference("reset_pw");
    }

    // RESET PASSWORD STATIC INNER CLASS SCOPE
    public static class ResetPassword extends DialogFragment {
        private View thisView;
        private ResetPassword resetPassword;
        private Dialog thisDialog;

        private Button resetBtn, cancelBtn;
        private EditText current_pw, new_pw, retype_new_pw;

        private FirebaseFirestore firestore;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            thisView = inflater.inflate(R.layout.dialog_reset_password, null);
            resetPassword = this;
            builder.setView(thisView);
            initComponents(thisView);
            thisDialog = builder.create();
            return thisDialog;
        }

        private void initComponents(View v) {
            resetBtn = v.findViewById(R.id.pw_reset);
            cancelBtn = v.findViewById(R.id.cancel);

            current_pw = v.findViewById(R.id.current_pw);
            new_pw = v.findViewById(R.id.new_pw);
            retype_new_pw = v.findViewById(R.id.retype_new_pw);

            firestore = FirebaseFirestore.getInstance();

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisDialog.dismiss();
                }
            });

            resetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (current_pw.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getContext(), "You must provide your current password to reset password !", Toast.LENGTH_SHORT).show();
                    } else if (new_pw.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getContext(), "You must provide your new password to reset password !", Toast.LENGTH_SHORT).show();
                    } else if (retype_new_pw.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getContext(), "You must confirm new password to reset password !", Toast.LENGTH_SHORT).show();
                    } else {
                        if (new_pw.getText().toString().trim().equals(retype_new_pw.getText().toString().trim())) {
                            // remember to write here if of check password in correct password or not
                            if (Main.CURRENT_USER_KEY != null) {
                                if (NetworkUtils.isNetworkAvailable(getActivity().getApplicationContext())) {
                                    final DocumentReference ref = firestore.collection("Users").document(Main.CURRENT_USER_KEY)
                                            .collection("Profile").document("LoginData");
                                    ref.get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot snapshot) {
                                                    // USER OBJECT FOUND
                                                    User user = snapshot.toObject(User.class);
                                                    if (user.getPassword().trim().equals(current_pw.getText().toString().trim())) {
                                                        if (!user.getPassword().equals(new_pw.getText().toString().trim())) {
                                                            user.setPassword(new_pw.getText().toString().trim());
                                                            ref.set(user)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(getContext(), "Successfully password changed !", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(getContext(), "password can't changed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                                                                        }
                                                                    });
                                                        } else {
                                                            // IF NEW PASSWORD AND CURRENT PASSWORDS ARE EQUAL
                                                            Toast.makeText(getContext(), "write your new password to reset password !", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }else {
                                                        Toast.makeText(getContext(), "Provide your current password to confirm is you !", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getActivity(), "Something went wrong " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(getActivity(), "Please check your internet connection !", Toast.LENGTH_SHORT).show();
                                }
                            }
                            // end remember
                        } else {
                            Toast.makeText(getContext(), "You must confirm new password to reset password !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
    // RESET PASSWORD STATIC INNER CLASS SCOPE
}
