package com.smart.planner;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.smart.planner.Classes.Converters;
import com.smart.planner.Classes.RequestCodes;
import com.smart.planner.JobService.QuarterChecker;
import com.smart.planner.POJOs.User;
import com.smart.planner.reminder.QuarterAlarmReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int DEFAULT = 1, SUMMER = 2, AUTUMN = 3, SAKURA = 4, SUNSHINE = 5,WINTER=6;
    public static String CURRENT_USER_KEY;

    public final static String PROFILE_PATH = "/profile/current_profile.jpg";
    private static String profileSignature;

    //shared preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    protected DrawerLayout drawerLayout;
    Toolbar toolbar;
    private NavigationView navigationView;
    private TextView user_name, user_email;
    private ImageView user_profile;
    private RelativeLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private MaterialCardView addList, viewSettings;

    private Fragment selectedFragment = null;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private StorageReference profileReference;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat;
    private BroadcastReceiver minuteUpdater;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        this.overridePendingTransition(R.anim.layout_translate_ltr_design, R.anim.layout_translate_rtl_design);
        if (savedInstanceState == null) {
            selectedFragment = new FragmentTasks();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            getSupportActionBar().setTitle("Task");
            // navigationView.setCheckedItem(R.id.nav_settings);
        }

        // check app open first time or not
        if (sharedPreferences.getBoolean("first_time_app_open", false)) {
            String permissions[] = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            requestPermissions(permissions, RequestCodes.ALL_PERMISSION_REQUEST_CODE);
        } else {
            checkPermissions();
        }


    }

    public static void applyTheme(Activity a) {
        int theme = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext())
                .getInt("theme",-1);
        if(theme != -1) {
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext())
                    .getBoolean("isDarkTheme",false);
            if(!isDarkTheme) {
                switch (theme) {
                    case 1:
                        a.setTheme(R.style.AppTheme);
                        break;
                    case 2:
                        a.setTheme(R.style.SummerTheme);
                        break;
                    case 3:
                        a.setTheme(R.style.AutumnTheme);
                        break;
                    case 4:
                        a.setTheme(R.style.SakuraTheme);
                        break;
                    case 5:
                        a.setTheme(R.style.SunshineTheme);
                        break;
                    case 6:
                        a.setTheme(R.style.WinterTheme);
                        break;
                }
            }else{
                switch (theme) {
                    case 1:
                        a.setTheme(R.style.AppDarkTheme);
                        break;
                    case 2:
                        a.setTheme(R.style.SummerDarkTheme);
                        break;
                    case 3:
                        a.setTheme(R.style.AutumnDarkTheme);
                        break;
                    case 4:
                        a.setTheme(R.style.SakuraDarkTheme);
                        break;
                    case 5:
                        a.setTheme(R.style.SunshineDarkTheme);
                        break;
                    case 6:
                        a.setTheme(R.style.WinterDarkTheme);
                        break;
                }
            }
        }
    }

    private void setProfileImage() {
        Glide.with(this)
                .load(profileReference)
                .apply(new RequestOptions().signature(new ObjectKey(profileSignature)))
                .into(user_profile);
    }

    protected static void changeProfileSignature() {
        profileSignature = Calendar.getInstance().getTimeInMillis() + "";
    }

    protected static String getProfileSignature() {
        return profileSignature;
    }

    private void initComponents() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        Main.CURRENT_USER_KEY = sharedPreferences.getString("current_user_KEY", null);
        scheduleHalfNotificationChecker();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_navView);
        profileSignature = Calendar.getInstance().getTimeInMillis() + "";

        user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        user_profile = navigationView.getHeaderView(0).findViewById(R.id.user_profile);
        viewSettings = navigationView.getHeaderView(0).findViewById(R.id.report_card);
        addList = navigationView.getHeaderView(0).findViewById(R.id.add_list_card);

        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        viewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DialogNewList();
                newFragment.show(getSupportFragmentManager(), "new_list_dialog");
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("current_user_object")) {
                    setUserInfoToNavViewHeader();
                }
            }
        };

        storageRef = FirebaseStorage.getInstance().getReference();
        profileReference = storageRef.child(CURRENT_USER_KEY + "" + PROFILE_PATH);

        setUserInfoToNavViewHeader();

        firestore = FirebaseFirestore.getInstance();
        // rewrite profile image file when Profile changed !
        firestore.collection("Users").document(CURRENT_USER_KEY)
                .collection("profilePath").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        uploadProfileImageToStorage(Main.CURRENT_USER_KEY);
                    }
                });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setProfileImage();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_logout:
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().clear().commit();
                        startActivity(new Intent(getApplicationContext(), SignIn.class));
                        finish();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_task:
                        selectedFragment = new FragmentTasks();
                        getSupportActionBar().setTitle("Task");
                        break;

                    case R.id.page_calendar:
                        selectedFragment = new CustomCalendarFragment();
                        getSupportActionBar().setTitle("Calender");
                        break;

                    case R.id.page_list:
                        selectedFragment = new FragmentList();
                        getSupportActionBar().setTitle("List");
                        break;

                    case R.id.page_location:
                        selectedFragment = new FragmentLocationReminder();
                        getSupportActionBar().setTitle("");
                        break;

                    case R.id.page_reports:
                        selectedFragment = new FragmentReports();
                        getSupportActionBar().setTitle("Reports");
                        break;

                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .setCustomAnimations(R.anim.layout_translate_rtl_design, R.anim.layout_translate_ltr_design)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

    }

    public void startMinuteUpdater() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        minuteUpdater = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                counter++;
                Toast.makeText(context, "" + counter, Toast.LENGTH_SHORT).show();
            }
        };

        registerReceiver(minuteUpdater, filter);
    }

    private void setUserInfoToNavViewHeader() {
        String userString = sharedPreferences.getString("current_user_object", null);
        if (userString != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(userString, User.class);
            if (user != null) {
                user_name.setText(user.getName());
                user_email.setText(user.getEmail());
            }
        }
    }

    // Navigation Item Selected Method
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return true;
    }

    // toolbar menu setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        MenuItem location_search = menu.findItem(R.id.action_map_location_search);
        location_search.setVisible(false);

        //searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                if (selectedFragment != null) {
                    if (selectedFragment instanceof FragmentList) {
                        FragmentList fragmentList = (FragmentList) selectedFragment;
                        if (fragmentList.listViewAdapter != null) {
                            fragmentList.getFilter().filter(queryText);
                        }
                    } else if (selectedFragment instanceof FragmentTasks) {
                        FragmentTasks fragmentTasks = (FragmentTasks) selectedFragment;
                        if (fragmentTasks.taskViewAdapter != null) {
                            fragmentTasks.getFilter().filter(queryText);
                        }
                    }
                }
                return false;
            }
        });
        return true;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permissions not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(Main.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(Main.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // explanation of requested permissions

                // snack bar here with request explanation
                Snackbar.make(Main.this.findViewById(android.R.id.content),
                        "Please Grant Permission to upload profile photo",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                        },
                                        RequestCodes.STORAGE_PERMISSION_REQUEST_CODE);
                            }
                        }).show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        Main.this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        RequestCodes.STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestCodes.ALL_PERMISSION_REQUEST_CODE: {
                // When request is cancelled, the results array are empty
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // Permissions are granted
                    Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
                } else {
                    // Permissions are denied
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCodes.STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    uploadProfileImageToStorage(Main.CURRENT_USER_KEY);
                }
                return;
            }
        }
    }

    public void uploadProfileImageToStorage(final String current_user_key) {
        if (current_user_key != null) {
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(current_user_key).child("profile").child("current_profile.jpg");
            long ONE_GIGABYTE = 1024 * 1024 * 1024;
            reference.getBytes(ONE_GIGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = Converters.ByteArrayToBitmap(bytes);
                    try {
                        File rootFolder = new File(getApplicationContext().getFilesDir() + "/Planner");
                        if (!rootFolder.exists()) {
                            rootFolder.mkdir();
                        }

                        File profileImageFolder = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/Planner", "Planner Profile Photos");

                        if (!profileImageFolder.exists()) {
                            profileImageFolder.mkdir();
                        }

                        boolean fileDeleted = false;
                        File file = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/Planner/Planner Profile Photos/Current_Profile.jpeg");
                        if (file.exists()) {
                            fileDeleted = file.delete();
                        }

                        if (fileDeleted) {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                            setProfileImage();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // On Back Pressed Method
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startMinuteUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sharedPreferences.getBoolean("first_time_app_open", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_time_app_open", false);
            editor.apply();
        }
        //unregisterReceiver(minuteUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        //cancelHalfDayAlarm();
    }

    public void cancelQuarterAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), QuarterAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), QuarterAlarmReceiver.REQUEST_CODE, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    public void scheduleQuarterAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), QuarterAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), QuarterAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 1);
        start.set(Calendar.MINUTE, 00);
        start.set(Calendar.SECOND, 00);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60, pendingIntent);
    }

    public void scheduleHalfNotificationChecker() {
        Intent intent = new Intent(getApplicationContext(), QuarterChecker.class);
        intent.putExtra("DOC_KEY", CURRENT_USER_KEY);
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), QuarterChecker.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                900000, pIntent);
    }

}
