package com.main.toledo.gymtrackr;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adam on 2/26/2015.
 */
public class HistoricExerciseAdapter extends ArrayAdapter {
    //private final DynamicView d;
    private ArrayList<ExerciseHistory> m_exerciseHistory;

    //private LinearLayout metricLayout;

    private final int dateId = View.generateViewId();
    private final int firstMetricId = View.generateViewId();
    private final int secondMetricId = View.generateViewId();
    private final int thirdMetricId = View.generateViewId();
    private final int nameId = View.generateViewId();


    private final Context mContext;

    private static final int TYPE_TERRIBLE_THINGS = 0;
    private static final int TYPE_ONE_METRIC = 1;
    private static final int TYPE_TWO_METRICS = 2;
    private static final int TYPE_THREE_METRICS = 3;
    private static final int TYPE_COUNT = 4;

    private int mMarginsInPixels;
    private int mMetricEditLeftMarginPixels;
    private final int mMetricEditLeftMargininDP = 30;
    private int mExerciseName = 20;
    private int mMarginsDP = 10;
    public HistoricExerciseAdapter(Context context, int resource, ArrayList<ExerciseHistory> history){
        super(context, resource, history);
        m_exerciseHistory = history;
        mContext = context;
        float scale = context.getResources().getDisplayMetrics().density;
        mMetricEditLeftMarginPixels = (int) (mMetricEditLeftMargininDP * scale + 0.5f);

        mMarginsInPixels = (int) (mMarginsDP * scale + 0.5f);
    }
    @Override
    public int getItemViewType(int position){
        int num_metrics = m_exerciseHistory.get(position).getMetrics().size();
        int type;
        switch (num_metrics){
            case 1:
                type = TYPE_ONE_METRIC;
                break;
            case 2:
                type = TYPE_TWO_METRICS;
                break;
            case 3:
                type =  TYPE_THREE_METRICS;
                break;
            default:
                type = TYPE_TERRIBLE_THINGS;
                break;
        }
        return type;
    }

    @Override
    public int getViewTypeCount(){
        return TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Log.d("4.11", "getView called on position: " + position);
        ViewHolder holder = null;
        int type = getItemViewType(position);
        ArrayList<Metric> metrics = m_exerciseHistory.get(position).getMetrics();
        int numMetrics = metrics.size();
        if( convertView == null ){
            holder = new ViewHolder();
            LinearLayout metricLayout = initializeView(numMetrics);
            switch(type){
                case TYPE_ONE_METRIC:
                    convertView = metricLayout;
                    holder.name = (TextView)convertView.findViewById(nameId);
                    holder.firstMetric = (TextView)convertView.findViewById(firstMetricId);
                    break;
                case TYPE_TWO_METRICS:
                    convertView = metricLayout;
                    holder.name = (TextView)convertView.findViewById(nameId);
                    holder.firstMetric = (TextView)convertView.findViewById(firstMetricId);
                    holder.secondMetric = (TextView)convertView.findViewById(secondMetricId);
                    break;
                case TYPE_THREE_METRICS:
                    convertView = metricLayout;
                    holder.name = (TextView)convertView.findViewById(nameId);
                    holder.firstMetric = (TextView)convertView.findViewById(firstMetricId);
                    holder.secondMetric = (TextView)convertView.findViewById(secondMetricId);
                    holder.thirdMetric = (TextView)convertView.findViewById(thirdMetricId);
                    break;
                case TYPE_TERRIBLE_THINGS:
                    convertView = metricLayout;
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(m_exerciseHistory.get(position).getExerciseName());
        String s;
        switch(numMetrics){
            case 0:
                break;
            case 1:
                s = "" + metrics.get(0).getType() + ": " + metrics.get(0).getMetricIntValue();
                holder.firstMetric.setText(s);
                break;
            case 2:
                s = "" + metrics.get(0).getType() + ": " + metrics.get(0).getMetricIntValue();
                holder.firstMetric.setText(s);
                s = "" + metrics.get(1).getType() + ": " + metrics.get(1).getMetricIntValue();
                holder.secondMetric.setText(s);
                break;
            case 3:
                s = "" + metrics.get(0).getType() + ": " + metrics.get(0).getMetricIntValue();
                holder.firstMetric.setText(s);
                s = "" + metrics.get(1).getType() + ": " + metrics.get(1).getMetricIntValue();
                holder.secondMetric.setText(s);
                s = "" + metrics.get(2).getType() + ": " + metrics.get(2).getMetricIntValue();
                holder.thirdMetric.setText(s);
                break;
            default:
                break;
        }

        return convertView;
    }

    private LinearLayout initializeView(int numMetrics){
        LinearLayout metricLayout = new LinearLayout(mContext);
        metricLayout.setOrientation(LinearLayout.VERTICAL);
        metricLayout.setPadding(0,0,0,mMarginsDP);

        TextView nameView = new TextView(mContext);
        nameView.setId(nameId);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mExerciseName);
        metricLayout.addView(nameView);

        TextView firstMetricView = new TextView(mContext);
        firstMetricView.setId(firstMetricId);
        firstMetricView.setPadding(mMetricEditLeftMarginPixels,0,0,0);

        TextView secondMetricView = new TextView(mContext);
        secondMetricView.setId(secondMetricId);
        secondMetricView.setPadding(mMetricEditLeftMarginPixels,0,0,0);

        TextView thirdMetricView = new TextView(mContext);
        thirdMetricView.setId(thirdMetricId);
        thirdMetricView.setPadding(mMetricEditLeftMarginPixels,0,0,0);

        switch(numMetrics) {
            case 0:
                break;
            case 1:
                metricLayout.addView(firstMetricView);
                break;
            case 2:
                metricLayout.addView(firstMetricView);
                metricLayout.addView(secondMetricView);
                break;
            case 3:
                metricLayout.addView(firstMetricView);
                metricLayout.addView(secondMetricView);
                metricLayout.addView(thirdMetricView);
                break;
            default:
                break;
        }
        return metricLayout;
    }

    public static class ViewHolder{
        public TextView name;
        public TextView firstMetric;
        public TextView secondMetric;
        public TextView thirdMetric;
    }

}
