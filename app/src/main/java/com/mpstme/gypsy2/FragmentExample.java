package com.mpstme.gypsy2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpstme.gypsy2.handler.NetworkClass;
import com.mpstme.gypsy2.models.Hotel;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class FragmentExample extends Fragment {

    private static final String TAG = FragmentExample.class.getSimpleName();

    private RecyclerView recyclerView;
    private List<Hotel> hotelList;
    private StoreAdapter mAdapter;

    public FragmentExample() {
        // Required empty public constructor
    }

    public static FragmentExample newInstance(String param1, String param2) {
        FragmentExample fragment = new FragmentExample();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_example, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        hotelList = new ArrayList<>();
        mAdapter = new StoreAdapter(getActivity(), hotelList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        fetchStoreItems();

        Toolbar toolbar = view.findViewById(R.id.toolbar);


        return view;
    }

    private void fetchStoreItems() {
        final String URL = Keys.SERVER + "/mobile/allhotels";
        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getActivity(), "Couldn't fetch the delegate List! Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<Hotel> items = new Gson().fromJson(response.toString(), new TypeToken<List<Hotel>>() {
                        }.getType());

                        hotelList.clear();
                        hotelList.addAll(items);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        NetworkClass.getInstance().addToRequestQueue(request);
    }

    public interface OnFragmentInteractionListener {
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {
        private Context context;
        private List<Hotel> hotelList;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView name, address, cost_rating;
            public ImageView thumbnail;
            private ItemClickListener clickListener;
            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.hotel_name);
                address = view.findViewById(R.id.hotel_address);
                cost_rating = view.findViewById(R.id.hotel_cost);
                thumbnail = view.findViewById(R.id.thumbnail);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                clickListener.onClick(view, getPosition(), false);
            }

            public void setClickListener(ItemClickListener itemClickListener) {
                this.clickListener = itemClickListener;
            }
        }


        public StoreAdapter(Context context, List<Hotel> hotelList) {
            this.context = context;
            this.hotelList = hotelList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hotel_single, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Hotel hotel = hotelList.get(position);
            holder.name.setText(hotel.getName());
            holder.address.setText(hotel.getAddress());
            String cost_and_rating = "Cost : " + hotel.getCost() + " & Rating : " + hotel.getRating();
            holder.cost_rating.setText(cost_and_rating);
            String image_url = Keys.SERVER + hotel.getImage();

            Glide.with(context)
                    .load(image_url)
                    .into(holder.thumbnail);

            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    /*String name = ((TextView) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.delegate_name)).getText().toString();
                    String delegate_identifier = delegate.getIdentifier();
                    String process_url = Keys.SERVER + "user_details/" + delegate_identifier;

                    Intent i = new Intent(getActivity(), UserDetails.class);
                    i.putExtra("process_url", process_url);
                    startActivity(i);
                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                    Log.d("TAG", String.valueOf(delegateList.get(position)));*/

                    /*
                    * Here you can set on click listener
                    * So lets do one thing, create a new activity for hotel details, and send data to the activity from here
                    *
                    * */

                    String image_uri = Keys.SERVER + hotel.getImage();
                    String cost_rating = "Cost : " + hotel.getCost() + " & Rating :" + hotel.getRating();
                    String get_booking = hotel.getName();
                    Intent sendToSingle = new Intent(getActivity(), HotelSingleActivity.class);
                    sendToSingle.putExtra("hotel_name", String.valueOf(hotel.getName()));
                    sendToSingle.putExtra("hotel_address", String.valueOf(hotel.getAddress()));
                    sendToSingle.putExtra("hotel_image_url", image_uri);
                    sendToSingle.putExtra("hotel_rating", cost_rating);
                    sendToSingle.putExtra("hotel_booking", get_booking);
                    startActivity(sendToSingle);
                }
            });
        }

        @Override
        public int getItemCount() {
            return hotelList.size();
        }


    }
}