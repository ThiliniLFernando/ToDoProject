package com.smart.planner;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smart.planner.Adapters.ListViewAdapter;
import com.smart.planner.POJOs.List;
import com.smart.planner.LocalDB.SQLiteHelper;

import java.util.ArrayList;

public class FragmentList extends Fragment implements Filterable {

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private FirebaseFirestore firestore;
    private Query firestoreQuery;
    ListenerRegistration listenerRegistration;

    protected ListViewAdapter listViewAdapter ;
    protected ArrayList<List> listData;
    private ArrayList<List> listsDataFullCopy ;

    private RecyclerView listView;
    private StaggeredGridLayoutManager recycleViewLayoutManager;

    private FloatingActionButton listFab;
    private DialogNewList dialogNewList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(final View view) {
        sqLiteHelper = new SQLiteHelper(getContext());
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        // initialize fire store
        firestore = FirebaseFirestore.getInstance();

        listData = new ArrayList<>();
        listsDataFullCopy = new ArrayList<>(listData);

        recycleViewLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recycleViewLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        listView = view.findViewById(R.id.list_recycleView);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(recycleViewLayoutManager);
        listViewAdapter = new ListViewAdapter(listData,FragmentList.this);
        listView.setAdapter(listViewAdapter);
        retrieveListData();

        // Floating Action Button // Add New List Dialog
        listFab = view.findViewById(R.id.fab_add_list);
        listFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogNewList == null) {
                    dialogNewList = new DialogNewList();
                    dialogNewList.setTargetFragment(FragmentList.this, 120);
                }
                dialogNewList.show(getParentFragmentManager(), "fromFragmentList");
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listenerRegistration.remove();
    }

    public void removeLists(List[] lists){
        if (Main.CURRENT_USER_KEY != null){
        for (List list : lists){
                firestore.collection("Users").document(Main.CURRENT_USER_KEY)
                        .collection("Lists").document(list.getDocumentId())
                        .delete();
            }
        }
    }

    // retrieve List Data
    public ArrayList<List> retrieveListData() {
        listData.clear();
        if (Main.CURRENT_USER_KEY != null) {
            //
            firestoreQuery = firestore.collection("Users").document(Main.CURRENT_USER_KEY).collection("Lists");
            listenerRegistration =firestoreQuery.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshots,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {

                    if (e != null){
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange change : querySnapshots.getDocumentChanges()) {
                        List l = change.getDocument().toObject(List.class);
                        switch (change.getType()){
                            case ADDED:
                                l.setDocumentId(change.getDocument().getId());
                                listData.add(l);
                                listsDataFullCopy.add(l);
                                break;
                            case REMOVED:
                                updateList(querySnapshots);
                                break;
                            case MODIFIED:
                                updateList(querySnapshots);
                                break;
                        }
                        listViewAdapter.notifyDataSetChanged();
                    }
                }

                private void updateList(QuerySnapshot querySnapshots) {
                    listData.clear();
                    listsDataFullCopy.clear();
                    for (DocumentSnapshot snapshot : querySnapshots){
                        List list = snapshot.toObject(List.class);
                        list.setDocumentId(snapshot.getId());
                        listData.add(list);
                        listsDataFullCopy.add(list);
                    }
                }
            });

        } else {
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM list", null);
            while (cursor.moveToNext()) {
                List list = new List(
                        cursor.getString(cursor.getColumnIndex("listName")),
                        cursor.getString(cursor.getColumnIndex("listColor"))
                );
                listData.add(list);
            }
        }
        return listData ;
    }

    // List Data Filtering
    private Filter listFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<List> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length()==0){
                filteredList.addAll(listsDataFullCopy);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (List listItem : listsDataFullCopy){
                    if (listItem.getListName().toLowerCase().startsWith(filterPattern)){
                        filteredList.add(listItem);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList ;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listData.clear();
            listData.addAll((ArrayList)results.values);
            listViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return listFilter;
    }

    // List Data Filtering
}
