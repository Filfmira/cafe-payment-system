package com.example.filipedgb.cmovproj1.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.filipedgb.cmovproj1.R;
import com.example.filipedgb.cmovproj1.classes.Order;
import com.example.filipedgb.cmovproj1.classes.Voucher;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class HistoryFragment extends Fragment {


    private FirebaseApp app;
    private FirebaseAuth auth;

    public HistoryFragment() {
        // Required empty public constructor
    }


    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        app= FirebaseApp.getInstance();
        auth= FirebaseAuth.getInstance(app);


        View rootView = inflater.inflate(R.layout.fragment_history, container, false);



        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mPostReference = database.getReference("orders_by_user").child(auth.getCurrentUser().getUid());

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final LinearLayout l=(LinearLayout)  getView().findViewById(R.id.linearlayout_history);
                l.removeAllViews();

                for (final DataSnapshot child: dataSnapshot.getChildren()) {

                    DatabaseReference ordersUserRef = database.getReference("orders_by_user").child(child.getValue(String.class));
                    Log.e("dataref",ordersUserRef.getKey().toString());

                    DatabaseReference vaucherRef = database.getReference("orders");
                    LayoutInflater inflator = getActivity().getLayoutInflater();
                    final View ordersView = inflator.inflate(R.layout.content_order_history,null);

                    vaucherRef.child(ordersUserRef.getKey().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            final Order orderObj = snapshot.getValue(Order.class);
                            TextView price= (TextView) ordersView.findViewById(R.id.tv_price);
                            Double price_db=orderObj.getOrder_price();

                            if(orderObj.getListOfProducts()!=null)
                            {
                                for (final String key : orderObj.getListOfProducts().keySet())
                                {
                                    DatabaseReference productRef = database.getReference("products");
                                    productRef.child(key).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {

                                            LinearLayout linearlayoutproducts=(LinearLayout)ordersView.findViewById(R.id.linearlayoutproducts);

                                            TextView tv= new TextView(linearlayoutproducts.getContext());
                                            tv.setTextColor(Color.BLACK);
                                            tv.setText(orderObj.getListOfProducts().get(key)+"  "+snapshot.child("name").getValue());

                                            linearlayoutproducts.addView(tv);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }

                                    });
                                }
                            }






                            price.setText(round(price_db,1)+"€");
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                    l.addView(ordersView);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mPostReference.addListenerForSingleValueEvent(postListener);



        return rootView;
    }


}