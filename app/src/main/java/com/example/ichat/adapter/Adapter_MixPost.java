package com.example.ichat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichat.AddPostActivity;
import com.example.ichat.PostDetailActivity;
import com.example.ichat.PostLikedByActivity;
import com.example.ichat.R;
import com.example.ichat.ThereProfileActivity;
import com.example.ichat.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Adapter_MixPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_HEADER = 0;
    private static final int ITEM_VIEW = 1;

    Context context;
    List<Post> postList;

    String myUid;
    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postsRef; //reference of posts
    boolean mProcessLike = false;

    public Adapter_MixPost(Context context, List<Post> postList) {
        this.context = context;
        postList = new ArrayList<>();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM_HEADER:
                View viewHeader = inflater.inflate(R.layout.item_post_header, parent, false);
                viewHolder = new HeaderProfileVH(viewHeader);
                break;
            case ITEM_VIEW:
                View viewItem = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingItemVH(viewItem);
                break;

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = postList.get(position);

        final String uid = post.getUid();
        final String pId = post.getpId();
        String uDp = post.getuDp();
        String username = post.getUsername();
        String gender = post.getGender();
        String birthday = post.getBirthday();
        final String pImage = post.getpImage();
        final String pDescription = post.getpDescr();

        switch (getItemViewType(position)) {

            case ITEM_HEADER:
                final HeaderProfileVH heroVh = (HeaderProfileVH) holder;

                heroVh.Username.setText(username);
                heroVh.gender.setText(gender);
                heroVh.birthday.setText(birthday);


                try {
                    Picasso.get().load(uDp).placeholder(R.drawable.a6).into(heroVh.tv_avatar_cover);
                } catch (Exception e) {
                    Log.e("TAG", "onBindViewHolder 1: " + e.getMessage());
                }

                break;

            case ITEM_VIEW:
                final LoadingItemVH loadItemView = (LoadingItemVH) holder;

                loadItemView.tvUsername.setText(post.getUsername());
                loadItemView.tvNumberLike.setText(post.getpLikes());
                loadItemView.tvNumberComment.setText(post.getpComments());

                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(Long.parseLong(post.getpTime()));
                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                loadItemView.tvTimePost.setText(pTime);

                setLikes(loadItemView, pId);
                try {
                    Picasso.get().load(uDp).placeholder(R.drawable.a6).into(loadItemView.imgAvatarSmall);
                } catch (Exception e) {
                    Log.e("TAG", "onBindViewHolder 1: " + e.getMessage());
                }


                if (pImage.equals("noImage")) {
                    loadItemView.imgPhotoUpload.setVisibility(View.GONE);
                } else {
                    loadItemView.imgPhotoUpload.setVisibility(View.VISIBLE);

                    try {
                        Picasso.get().load(pImage).into(loadItemView.imgPhotoUpload);
                    } catch (Exception e) {
                        Log.e("TAG", "onBindViewHolder 2: " + e.getMessage());
                    }
                }



                loadItemView.ll_like_post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //get total number of likes for the post, whose like button clicked
                        //if currently signed in user has not liked it before
                        //increase value by 1, otherwise decrease value by 1
                        final int pLikes = Integer.parseInt(post.getpLikes());
                        mProcessLike = true;
                        //get id of the post clicked
                        final String postIde = post.getpId();
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(postIde).hasChild(myUid)) {
                                        //already liked, so remove like
                                        postsRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                                        likesRef.child(postIde).child(myUid).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        // not liked, like it
                                        postsRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
                                        likesRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                                        mProcessLike = false;

                                        addToHisNotifications("" + uid, "" + pId, "Liked your post");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                loadItemView.imgComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //start PostDetailActivity
                        Intent intent = new Intent(context, PostDetailActivity.class);
                        intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                        context.startActivity(intent);
                    }
                });

                loadItemView.imgMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMoreOptions(loadItemView.imgMore, uid, myUid, pId, pImage);
                    }
                });


                loadItemView.imgShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*some posts contains only text, and some contains image and text so, we will handle them both*/
                        //get image from imageview
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) loadItemView.imgPhotoUpload.getDrawable();
                        if (bitmapDrawable == null) {
                            shareTextOnly(pDescription);
                        } else {
                            Bitmap bitmap = bitmapDrawable.getBitmap();
                            shareImageAndText(pDescription, bitmap);
                        }
                    }
                });


                loadItemView.profileLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*click to go to ThereProfileActivity with uid, this uid is of clicked user
                         * which will be used to show user specific data/posts*/
                        Intent intent = new Intent(context, ThereProfileActivity.class);
                        intent.putExtra("uid", uid);
                        context.startActivity(intent);
                    }
                });


                //click like count to start PostLikedByActiivty, and pass the post id
                loadItemView.tvNumberLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PostLikedByActivity.class);
                        intent.putExtra("postId", pId);
                        context.startActivity(intent);
                    }
                });


                break;

        }
    }


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {
                    //delete is clicked
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                } else if (id == 2) {
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();

    }


    private void setLikes(final LoadingItemVH holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this(SignedIn) user
                    Change drawable left icon of like button
                    Change text of like button from "Like" to "Liked"*/

                    holder.imgLike.setImageResource(R.drawable.ic_heart_liked);
//                    holder.likeBtn.setText("Liked");
                } else {
                    //user has not liked this post
                    /*To indicate that the post is not liked by this(SignedIn) user
                    Change drawable left icon of like button
                    Change text of like button from "Liked" to "Like"*/
                    holder.imgLike.setImageResource(R.drawable.ic_heart);
//                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        //timestamp for time and notification id
        String timestamp = "" + System.currentTimeMillis();

        //data to put in notification in firebase
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }


    private void shareTextOnly(String pDescription) {
        //concatenate title and description to share
        String shareBody = pDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //message to show in share dialog

    }


    private void shareImageAndText(String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = pDescription;

        //first we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.ichat.notifications.fileprovider",
                    file);
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }


    private void beginDelete(String pId, String pImage) {
        //post can be with or without image

        if (pImage.equals("noImage")) {
            //post is without image
            deleteWithoutImage(pId);
        } else {
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }


    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        /*Steps:
          1) Delete Image using url
          2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                                }
                                //deleted
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, can't go further
                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    private class HeaderProfileVH extends RecyclerView.ViewHolder {
        //views from row_post.xml
        ImageView bgCover, tv_avatar_cover;
        TextView birthday, Username, gender, tvShowDetails;

        public HeaderProfileVH(View viewItem) {
            super(viewItem);
            birthday = itemView.findViewById(R.id.tv_birthday_cover);
            Username = itemView.findViewById(R.id.tv_Username_cover);
            gender = itemView.findViewById(R.id.tv_gender_cover);
            bgCover = itemView.findViewById(R.id.img_cover_profile);
            tv_avatar_cover = itemView.findViewById(R.id.tv_avatar_cover);
            tvShowDetails = itemView.findViewById(R.id.tv_ShowDetails_cover);
        }
    }

    private class LoadingItemVH extends RecyclerView.ViewHolder {
        ImageView imgAvatarSmall;
        ImageView imgPhotoUpload;
        ImageView imgLike;
        ImageView imgComment;
        ImageView imgShare;
        ImageButton imgMore;
        TextView tvUsername, tvDescriptionPost, tvNumberLike, tvNumberComment, tvTimePost;
        LinearLayout profileLayout, ll_like_post;

        public LoadingItemVH(View itemLoading) {
            super(itemLoading);
            imgAvatarSmall = itemLoading.findViewById(R.id.uPictureIv);
            imgPhotoUpload = itemLoading.findViewById(R.id.pImageIv);
            imgLike = itemLoading.findViewById(R.id.likeBtn);
            imgComment = itemLoading.findViewById(R.id.commentBtn);
            imgShare = itemLoading.findViewById(R.id.shareBtn);
            imgMore = itemLoading.findViewById(R.id.moreBtn);
            tvUsername = itemLoading.findViewById(R.id.uNameTv);
            tvDescriptionPost = itemLoading.findViewById(R.id.pDescriptionTv);
            tvNumberLike = itemLoading.findViewById(R.id.pLikesTv);
            tvNumberComment = itemLoading.findViewById(R.id.pCommentsTv);
            tvTimePost = itemLoading.findViewById(R.id.pTimeTv);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            ll_like_post = itemView.findViewById(R.id.ll_like_post);
        }
    }
}
