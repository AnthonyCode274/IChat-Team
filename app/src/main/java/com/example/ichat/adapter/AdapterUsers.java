package com.example.ichat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichat.ChatActivity;
import com.example.ichat.R;
import com.example.ichat.ThereProfileActivity;
import com.example.ichat.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import customfonts.MyTextView_Arial;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<User> userList;
    private boolean isChat;

    //for getting current user's uid
    FirebaseAuth firebaseAuth;
    String myUid;

    //constructor
    public AdapterUsers(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
//        this.isChat = isChat;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //get data
        final String hisUID = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String userEmail = userList.get(i).getEmail();

        //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.a7)
                    .into(myHolder.mAvatarIv);
        } catch (Exception e) {

        }

        if (isChat) {
            if (userList.get(i).getOnlineStatus().equals("online")) {
                myHolder.imgOn.setVisibility(View.VISIBLE);
                myHolder.imgOff.setVisibility(View.GONE);
            } else {
                myHolder.imgOn.setVisibility(View.GONE);
                myHolder.imgOff.setVisibility(View.VISIBLE);
            }
        } else {
            myHolder.imgOn.setVisibility(View.GONE);
            myHolder.imgOff.setVisibility(View.GONE);
        }


        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "" + userEmail, Toast.LENGTH_SHORT).show();
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        }
                        if (which == 1) {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", hisUID);
                            context.startActivity(intent);

                        }
                    }
                });
                builder.create().show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        TextView mNameTv, mEmailTv;
        CircleImageView imgOn, imgOff, mAvatarIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
//            blockIv = itemView.findViewById(R.id.blockIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            imgOn = itemView.findViewById(R.id.img_on);
            imgOff = itemView.findViewById(R.id.img_off);
        }
    }
}
