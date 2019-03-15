package com.mpstme.gypsy2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpstme.gypsy2.handler.NetworkClass;
import com.mpstme.gypsy2.models.Booking;
import com.mpstme.gypsy2.models.Hotel;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {

    private static final String TAG = BookingFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private List<Booking> hotelList;
    private StoreAdapter mAdapter;

    SharedPreferences sharedPreferences;
    String user_email;

    public BookingFragment() {
        // Required empty public constructor
    }

    public static BookingFragment newInstance(String param1, String param2) {
        BookingFragment fragment = new BookingFragment();
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
        View view = inflater.inflate(R.layout.fragment_booking, container, false);


        sharedPreferences = getContext().getSharedPreferences(Keys.PREFERENCES, Context.MODE_PRIVATE);
        user_email = sharedPreferences.getString("email", "null");

        recyclerView = view.findViewById(R.id.recycler_view_bookings);
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
        final String URL = Keys.SERVER + "/user_bookings/" + user_email;
        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getActivity(), "Couldn't fetch the delegate List! Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<Booking> items = new Gson().fromJson(response.toString(), new TypeToken<List<Booking>>() {
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
        private List<Booking> hotelList;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView name;
            private ItemClickListener clickListener;
            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.hotel_booking_id);

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


        public StoreAdapter(Context context, List<Booking> hotelList) {
            this.context = context;
            this.hotelList = hotelList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.booking_single, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Booking hotel = hotelList.get(position);
            holder.name.setText(hotel.getHotel());


            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(final View view, int position, boolean isLongClick) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("Cancel Booking?");
                    builder.setMessage("Are you sure?");

                    final String process_url = Keys.SERVER + "/cancel_booking/" + user_email + "&" + hotel.getHotel();

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(final DialogInterface dialog, int which) {
                            // Do nothing but close the dialog

                            new HttpRequestTask(
                                    new HttpRequest(process_url, HttpRequest.GET),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                Snackbar.make(view, "Booking cancelled", Snackbar.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            } else {
                                                Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }).execute();

                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return hotelList.size();
        }


    }
}