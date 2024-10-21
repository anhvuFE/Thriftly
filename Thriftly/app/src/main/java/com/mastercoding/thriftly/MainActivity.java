package com.mastercoding.thriftly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.mastercoding.thriftly.Authen.SignInActivity;
import com.mastercoding.thriftly.UI.HomeFragment;
import com.mastercoding.thriftly.UI.AddProductActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private HomeFragment homeFragment;

    private void bindingView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbar);
        homeFragment = new HomeFragment();
        fab = findViewById(R.id.fab);  // Tham chiếu FAB
    }

    private void bindingAction() {
        // Sự kiện khi nhấn vào FAB để chuyển sang AddProductActivity
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindingView();
        bindingAction();  // Thêm sự kiện vào FAB
        if (getIntent().hasExtra("showFragment")) {
            String fragmentToShow = getIntent().getStringExtra("showFragment");
            if ("homeFragment".equals(fragmentToShow)) {
                switchFragment(new HomeFragment());
            }
        }
        setSupportActionBar(toolbar);

        bottomNavigationView.setBackground(null);
        setupBottomNavigation();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.d("MainActivity", "No user is signed in");
        } else {
            Log.d("MainActivity", "User is signed in");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                switchFragment(homeFragment);
            } else if (id == R.id.menu_shorts) {
                Log.d("MainActivity", "menu_shorts");
                // Thêm fragment Shorts
            } else if (id == R.id.menu_subscriptions) {
                Log.d("MainActivity", "menu_subscriptions");
                // Thêm fragment Subscription
            } else if (id == R.id.menu_library) {
                logout();
                Log.d("MainActivity", "menu_library");
            }
            return true;
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}
