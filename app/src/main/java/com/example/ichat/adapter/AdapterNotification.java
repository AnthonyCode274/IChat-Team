package com.example.ichat.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichat.PostDetailActivity;
import com.example.ichat.R;
import com.example.ichat.models.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {


    private Context context;
    private ArrayList<Notification> notificationsList;

    private FirebaseAuth firebaseAuth;

    public AdapterNotification(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate view row_notification

        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);

        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotification holder, int position) {
        //get and set data to views

        //get data
        final Notification model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        final String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        final String pId = model.getpId();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //we will get the name, email, image of the user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();
                            String email = "" + ds.child("email").getValue();
                            String username = "" + ds.child("username").getValue();

                            //add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to views
                            holder.nameTv.setText(username);

                            try {

                                if (image != null) {
                                    Picasso.get().load(image).placeholder(R.drawable.a7).into(holder.avatarIv);
                                }
                            } catch (Exception e) {

                                if (position % 3 == 0) {
                                    holder.avatarIv.setImageResource(R.drawable.a1);
                                } else if (position % 3 == 1) {
                                    holder.avatarIv.setImageResource(R.drawable.a2);
                                } else if (position % 3 == 2){
                                    holder.avatarIv.setImageResource(R.drawable.a3);
                                }else if (position % 2 == 0){
                                    holder.avatarIv.setImageResource(R.drawable.a4);
                                }
                                else if (position % 2 == 1){
                                    holder.avatarIv.setImageResource(R.drawable.a5);
                                }
                                holder.avatarIv.setImageResource(R.drawable.a6);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        //click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
        });

        //long press to show delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete notification

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //deleted
                                        Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    //holder class for views of row_notifications.xml
    class HolderNotification extends RecyclerView.ViewHolder {

        //declare views
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
