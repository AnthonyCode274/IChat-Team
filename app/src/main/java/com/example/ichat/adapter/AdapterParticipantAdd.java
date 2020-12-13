package com.example.ichat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichat.R;
import com.example.ichat.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {
    private Context context;
    private ArrayList<User> userList;
    private String groupId, myGroupRole; //creator/admin/participant

    public AdapterParticipantAdd(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add, parent, false);

        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get data
        final User user = userList.get(position);
        String name = user.getName();
        String email = user.getEmail();
        String image = user.getImage();
        final String uid = user.getUid();

        //set data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.a5).into(holder.avatarIv);
        }
        catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.a5);
        }

        checkIfAlreadyExists(user, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Check if user already added or not
                 * If added: show remove-participant/make-admin/remove-admin option (Admin will not able to change role of creator)
                 * If not added, show add participant option*/
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    //user exists/participant
                                    String hisPreviousRole = ""+dataSnapshot.child("role").getValue();

                                    //options to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            //im creator, he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which==0){
                                                        //Remove Admin clicked
                                                        removeAdmin(user);
                                                    }
                                                    else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();

                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            //im creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which==0){
                                                        //Make Admin clicked
                                                        makeAdmin(user);
                                                    }
                                                    else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            //im admin, he is creator
                                            Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hisPreviousRole.equals("admin")){
                                            //im admin, he is admin too
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which==0){
                                                        //Remove Admin clicked
                                                        removeAdmin(user);
                                                    }
                                                    else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            //im admin, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which==0){
                                                        //Make Admin clicked
                                                        makeAdmin(user);
                                                    }
                                                    else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else {
                                    //user doesn't exists/not-participant: add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addParticipant(user);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void addParticipant(User user) {
        //setup user data - add user in group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding user in group
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(User user) {
        //setup data - change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin"); //roles are: participant/admin/creator
        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dailed making admin
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(User user) {
        //remove participant from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //removed successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed removing participant
                    }
                });
    }

    private void removeAdmin(User user) {
        //setup data - remove admin - just change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant"); //roles are: participant/admin/creator
        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "The user is no longer admin...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dailed making admin
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(User user, final HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //already exists
                            String hisRole = ""+dataSnapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                        else {
                            //doesn't exists
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv, emailTv, statusTv;

        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
