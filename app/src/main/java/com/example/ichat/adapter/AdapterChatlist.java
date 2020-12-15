package com.example.ichat.adapter;

import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.ChatActivity;
import com.example.ichat.models.Chat;
import com.example.ichat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.ichat.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.ChatHolder> {
    private Context mContext;
    private List<User> users;
    private boolean isChat;

    String theLastMessage;
    Long theTimeMessage;

    public AdapterChatlist(Context mContext, List<User> users, boolean isChat) {
        this.mContext = mContext;
        this.users = users;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_chatlist, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        final User user = users.get(position);

        //title
        holder.tvItemChat.setText(user.getName());

        //image
        if (user.getImage().equals("default")) {
            holder.imgItemChat.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImage()).into(holder.imgItemChat);
        }

        //last message

        if (isChat) {
            lastMessage(user.getUid(), holder.tvItemMess, holder.tvItemTime);
        } else {
            holder.tvItemMess.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getOnlineStatus().equals("online")) {
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }

        holder.itemChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("hisUid", user.getUid());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgItemChat;
        private TextView tvItemChat;
        private TextView tvItemMess;
        private TextView tvItemTime;
        private CircleImageView imgOn;
        private CircleImageView imgOff;
        private RelativeLayout itemChats;


        public ChatHolder(View itemView) {
            super(itemView);

            imgItemChat = (CircleImageView) itemView.findViewById(R.id.imgItemChat);
            tvItemChat = (TextView) itemView.findViewById(R.id.tvItemChat);
            tvItemMess = (TextView) itemView.findViewById(R.id.tvItemMess);
            tvItemTime = (TextView) itemView.findViewById(R.id.tvItemTime);
            imgOn = (CircleImageView) itemView.findViewById(R.id.img_on);
            imgOff = (CircleImageView) itemView.findViewById(R.id.img_off);
            itemChats = (RelativeLayout) itemView.findViewById(R.id.itemChats);
        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final TextView lastTime) {
        //time
        final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");


        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                            theTimeMessage = chat.getTime();
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
//                        lastTime.setText(sdf.format(theTimeMessage));
                        lastTime.setText(covertTimeToText(String.valueOf(theTimeMessage)));

                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public String covertTimeToText(String dataDate) {

        String convTime = null;

        String prefix = "";
        String suffix = "trước";

        try {

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(dataDate));
            Date d = (Date) c.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(d);
            Date pasTime = format.parse(time);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + " giây " + suffix;
            } else if (minute < 60) {
                convTime = minute + " phút "+suffix;
            } else if (hour < 24) {
                convTime = hour + " giờ "+suffix;
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " năm " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " tháng " + suffix;
                } else {
                    convTime = (day / 7) + " tuần " + suffix;
                }
            } else if (day < 7) {
                convTime = day+" ngày "+suffix;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;
    }

}
