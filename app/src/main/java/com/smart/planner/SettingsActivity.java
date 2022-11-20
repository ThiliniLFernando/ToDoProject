package com.smart.planner;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.smart.planner.Classes.NetworkUtils;
import com.smart.planner.LocalDB.SQLiteHelper;
import com.smart.planner.POJOs.User;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "Settings";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    // header fragment
    public static class SettingFragment extends PreferenceFragmentCompat {
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
        private Preference reset_pw,darkTheme;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_setting, rootKey);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            init();
            initListeners();
        }

        private void init() {
            firestore = FirebaseFirestore.getInstance();
            firebaseStorage = FirebaseStorage.getInstance();
            storageReference = firebaseStorage.getReference();

            navigationView = getActivity().findViewById(R.id.nav_view);
            //user_profile = navigationView.getHeaderView(0).findViewById(R.id.user_profile);
            //user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            reset_name = (EditTextPreference) findPreference("reset_name");
            reset_pw = findPreference("reset_pw");
            darkTheme = (SwitchPreference) findPreference("night_mode");
        }

        private void initListeners() {
            findPreference("reset_profile").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference p) {
                    startActivity(new Intent(getActivity(), ProfileScreen.class));
                    return true;
                }
            });

            // reset_name.setText(user_name.getText().toString());
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
                    SettingsActivity.ResetPassword resetPassword = new SettingsActivity.ResetPassword();
                    resetPassword.show(getParentFragmentManager(), "reset_pw");
                    return true;
                }
            });

            darkTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean onOff = ((SwitchPreference) preference).isChecked();
                    onOff = !onOff;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isDarkTheme",onOff);
                    editor.apply();
                    Intent main = new Intent(getContext(), Main.class);
                    startActivity(main);
                    main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(new Intent(getContext(), SettingsActivity.class));

                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    return true;
                }
            });
        }
    }

    // RESET PASSWORD STATIC INNER CLASS SCOPE
    public static class ResetPassword extends DialogFragment {
        private View thisView;
        private SettingsActivity.ResetPassword resetPassword;
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
                                                    } else {
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

    // theme fragment
    public static class ThemeFragment extends Fragment {
        private RadioButton defaultRb, summerRb, autumnRb, winterRb, sakuraRb, sunshineRb;
        private RadioButton[] radioButtons;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_theme, container, false);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            init(view);
            initListeners();
        }

        private void initListeners() {
            applyTheme();
            for (RadioButton rb : radioButtons) {
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            uncheckOtherRBs(rb);
                            changeTheme(rb);
                        }
                    }
                });
            }
        }

        private void changeTheme(RadioButton rb) {
            Toast.makeText(getContext(), "" + rb.getText(), Toast.LENGTH_SHORT).show();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            boolean updated = true;
            switch (rb.getText().toString().trim().toLowerCase()) {
                case "default":
                    editor.putInt("theme", 1);
                    break;
                case "summer":
                    editor.putInt("theme", 2);
                    break;
                case "autumn":
                    editor.putInt("theme", 3);
                    break;
                case "sakura":
                    editor.putInt("theme", 4);
                    break;
                case "sunshine":
                    editor.putInt("theme", 5);
                    break;
                case "winter":
                    editor.putInt("theme", 6);
                    break;
                default:
                    updated = false;
            }
            editor.apply();
            Intent main = new Intent(getContext(), Main.class);
            startActivity(main);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(new Intent(getContext(), SettingsActivity.class));

            getActivity().finish();
            getActivity().overridePendingTransition(0, 0);
            if (updated)
                Toast.makeText(getContext(), rb.getText() + " theme updated successfully", Toast.LENGTH_SHORT).show();
        }

        private void uncheckOtherRBs(RadioButton checkRb) {
            for (RadioButton rb : radioButtons) {
                if (rb.getId() != checkRb.getId())
                    rb.setChecked(false);
            }
        }

        private void init(View v) {
            defaultRb = v.findViewById(R.id.rb_default);
            summerRb = v.findViewById(R.id.rb_summer);
            autumnRb = v.findViewById(R.id.rb_autumn);
            sakuraRb = v.findViewById(R.id.rb_sakura);
            sunshineRb = v.findViewById(R.id.rb_sunshine);
            winterRb = v.findViewById(R.id.rb_winter);
            radioButtons = new RadioButton[]{defaultRb, summerRb, autumnRb, winterRb, sakuraRb, sunshineRb};

        }

        private void applyTheme() {
            int theme = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext())
                    .getInt("theme", -1);
            if (theme != -1) {
                switch (theme) {
                    case 1:
                        defaultRb.setChecked(true);
                        uncheckOtherRBs(defaultRb);
                        break;
                    case 2:
                        summerRb.setChecked(true);
                        uncheckOtherRBs(summerRb);
                        break;
                    case 3:
                        autumnRb.setChecked(true);
                        uncheckOtherRBs(autumnRb);
                        break;
                    case 4:
                        sakuraRb.setChecked(true);
                        uncheckOtherRBs(sakuraRb);
                        break;
                    case 5:
                        sunshineRb.setChecked(true);
                        uncheckOtherRBs(sunshineRb);
                        break;
                    case 6:
                        winterRb.setChecked(true);
                        uncheckOtherRBs(winterRb);
                        break;
                }
            }
        }
    }
}