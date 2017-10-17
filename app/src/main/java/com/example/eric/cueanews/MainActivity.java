package com.example.eric.cueanews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.eric.cueanews.PostDetailActivity.POST_TRANSFER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GetCategoryJsonData.OnCategoryDataAvailable, GetPostsJsonData.OnPostDataAvailable,
        RecyclerItemClickListenter.OnRecyclerItemClickListenter,
        GetUserJsonData.OnUserDataAvailable {

    private static final String TAG = "MainActivity";

    //UI references
    private TabLayout mTabLayout;
    private View mProgressView;
    private RecyclerView mRecyclerView;
    private PostsRecyclerViewAdapter mPostsRecyclerViewAdapter;
    private TextView mUserFullname;
    private TextView mUserEmail;
    private ImageView mUserAvatar;

    //variables
    private String mCategory;
    private int mAuthor = 0;
    private List<Category> mCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_main);

        setSupportActionBar(toolbar);

        //set recycler view layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set recycler view item touch listener
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListenter(this, mRecyclerView, this));

        //init posts recycler view adapter
        mPostsRecyclerViewAdapter = new PostsRecyclerViewAdapter(this, new ArrayList<Post>());

        //set recycler view adapter
        mRecyclerView.setAdapter(mPostsRecyclerViewAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //setup nav header view ui
        View navHeaderView = navigationView.getHeaderView(0);
        mUserFullname = (TextView) navHeaderView.findViewById(R.id.user_fullname);
        mUserEmail = (TextView) navHeaderView.findViewById(R.id.user_email);
        mUserAvatar = (ImageView) navHeaderView.findViewById(R.id.user_avatar);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            /**
             * Called when a tab enters the selected state.
             *
             * @param tab The tab that was selected
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: " + tab.getText().toString() + " --> " + tab.getPosition());

                if (tab.getPosition() > 0) {
                    mCategory = mCategories.get(tab.getPosition() - 1).getSlug();
                } else {
                    mCategory = null;
                }

                categorySearch();
            }

            /**
             * Called when a tab exits the selected state.
             *
             * @param tab The tab that was unselected
             */
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            /**
             * Called when a tab that is already selected is chosen again by the user. Some applications
             * may use this action to return to the top level of a category.
             *
             * @param tab The tab that was reselected.
             */
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mProgressView = findViewById(R.id.posts_progress);

        Log.d(TAG, "onCreate: ends");
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    void setupUserDetails() {

        SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.app_name), 0);

        String storedName = preferences.getString("name", null);
        String storedUsername = preferences.getString("username", null);
        String storedAvatar = preferences.getString("avatar", null);
        String storedEmail = preferences.getString("email", null);
        String storedToken = preferences.getString("access_token", null);

        if ((storedName != null) && (storedUsername != null) &&
                (storedAvatar != null) && (storedEmail != null) && (storedToken != null)) {
            mUserFullname.setText(storedName + " (" + storedUsername + ")");

        } else {
            fetchUserDetails(storedToken);
        }

        if (mUserEmail != null) {
            mUserEmail.setText(storedEmail);
        }

    }

    void resetUserDetails(String data) {

        JSONObject jsonData = null;

        int id = -1;
        String name = null;
        String avatar = null;
        String first_name = null;
        String last_name = null;
        String username = null;
        String phone = null;
        String country = null;
        int is_verified = 0;
        String created_at = null;
        String updated_at = null;

        try {
            jsonData = new JSONObject(data);
            id = jsonData.has("id") ? jsonData.getInt("id") : -1;
            name = jsonData.has("name") ? jsonData.getString("name") : "";
            first_name = jsonData.has("first_name") ? jsonData.getString("first_name") : "";
            last_name = jsonData.has("last_name") ? jsonData.getString("last_name") : "";
            username = jsonData.has("username") ? jsonData.getString("username") : "";
            phone = jsonData.has("phone") ? jsonData.getString("phone") : "";
            country = jsonData.has("country") ? jsonData.getString("country") : "";
            is_verified = jsonData.has("is_verified") ? jsonData.getInt("is_verified") : 0;
            avatar = jsonData.has("avatar") ? jsonData.getString("avatar") : "";
        } catch (JSONException jsone) {
            jsone.printStackTrace();

            Log.e(TAG, "resetUserDetails: Error processing Json data " + jsone.getMessage());
        }

        SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.app_name), 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("id", id);
        editor.putString("name", name);
        editor.putString("first_name", first_name);
        editor.putString("last_name", last_name);
        editor.putString("username", username);
        editor.putString("avatar", avatar);
        editor.putString("phone", phone);
        editor.putString("country", country);
        editor.putInt("verified", is_verified);
        editor.putString("created_at", created_at);
        editor.putString("updated_at", updated_at);
        editor.commit();

        if ((name != null) && (username != null)) {
            mUserFullname.setText(name + " (" + username + ")");

//            //set post thumbnail
//            Picasso.with(this).load(avatar)
//                    .error(R.drawable.avatar)
//                    .placeholder(R.drawable.avatar)
//                    .into(mUserAvatar);

        }
    }

    void fetchUserDetails(String accessToken) {
        Log.d(TAG, "fetchUserDetails: starts");

        GetUserJsonData getUserJsonData = new GetUserJsonData(this, "http://cueanews.jangobid.com/api/user", accessToken);
        getUserJsonData.execute((String[]) null);

        Log.d(TAG, "fetchUserDetails: ends");
    }

    private void categorySearch() {
        Log.d(TAG, "categorySearch: starts");

        GetPostsJsonData getPostsJsonData = new GetPostsJsonData(this, "http://cueanews.jangobid.com/api/posts", mCategory, mAuthor);
        getPostsJsonData.executeOnSameThread(null);

        Log.d(TAG, "categorySearch: ends");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume starts");

        super.onResume();

        showProgress(true);

        setupUserDetails();

        GetCategoryJsonData getCategoryJsonData = new GetCategoryJsonData(this, "http://cueanews.jangobid.com/api/categories");
        getCategoryJsonData.execute((String[]) null);

        GetPostsJsonData getPostsJsonData = new GetPostsJsonData(this, "http://cueanews.jangobid.com/api/posts", mCategory, mAuthor);
        getPostsJsonData.executeOnSameThread(null);

        Log.d(TAG, "onResume ends");
    }

    private void tabAdapter(TabLayout tabLayout, List<Category> data) {

        try {
            if (data.isEmpty()) {
                tabLayout.setVisibility(View.GONE);
            } else {

                tabLayout.removeAllTabs();

                tabLayout.addTab(tabLayout.newTab().setText("Latest"));

                for (int i = 0; i < data.size(); i++) {
                    mTabLayout.addTab(mTabLayout.newTab().setText(data.get(i).getName()));
                }

                mTabLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "tabAdapter: has error " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            showProgress(true);

            //redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.app_name), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("access_token", null);
            editor.commit();

            finish();
        } else if (id == R.id.action_refresh) {
            Log.d(TAG, "onOptionsItemSelected: start of refresh action.");

            showProgress(true);

            setupUserDetails();

            GetCategoryJsonData getCategoryJsonData = new GetCategoryJsonData(this, "http://cueanews.jangobid.com/api/categories");
            getCategoryJsonData.execute((String[]) null);

            GetPostsJsonData getPostsJsonData = new GetPostsJsonData(this, "http://cueanews.jangobid.com/api/posts", mCategory, mAuthor);
            getPostsJsonData.executeOnSameThread(null);

            Log.d(TAG, "onOptionsItemSelected: end of refresh action.");
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            // Handle the news action
            // Intent code for open new activity through intent.
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            //fragment  = new NewsListFragment();
        } else if (id == R.id.nav_about) {
            // Handle the about action
            // Intent code for open new activity through intent.
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        }else if (id == R.id.nav_faqs) {
            // Handle the about action
            // Intent code for open new activity through intent.
            startActivity(new Intent(getApplicationContext(), FaqActivity.class));
        } else if (id == R.id.nav_fb) {
            // Handle the fb action

            Intent fb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com"));
            startActivity(fb);
        } else if (id == R.id.nav_web) {
            // Handle the web action

            Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.cuea.edu"));
            startActivity(web);
        } else if (id == R.id.nav_share) {
            // Handle the share action
            try {
                //String phn = "null";
                String msg = "Hello! Check out Cuea411 App for your smartphone. " +
                        "Download it today from https://cueanews.jangobid.com " +
                        "Its the new thing around campus.";

                Intent msgintent = new Intent(Intent.ACTION_VIEW);
                // Invokes only SMS/MMS clients
                msgintent.setType("vnd.android-dir/mms-sms");
                // Specify the Phone Number
                // msgintent.putExtra("address", phn);
                // Specify the Message
                msgintent.putExtra("sms_body", msg);

                startActivity(msgintent);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "SMS failed, please try again later!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else if (id == R.id.nav_twitter)
        {
            // Handle the twitter action
            Intent twitt = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitter.com"));
            startActivity(twitt);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCategoryDataAvailable(List<Category> data, DownloadStatus status, int responseCode) {

        if (status == DownloadStatus.OK) {

            if ((mCategories != null) && (mCategories.size() > 0) && (data != null) && ((mCategories.size() - 1) < data.size())) {
                mCategories.clear();
            }

            mCategories = data;

            tabAdapter(mTabLayout, data);

        } else {
            tabAdapter(mTabLayout, mCategories);
            Log.e(TAG, "onDataAvailable failed with status " + status);
        }

    }

    @Override
    public void onPostDataAvailable(List<Post> data, DownloadStatus status) {
        Log.d(TAG, "onPostDataAvailable: starts" + data);

        if (status == DownloadStatus.OK) {
            showProgress(false);

            mPostsRecyclerViewAdapter.loadNewData(data);
        } else {
            Log.e(TAG, "onDataAvailable failed with status " + status);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: starts");

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(POST_TRANSFER, mPostsRecyclerViewAdapter.getPost(position));
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.d(TAG, "onItemLongClick: starts");

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(POST_TRANSFER, mPostsRecyclerViewAdapter.getPost(position));
        startActivity(intent);
    }

    @Override
    public void onUserDataAvailable(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onUserDataAvailable: starts");

        if (status == DownloadStatus.OK) {
            resetUserDetails(data);
        } else {
            Log.d(TAG, "onUserDataAvailable: Failed with status " + status + " and response code " + responseCode);
        }
    }
}
