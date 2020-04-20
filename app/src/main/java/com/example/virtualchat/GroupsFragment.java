package com.example.virtualchat;


import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private ListView listView;
    private View groupFragmentview;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> grouplist=new ArrayList<>();
    private DatabaseReference groupref;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        groupFragmentview =inflater.inflate(R.layout.fragment_groups, container, false);
        groupref= FirebaseDatabase.getInstance().getReference().child("Group");

        initializefields();
        RetrieveAndDisplayGroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentgroupname=parent.getItemAtPosition(position).toString();

                Intent groupchatintent= new Intent(getContext(),GroupchatActivity.class);
                groupchatintent.putExtra("groupname",currentgroupname);
                startActivity(groupchatintent);
            }
        });

        return groupFragmentview;
    }



    private void initializefields() {
        listView=(ListView) groupFragmentview.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,grouplist);
        listView.setAdapter(arrayAdapter);
    }
    private void RetrieveAndDisplayGroups() {
        groupref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new  HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();

                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                grouplist.clear();
                grouplist.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
