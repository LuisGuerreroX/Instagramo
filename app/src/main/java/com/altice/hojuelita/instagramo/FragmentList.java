package com.altice.hojuelita.instagramo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class FragmentList extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

 //   private Map<String,Object> documents = null;
 //   private Noticia[] documents = new Noticia[10];

    private List<Noticia> documents = new ArrayList<>();
 //   private DocumentSnapshot[] documents;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView;

        rootView =  inflater.inflate(R.layout.fragment_list, container, false);

        final RecyclerView recyclerView = rootView.findViewById(R.id.my_recycler_view);

        db.collection("Noticias").get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        documents.add(document.toObject(Noticia.class));
                    }

                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(new MyRecyclerAdapter(getActivity(), documents));
                }
            }
        });

        return rootView;
    }


}