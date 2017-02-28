package com.stewartmcm.android.evclarity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stewartmcm.android.evclarity.activities.Main2Activity;
import com.stewartmcm.android.evclarity.data.Contract;
import com.stewartmcm.android.evclarity.data.PredictEvDatabaseHelper;
import com.stewartmcm.android.evclarity.models.Trip;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by stewartmcmillan on 5/26/16.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripAdapterViewHolder> {

    final private Context mContext;
    LayoutInflater cursorInflater;
    private Cursor mCursor;
    private ArrayList<Trip> mTrips;
    final private View mEmptyView;
    final private TripAdapterOnClickHandler mClickHandler;
    final private ItemChoiceManager mICM;



    public class TripAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTotalSavingsTextView;
        TextView mTotalMileageTextView;

        TextView mMileageTextView;
        TextView mDateTimeTextView;
        TextView mSavingsTextView;

        public TripAdapterViewHolder(View itemView) {
            super(itemView);

            mTotalSavingsTextView = (TextView) itemView.findViewById(R.id.savings_text_view);
            mTotalMileageTextView = (TextView) itemView.findViewById(R.id.total_mileage_textview);

            mMileageTextView = (TextView) itemView.findViewById(R.id.list_item_mileage);
            mDateTimeTextView = (TextView) itemView.findViewById(R.id.list_item_date);
            mSavingsTextView = (TextView) itemView.findViewById(R.id.list_item_savings);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int symbolColumn = mCursor.getColumnIndex(Contract.Trip.COLUMN_DATE);

            //TODO: uncomment to implement recyclerview clickhandler
//            mClickHandler.onClick(mCursor.getString(symbolColumn), this);
            mICM.onClick(this);

        }
    }
    public static interface TripAdapterOnClickHandler {
        void onClick(String dateTime, TripAdapterViewHolder viewHolder);
    }

    public TripAdapter(Context context, TripAdapterOnClickHandler clickHandler, View emptyView, int choiceMode) {

        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);

        mTrips = new ArrayList<>();

        SQLiteDatabase db = null;
        PredictEvDatabaseHelper mHelper = PredictEvDatabaseHelper.getInstance(mContext);
        db = mHelper.getReadableDatabase();

        mCursor = db.query("TRIP", null, null, null, null, null, null);
        mCursor.moveToFirst();

        for (int i = 0; i < mCursor.getCount(); i++) {

            Trip trip = new Trip(mCursor.getString(Main2Activity.COL_DATE),
                    mCursor.getFloat(Main2Activity.COL_TRIP_MILES),
                    mCursor.getFloat(Main2Activity.COL_TRIP_SAVINGS));

            mTrips.add(trip);
            mCursor.moveToNext();
        }
        mCursor.close();

        //TODO: delete after validating code above adds trips to recyclerview correctly
//        for (int i = 0; i < 20; i++) {
//
//            Trip trip = new Trip("today",
//                    i,
//                    i);
//
//            mTrips.add(trip);
//        }

    }

    @Override
    public TripAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        if ( parent instanceof RecyclerView ) {
//            int layoutId = -1;
//            switch (viewType) {
//                case VIEW_TYPE_SUMMARY: {
//                    layoutId = R.layout.list_item_summary;
//                    break;
//                }
//                case VIEW_TYPE_TRIP: {
//                    layoutId = R.layout.list_item_trip;
//                    break;
//                }
//            }
//            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
//            view.setFocusable(true);
//            return new TripAdapterViewHolder(view);
//        } else {
//            throw new RuntimeException("Not bound to RecyclerView");
//        }

        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trip, parent, false);

        return new TripAdapterViewHolder(item);
    }

    @Override
    public void onBindViewHolder(TripAdapterViewHolder holder, int position) {

        float miles = mTrips.get(position).getMiles();
        float savings = mTrips.get(position).getSavings();

        DecimalFormat milesFormat = new DecimalFormat("###.0");
        DecimalFormat savingsFormat = new DecimalFormat("###.00");

        String savingsString = savingsFormat.format(savings);
        Log.i(TAG, "doInBackground: " + savingsString);

        holder.mMileageTextView.setText(miles + " miles");
        holder.mSavingsTextView.setText("$" + savingsString);
        holder.mDateTimeTextView.setText(mTrips.get(position).getTimeStamp());

//        String date = mCursor.getString(Main2Activity.COL_DATE);
//
//        holder.mMileageTextView.setText(mCursor.getString(Main2Activity.COL_TRIP_MILES) + " miles");
//        holder.mDateTimeTextView.setText(date);
//        holder.mSavingsTextView.setText(mCursor.getString(Main2Activity.COL_TRIP_SAVINGS));

        mICM.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

//    public String getTripAtPosition(int position) {
//
//        mCursor.moveToPosition(position);
//        return mCursor.getString(Main2Activity.COL_DATE);
//    }

    @Override
    public int getItemCount() {
        int count = 0;

        //commented out below to debug onSwipe
//        if (mCursor != null) {
//            count = mCursor.getCount();
//        }
        count = mTrips.size();
        return count;
    }

    public int removeTrip(int tripPosition) {
        mTrips.remove(tripPosition);

        return mTrips.size();
        //todo:consider returning new array size?
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof TripAdapterViewHolder ) {
            TripAdapterViewHolder vfh = (TripAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

//    =========== Old ListView code below; new RecyclerView code above

//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        TextView tripMileageTextView = (TextView) view.findViewById(R.id.mileage_text_view);
////        TextView dateTextView = (TextView) view.findViewById(R.id.date_text_view);
//        // might not need textview below
//        TextView staticMilesTextView;
//
//        String mileage = cursor.getString( cursor.getColumnIndex( PredictEvDatabaseHelper.COL_TRIP_MILES ) );
//        tripMileageTextView.setText(mileage + " miles");
//
////        String date = cursor.getString( cursor.getColumnIndex(PredictEvDatabaseHelper.COL_DATE));
////        dateTextView.setText(date);
//    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//
//        return cursorInflater.inflate(R.layout.list_item_trip, parent, false);
//    }
}