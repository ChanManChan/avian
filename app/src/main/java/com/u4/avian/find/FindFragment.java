package com.u4.avian.find;

import static com.u4.avian.common.NodeNames.NAME;
import static com.u4.avian.common.NodeNames.PHOTO;
import static com.u4.avian.common.NodeNames.USERS;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.u4.avian.R;

import java.util.ArrayList;
import java.util.List;

public class FindFragment extends Fragment {

    private RecyclerView rvFind;
    private FindAdapter findAdapter;
    private List<FindModel> findModelList;
    private TextView tvEmptyUserList;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private View progressBar;

    public FindFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFind = view.findViewById(R.id.rvFind);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyUserList = view.findViewById(R.id.tvEmptyFind);

        rvFind.setLayoutManager(new LinearLayoutManager(getActivity()));

        findModelList = new ArrayList<>();
        findAdapter = new FindAdapter(getActivity(), findModelList);
        rvFind.setAdapter(findAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild(NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findModelList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userId = ds.getKey();

                    if (userId != null && userId.equals(currentUser.getUid())) {
                        continue;
                    }

                    Object nameObject = ds.child(NAME).getValue();
                    Object photoObject = ds.child(PHOTO).getValue();

                    if (nameObject != null && photoObject != null) {
                        String fullName = nameObject.toString();
                        String photoName = photoObject.toString();
                        findModelList.add(new FindModel(fullName, photoName, userId, false));
                    }
                }

                if (findModelList.size() > 0) {
                    tvEmptyUserList.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
                findAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                tvEmptyUserList.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_users, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}