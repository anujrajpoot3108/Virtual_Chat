package com.example.virtualchat;



import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class message_adapter extends RecyclerView.Adapter<message_adapter.messageviewholder> {

    private List<messages> usermessagelist;
    private FirebaseAuth mauth;
    private DatabaseReference userref;

    public message_adapter (List<messages> usermessagelist){

        this.usermessagelist=usermessagelist;
    }
    public   class messageviewholder extends RecyclerView.ViewHolder{

        public TextView sendermessagetext,receivermessagetext,sendertime,receivertime;
        public CircleImageView receiverimage;
        public ImageView messagesenderpicture,messagereceiverpicture;

        public messageviewholder(@NonNull View itemView) {
            super(itemView);
            sendermessagetext=(TextView) itemView.findViewById(R.id.sender_message);
            receivermessagetext=(TextView) itemView.findViewById(R.id.receiver_message);
            receiverimage=(CircleImageView) itemView.findViewById(R.id.message_profile_receiver) ;
            messagereceiverpicture=(ImageView)itemView.findViewById(R.id.receiver_message_image_view);
            messagesenderpicture=(ImageView)itemView.findViewById(R.id.sender_message_image_view);
            sendertime=(TextView)itemView.findViewById(R.id.sender_message_time);
            receivertime=(TextView)itemView.findViewById(R.id.receiver_message_time);


        }
    }

    @NonNull
    @Override
    public messageviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);


        mauth=FirebaseAuth.getInstance();
        return new messageviewholder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final messageviewholder holder, final int position) {

        String messagesenderid=mauth.getCurrentUser().getUid();
        final messages message=usermessagelist.get(position);
        String fromuserid=message.getFrom();
        String frommessagetype=message.getType();

        userref=FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image")){

                    String receiverprofileimage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverprofileimage).placeholder(R.drawable.profile_image).into(holder.receiverimage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receivermessagetext.setVisibility(View.GONE);
        holder.receiverimage.setVisibility(View.GONE);
        holder.sendermessagetext.setVisibility(View.GONE);
        holder.messagereceiverpicture.setVisibility(View.GONE);
        holder.messagesenderpicture.setVisibility(View.GONE);
        holder.sendertime.setVisibility(View.GONE);
        holder.receivertime.setVisibility(View.GONE);


        if("text".equals(frommessagetype)){


            if(fromuserid.equals(messagesenderid)){
                holder.sendermessagetext.setVisibility(View.VISIBLE);
                holder.sendertime.setVisibility(View.VISIBLE);
                holder.sendermessagetext.setBackgroundResource(R.drawable.sender_message_text);
                holder.sendermessagetext.setTextColor(Color.BLACK);
                holder.sendermessagetext.setText(message.getMessage());
                holder.sendertime.setText(message.getTime());
            }
            else{

                holder.receivermessagetext.setVisibility(View.VISIBLE);
                holder.receiverimage.setVisibility(View.VISIBLE);
                holder.receivertime.setVisibility(View.VISIBLE);
                holder.sendermessagetext.setVisibility(View.INVISIBLE);
                holder.sendertime.setVisibility(View.INVISIBLE);

                holder.receivermessagetext.setBackgroundResource(R.drawable.receiver_message_text);
                holder.receivermessagetext.setTextColor(Color.BLACK);
                holder.receivermessagetext.setText(message.getMessage());
                holder.receivertime.setText(message.getTime());
            }

        }
        else if("image".equals(frommessagetype)) {

            if(fromuserid.equals(messagesenderid)){
                holder.messagesenderpicture.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.messagesenderpicture);
        }
            else{
                holder.messagereceiverpicture.setVisibility(View.VISIBLE);
                holder.receiverimage.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(holder.messagereceiverpicture);
            }

        }
        else if("pdf".equals(frommessagetype)|| "docx".equals(frommessagetype)){
            if(fromuserid.equals(messagesenderid)){

                holder.messagesenderpicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/virtual-chat-939f5.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=63ca6819-4ed2-4165-bfd4-f0c713d7f889").into(holder.messagesenderpicture);



            }
            else{

                holder.messagereceiverpicture.setVisibility(View.VISIBLE);
                holder.receiverimage.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/virtual-chat-939f5.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=63ca6819-4ed2-4165-bfd4-f0c713d7f889").into(holder.messagereceiverpicture);


            }


        }

        if(fromuserid.equals(messagesenderid)){

           holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   if(usermessagelist.get(position).getType().equals("pdf")||usermessagelist.get(position).getType().equals("docx")){
                       CharSequence[] options=new CharSequence[]{
                               "Delete For ME",
                               "View this Document",
                               "Cancel",
                               "Delete For Everyone"
                       };

                       AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete message");
                       builder.setItems(options, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (which == 0) {

                                    deletesentmessages(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);

                               } else if (which == 1) {

                                   Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(usermessagelist.get(position).getMessage()));
                                   holder.itemView.getContext().startActivity(intent);

                               } else if (which == 3) {

                                    deletemessageforeveryone(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                           }
                       });
                       builder.show();

                   }
                   else if(usermessagelist.get(position).getType().equals("text")){
                       CharSequence[] options=new CharSequence[]{
                               "Delete For ME",
                               "Cancel",
                               "Delete For Everyone"
                       };

                       AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete message");
                       builder.setItems(options, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (which == 0) {

                                    deletesentmessages(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);

                               }
                               else if (which == 2) {

                                    deletemessageforeveryone(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                           }
                       });
                       builder.show();

                   }
                   else if(usermessagelist.get(position).getType().equals("image")){
                       CharSequence[] options=new CharSequence[]{
                               "Delete For ME",
                               "View this Image",
                               "Cancel",
                               "Delete For Everyone"
                       };

                       AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete message");
                       builder.setItems(options, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (which == 0) {

                                    deletesentmessages(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);

                               } else if (which == 1) {

                                   Intent intent=new Intent(holder.itemView.getContext(),imageviewerActivity.class);
                                   intent.putExtra("url",usermessagelist.get(position).getMessage());
                                   holder.itemView.getContext().startActivity(intent);

                               } else if (which == 3) {

                                    deletemessageforeveryone(position,holder);
                                   Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                           }
                       });
                       builder.show();

                   }
                   return  true;
               }
           });

        }
        else{

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(usermessagelist.get(position).getType().equals("pdf")||usermessagelist.get(position).getType().equals("docx")){
                        CharSequence[] options=new CharSequence[]{
                                "Delete For ME",
                                "View this Document",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    deletereceivedmessages(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 1) {

                                    Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(usermessagelist.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if(usermessagelist.get(position).getType().equals("text")){
                        CharSequence[] options=new CharSequence[]{
                                "Delete For ME",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    deletereceivedmessages(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if(usermessagelist.get(position).getType().equals("image")){
                        CharSequence[] options=new CharSequence[]{
                                "Delete For ME",
                                "View this Image",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    deletereceivedmessages(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 1) {

                                    Intent intent=new Intent(holder.itemView.getContext(),imageviewerActivity.class);
                                    intent.putExtra("url",usermessagelist.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    return  true;
                }
            });

        }


    }



    @Override
    public int getItemCount() {
        return usermessagelist.size();
    }


    private void deletesentmessages(final int position,final messageviewholder holder){


        DatabaseReference rootref=FirebaseDatabase.getInstance().getReference();

        rootref.child("message").child(usermessagelist.get(position).getFrom())
                .child(usermessagelist.get(position).getTo())
                .child(usermessagelist.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(holder.itemView.getContext(),"message deleted successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occured",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    private void deletereceivedmessages(final int position,final messageviewholder holder){


        DatabaseReference rootref=FirebaseDatabase.getInstance().getReference();

        rootref.child("message").child(usermessagelist.get(position).getTo())
                .child(usermessagelist.get(position).getFrom())
                .child(usermessagelist.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(holder.itemView.getContext(),"message deleted successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occured",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void deletemessageforeveryone(final int position,final messageviewholder holder){


        final DatabaseReference rootref=FirebaseDatabase.getInstance().getReference();

        rootref.child("message").child(usermessagelist.get(position).getFrom())
                .child(usermessagelist.get(position).getTo())
                .child(usermessagelist.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){


                    rootref.child("message").child(usermessagelist.get(position).getTo())
                            .child(usermessagelist.get(position).getFrom())
                            .child(usermessagelist.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(),"message deleted successfully",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"error occured",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}
