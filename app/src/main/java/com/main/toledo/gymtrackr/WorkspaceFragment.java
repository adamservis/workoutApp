package com.main.toledo.gymtrackr;


import android.app.usage.UsageEvents;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adam on 2/25/2015.
 */
public class WorkspaceFragment extends Fragment {
    final int NOT_BROWSE = 0, BROWSE_WORKOUT = 1, WORKOUT_BROWSE = 2;
    final int FROM_DETAIL = 6, FROM_WORKSPACE = 7;

    private static final String logTag = "wrkspcLstFrg";
    private WorkspaceExpandableListAdapterMKIII mListAdapter;

    private Context mContext;
    private WorkspaceExpandableListView workspaceListView;
    private ArrayList<Circuit> mWorkout;

    public TextView makeExercise;
    public TextView makeCircuit;
    private ImageView UIDraggable;
    private boolean mAddingItem = false;
    @Override

    public void onCreate(Bundle savedInstanceState) {
        //Log.d("PAD BUGS", "ONCREATE() CALLED IN WLFRAG");
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mWorkout = WorkoutData.get(mContext).getWorkout();
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //sets the view for the fragment
        //Log.d("PAD BUGS", "ON CREATE VIEW CALLED IN WLFRAG");
        View v = inflater.inflate(R.layout.w_frag, null);

        workspaceListView = (WorkspaceExpandableListView)
                v.findViewById(R.id.workspaceListView);
        /*
        workspaceListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                //Log.d("PAD BUGS", "ON GROUP COLLAPSE CALLED");
                if (!mDragInProgress)
                    WorkoutData.get(getActivity()).getWorkout().get(groupPosition).setExpanded(false);
                //((WorkspaceActivity)getActivity()).getAdapter().hideKeypad();
            }
        });

        workspaceListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Log.d("PAD BUGS", "ON GROUP EXPAND CALLED");
                WorkoutData.get(getActivity()).getWorkout().get(groupPosition).setExpanded(true);
            }
        });
        */
        workspaceListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        mListAdapter.hideKeypad();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        /*
            workspaceListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if(!WorkoutData.get(getActivity())
                        .getWorkout().get(groupPosition).isOpen()){
                    return true;
                } else {
                    return false;
                }
            }
        });
         */
        workspaceListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if(workspaceListView.isGroupExpanded(groupPosition) && mWorkout.get(groupPosition).isOpen()){
                    workspaceListView.collapseGroupWithAnimation(groupPosition);
                    //WorkoutData.get(mContext).getWorkout().get(groupPosition).setExpanded(false);
                    mWorkout.get(groupPosition).setExpanded(false);
                } else {
                    workspaceListView.expandGroupWithAnimation(groupPosition);
                    //WorkoutData.get(mContext).getWorkout().get(groupPosition).setExpanded(true);
                    mWorkout.get(groupPosition).setExpanded(true);
                }
                return true;
            }
        });

        makeExercise = (TextView) v.findViewById(R.id.make_exercise);
        makeExercise.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                final float y = event.getY();
                final float x = event.getX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {

                        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
                        mWindowParams.x = (int)x - 700;

                        mWindowParams.y = (int)y + 650;

                        //Log.d(logTag, "Placing view at x : " + x + " - " + "700" + " = " + (x-700));
                        //Log.d(logTag, "Placing view at y : " + y + " - " + "650" + " = " + (x-650));
                        //TODO: MAKE SCREEN SIZE SCALABLE
                        mWindowParams.height = 200;
                        mWindowParams.width = 200;
                        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                        mWindowParams.format = PixelFormat.TRANSLUCENT;
                        mWindowParams.windowAnimations = 0;

                        Context context = getActivity();

                        UIDraggable = new ImageView(context);
                        UIDraggable.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.drag5));


                        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        mWindowManager.addView(UIDraggable, mWindowParams);

                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mDraggedItemType = 2;
                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.setDragSpacing(200);
                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mCurrentXPos = 150;
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (UIDraggable != null) {
                            //Log.d(logTag, "in move in fragment");
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) UIDraggable.getLayoutParams();
                            layoutParams.x = (int)x - 700;
                            layoutParams.y = (int)y + 650;
                            WindowManager mWindowManager = (WindowManager) getActivity()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            mWindowManager.updateViewLayout(UIDraggable, layoutParams);

                            //TODO make scalable

                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mCurrentYPos = (int)y + 1300;
                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.dragHandling(false);
                            if(y < 0){
                                //Log.d(logTag, "Y is less than 0, mAddingItem = " + mAddingItem);
                                if(!mAddingItem){
                                    workspaceListView.beginItemAddition(ExpandableListView.PACKED_POSITION_TYPE_CHILD, (int)(workspaceListView.getBottom() - y), event, UIDraggable.getHeight());
                                    mAddingItem = true;
                                    //add item initiate
                                } else {
                                    //drag item
                                    //Log.d(logTag, "should call touch event in listview...");
                                    workspaceListView.onTouchEvent(event);
                                }
                            }
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {

                        if (UIDraggable != null) {
                            UIDraggable.setVisibility(View.GONE);
                            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                            wm.removeView(UIDraggable);
                            UIDraggable.setImageDrawable(null);
                            UIDraggable = null;

                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.closeUI();
                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.placeGenericExercise();
                            if(mAddingItem){
                                //Cleanup code for item addition goes here
                                workspaceListView.finishItemAddition();
                                mAddingItem = false;
                            }

                        }
                    }
                }
                return true;
            }
        });

        makeCircuit = (TextView) v.findViewById(R.id.make_circuit);
        makeCircuit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                final float y = event.getY();
                final float x = event.getX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {

                        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
                        mWindowParams.x = (int) x - 700;
                        Log.d(logTag, "");
                        mWindowParams.y = (int) y + 650;
                        //TODO: MAKE SCREEN SIZE SCALABLE
                        mWindowParams.height = 200;
                        mWindowParams.width = 200;
                        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                        mWindowParams.format = PixelFormat.TRANSLUCENT;
                        mWindowParams.windowAnimations = 0;

                        Context context = getActivity();
                        UIDraggable = new ImageView(context);

                        UIDraggable.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.drag1));


                        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        mWindowManager.addView(UIDraggable, mWindowParams);

                        //Todo readability: what is 1?

                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mDraggedItemType = 1;
                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.setDragSpacing(200);
                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mCurrentXPos = 150;

                        //call thing to collapse groups
                        //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.startedDraggingCircuit();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (UIDraggable != null) {
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) UIDraggable.getLayoutParams();
                            layoutParams.x = (int)x - 350;
                            layoutParams.y = (int)y + 650;
                            WindowManager mWindowManager = (WindowManager) getActivity()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            mWindowManager.updateViewLayout(UIDraggable, layoutParams);

                            //TODO make scalable
                            if(y < 0){
                                //Log.d(logTag, "Y is less than 0, mAddingItem = " + mAddingItem);
                                if(!mAddingItem){
                                    workspaceListView.beginItemAddition(ExpandableListView.PACKED_POSITION_TYPE_GROUP, (int)(workspaceListView.getBottom() - y), event, UIDraggable.getHeight());
                                    mAddingItem = true;
                                    //add item initiate
                                } else {
                                    //drag item
                                    //Log.d(logTag, "should call touch event in listview...");
                                    workspaceListView.onTouchEvent(event);
                                }
                            }

                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.mCurrentYPos = (int) y + 1300;
                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.dragHandling(false);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        Log.d(logTag, "Up called in move in circuit");
                        if (UIDraggable != null) {
                            UIDraggable.setVisibility(View.GONE);
                            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                            wm.removeView(UIDraggable);
                            UIDraggable.setImageDrawable(null);
                            UIDraggable = null;

                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.closeUI();
                            //((WorkspaceActivity) getActivity()).ListFragment.workspaceListView.placeNewCircuit();

                            if(mAddingItem){
                                //Cleanup code for item addition goes here
                                workspaceListView.finishItemAddition();
                                mAddingItem = false;
                            }

                        }
                    }
                }
                return true;
            }
        });

        return v;
    }

    @Override
    public void onResume(){
        Log.d(logTag, "listfrag resume");
        if(mListAdapter == null)
            mListAdapter = new WorkspaceExpandableListAdapterMKIII(mContext);
        workspaceListView.setAdapter(mListAdapter);
        mListAdapter.hideKeypad();
        workspaceListView.init();

        int browseMode = WorkoutData.get(mContext).getBrowseState();
        switch(browseMode){
            case NOT_BROWSE:
                break;
            case BROWSE_WORKOUT:
                WorkoutData.get(mContext).setBrowseState(NOT_BROWSE);
                int circuitVal = WorkoutData.get(mContext).getStateCircuit();

                focusItem(circuitVal, -1);

                //focus right thing
                break;
        }
        int detailMode = WorkoutData.get(mContext).getDetailTransition();
        switch(detailMode){
            case FROM_DETAIL:
                WorkoutData.get(mContext).setDetailTransition(FROM_WORKSPACE);
                int circuit = WorkoutData.get(mContext).getDetailCircuit();
                int exercise = WorkoutData.get(mContext).getDetailExercise();
                focusItem(circuit, exercise);

                break;
        }
        super.onResume();
    }

    private void focusItem(int circuitVal, int exercise){

        boolean circuitOpenStatus = WorkoutData.get(mContext).isStateCircuitOpen();
        int child;
        if(exercise == -1) {
            if (circuitOpenStatus)
                exercise = WorkoutData.get(mContext).getWorkout().get(circuitVal).getExercises().size() - 2;
            else
                exercise = 0;
        }
        workspaceListView.setSelectedChild(circuitVal, exercise, false);
        workspaceListView.post(new Runnable() {
            @Override
            public void run() {
                workspaceListView.smoothScrollBy(-300, 0);
            }
        });

    }
}

