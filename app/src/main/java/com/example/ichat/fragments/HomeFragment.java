package com.example.ichat.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ichat.AddPostActivity;
import com.example.ichat.DashboardActivity;
import com.example.ichat.FragmentIntent.AddpostFragment;
import com.example.ichat.HauNguyen.Login.LoginActivity;
import com.example.ichat.R;
import com.example.ichat.SettingsActivity;
import com.example.ichat.adapter.AdapterPosts;
import com.example.ichat.models.Post;
import com.example.ichat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";
    private static final int PAGE_START = 1;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static final int TOTAL_PAGES = 5;
    TextView clickPhoto;
    CircleImageView ivAvatar;
    RelativeLayout rlBell, rlt_yourThink;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager linearLayoutManager;
    LinearLayout rlt_error_loadingHome, error_layout;
    ProgressBar progressBar;
    Button btnRetry;
    TextView txtError;
    //firebase auth
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    List<User> users;
    RecyclerView recyclerView;
    List<Post> postList;
    AdapterPosts adapterPosts;
    int position;
    String hisDp;
    String image;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_2, container, false);
        innitView(view);
        rlt_error_loadingHome.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
//        getImage();
        //recycler view and its properties
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        postList = new ArrayList<>();
        getImage();
        loadPosts();

        rlBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Notifications fragment transaction
                NotificationsFragment fragment5 = new NotificationsFragment();
                FragmentTransaction ft5 = getActivity().getSupportFragmentManager().beginTransaction();
                ft5.replace(R.id.content, fragment5, TAG);
                ft5.commit();
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPosts();
            }
        });

        rlt_yourThink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddPostActivity.class);
                startActivity(i);
            }
        });

        return view;
    }

    private void getImage() {
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    image = "" + ds.child("image").getValue();
                    try {
                        Picasso.get().load(image).into(ivAvatar);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_user_default).into(ivAvatar);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void loadPosts() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    postList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        hisDp = "" + ds.child("uDp").getValue();

                        try {
                            Picasso.get().load(hisDp).placeholder(R.drawable.photo).into(ivAvatar);
                        } catch (Exception e) {
                            //Picasso.get().load(R.drawable.photo).into(ivAvatar);
                            Log.e(TAG, "onDataChange: " + e.getMessage());

                        }
                        Post post = ds.getValue(Post.class);
                        if (post != null){
                            postList.add(post);
                            adapterPosts = new AdapterPosts(getContext(), postList);
                            recyclerView.setAdapter(adapterPosts);
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //in case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Log.e(TAG, "loadPosts: " + e.getMessage());
            progressBar.setVisibility(View.INVISIBLE);
            rlt_error_loadingHome.setVisibility(View.VISIBLE);
        }

    }

    private void searchPosts(final String searchQuery) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);


                    if (post.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            post.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(post);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void checkUserStatus() {
        try {
            //get current user
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {

            } else {
                //user not signed in, go to main acitivity
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        }catch (Exception e){
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "User is not register!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AddpostFragment fm = new AddpostFragment();

        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_add_post) {
//            startFragment();
        } else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void startFragment() {
        AddpostFragment addpostFragment = new AddpostFragment();
        getFragmentManager().beginTransaction().replace(R.id.content, addpostFragment).commit();
    }

    private void innitView(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        rlBell = view.findViewById(R.id.rlBell);
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        clickPhoto = (TextView) view.findViewById(R.id.tvContent);
        progressBar = view.findViewById(R.id.main_progress_home);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);
        rlt_yourThink = view.findViewById(R.id.rlt_yourThink);
        error_layout = view.findViewById(R.id.error_layout);
        rlt_error_loadingHome = view.findViewById(R.id.rlt_error_loadingHome);
    }

}
