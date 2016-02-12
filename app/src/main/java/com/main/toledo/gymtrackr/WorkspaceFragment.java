package com.main.toledo.gymtrackr;


import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

    final static int CHILD_DELETE_ID = 0;
    final static int CHILD_SWAP_ID = 1;
    final static int CHILD_SWAP_ALL_ID = 6;
    final static int CHILD_COPY_ID = 2;

    final static int GROUP_RENAME_ID = 3;
    final static int GROUP_COPY_ID = 4;
    final static int GROUP_DELETE_ID = 5;

    private static final String logTag = "wrkspcLstFrg";
    private WorkspaceExpandableListAdapterMKIII mListAdapter;

    private Context mContext;
    private WorkspaceExpandableListView mWorkspaceListView;
    private ArrayList<Circuit> mWorkout;

    public TextView mMakeExercise;
    public TextView mMakeCircuit;
    public TextView mMakeDuplicate;
    private ImageView mUIDraggable;
    private boolean mAddingItem = false;

    private Exercise mExerciseToSwap;
    private Exercise mCopyExercise = null;
    private Circuit mCopyCircuit = null;

    public static final Exercise NO_EXERCISE_TO_COPY = null;
    public static final Circuit NO_CIRCUIT_TO_COPY = null;
    public interface swapHandler {
        void swap(Exercise replacementExercise);
    }

    private swapHandler mSingleSwapHandler = new swapHandler() {
        @Override
        public void swap(Exercise replacementExercise) {
            swapThisExercise(mExerciseToSwap, replacementExercise);
            mExerciseToSwap = null;
        }
    };

    private swapHandler mSwapAllHandler = new swapHandler(){
        @Override
        public void swap(Exercise replacementExercise) {
            swapAllExercise(mExerciseToSwap, replacementExercise);
            mExerciseToSwap = null;
        }
    };

    public void swapThisExercise(Exercise exerciseToReplace, Exercise replacementExercise){
        exerciseToReplace.setName(replacementExercise.getName());
        exerciseToReplace.setId(replacementExercise.getId());
        exerciseToReplace.setEquipment(replacementExercise.getEquipment());
        exerciseToReplace.setMuscleGroup(replacementExercise.getMuscleGroup());
        int tempWeight = 0;
        int tempTime = 0;
        int tempReps = 0;

        for(Metric m : exerciseToReplace.getMetrics()){
            switch(m.getType()){
                case REPS:
                    tempReps = m.getMetricIntValue();
                    break;
                case TIME:
                    tempTime = m.getMetricIntValue();
                    break;
                case WEIGHT:
                    tempWeight = m.getMetricIntValue();
                    break;
                case OTHER:
                    break;
            }
        }

        exerciseToReplace.clearMetrics();

        ArrayList<Metric> metrics = new ArrayList<>();
        Metric tempMetric;
        for(Metric m : replacementExercise.getMetrics()){
            switch(m.getType()){
                case REPS:
                    tempMetric = new Metric();
                    tempMetric.setType(metricType.REPS);
                    tempMetric.setMetricIntValue(tempReps);
                    metrics.add(tempMetric);
                    break;
                case TIME:
                    tempMetric = new Metric();
                    tempMetric.setType(metricType.TIME);
                    tempMetric.setMetricIntValue(tempTime);
                    metrics.add(tempMetric);
                    break;
                case WEIGHT:
                    tempMetric = new Metric();
                    tempMetric.setType(metricType.WEIGHT);
                    tempMetric.setMetricIntValue(tempWeight);
                    metrics.add(tempMetric);
                    break;
                case OTHER:
                    break;
            }
        }
        exerciseToReplace.addMetrics(metrics);
    }

    public void swapAllExercise(Exercise exerciseTypeToBeReplaced, Exercise exerciseToReplace){
        String name = exerciseTypeToBeReplaced.getName();
        for(Circuit c: mWorkout){
            for(Exercise potentialReplacementExercise: c.getExercises()){
                if(potentialReplacementExercise.getName().equals(name)){
                    swapThisExercise(potentialReplacementExercise, exerciseToReplace);
                }
            }
        }
    }

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

        mWorkspaceListView = (WorkspaceExpandableListView)
                v.findViewById(R.id.workspaceListView);
        /*
        mWorkspaceListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                //Log.d("PAD BUGS", "ON GROUP COLLAPSE CALLED");
                if (!mDragInProgress)
                    WorkoutData.get(getActivity()).getWorkout().get(groupPosition).setExpanded(false);
                //((WorkspaceActivity)getActivity()).getAdapter().hideKeypad();
            }
        });

        mWorkspaceListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Log.d("PAD BUGS", "ON GROUP EXPAND CALLED");
                WorkoutData.get(getActivity()).getWorkout().get(groupPosition).setExpanded(true);
            }
        });
        */
        mWorkspaceListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mListAdapter.hideKeypad();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        registerForContextMenu(mWorkspaceListView);
        /*
            mWorkspaceListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

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
        mWorkspaceListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if (mWorkspaceListView.isGroupExpanded(groupPosition) && mWorkout.get(groupPosition).isOpen()) {
                    mWorkspaceListView.collapseGroupWithAnimation(groupPosition);
                    //WorkoutData.get(mContext).getWorkout().get(groupPosition).setExpanded(false);
                    mWorkout.get(groupPosition).setExpanded(false);
                } else {
                    mWorkspaceListView.expandGroupWithAnimation(groupPosition);
                    //WorkoutData.get(mContext).getWorkout().get(groupPosition).setExpanded(true);
                    mWorkout.get(groupPosition).setExpanded(true);
                }
                return true;
            }
        });

        mMakeExercise = (TextView) v.findViewById(R.id.make_exercise);
        mMakeExercise.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                final float y = event.getY();
                final float x = event.getX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {

                        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
                        Log.d(logTag, "make exercise x: " + mMakeExercise.getX() + " make Exercise y: " + mMakeExercise.getY());
                        Log.d(logTag, "make exercise height: " + mMakeExercise.getHeight() + " make exercise width: " + mMakeExercise.getWidth());
                        mWindowParams.x = (int) x - 700;

                        mWindowParams.y = (int) y + 650;

                        //Log.d(logTag, "Placing view at x : " + x + " - " + "700" + " = " + (x-700));
                        //Log.d(logTag, "Placing view at y : " + y + " - " + "650" + " = " + (x-650));
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

                        mUIDraggable = new ImageView(context);
                        mUIDraggable.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.drag5));


                        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        mWindowManager.addView(mUIDraggable, mWindowParams);

                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mDraggedItemType = 2;
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.setDragSpacing(200);
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentXPos = 150;
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (mUIDraggable != null) {
                            //Log.d(logTag, "in move in fragment");
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mUIDraggable.getLayoutParams();
                            layoutParams.x = (int) x - 700;
                            layoutParams.y = (int) y + 650;
                            WindowManager mWindowManager = (WindowManager) getActivity()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            mWindowManager.updateViewLayout(mUIDraggable, layoutParams);

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentYPos = (int)y + 1300;
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.dragHandling(false);
                            if (y < 0) {
                                //Log.d(logTag, "Y is less than 0, mAddingItem = " + mAddingItem);
                                if (!mAddingItem) {
                                    mWorkspaceListView.beginItemAddition(
                                            ExpandableListView.PACKED_POSITION_TYPE_CHILD,
                                            (int) (mWorkspaceListView.getBottom() - y),
                                            event,
                                            mUIDraggable.getHeight(),
                                            NO_CIRCUIT_TO_COPY,
                                            NO_EXERCISE_TO_COPY);
                                    mAddingItem = true;
                                    //add item initiate
                                } else {
                                    //drag item
                                    //Log.d(logTag, "should call touch event in listview...");
                                    mWorkspaceListView.onTouchEvent(event);
                                }
                            }
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {

                        if (mUIDraggable != null) {
                            mUIDraggable.setVisibility(View.GONE);
                            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                            wm.removeView(mUIDraggable);
                            mUIDraggable.setImageDrawable(null);
                            mUIDraggable = null;

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.closeUI();
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.placeGenericExercise();
                            if (mAddingItem) {
                                //Cleanup code for item addition goes here
                                mWorkspaceListView.finishItemAddition();
                                mAddingItem = false;
                            }

                        }
                    }
                }
                return true;
            }
        });

        mMakeCircuit = (TextView) v.findViewById(R.id.make_circuit);
        mMakeCircuit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                final int action = event.getAction();
                final float y = event.getY();
                final float x = event.getX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        Log.d(logTag, "make circuit x: " + mMakeCircuit.getX() + " make circuit y: " + mMakeCircuit.getY());
                        Log.d(logTag, "make circuit height: " + mMakeCircuit.getHeight() + " make circuit width: " + mMakeCircuit.getWidth());
                        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
                        mWindowParams.x = (int) x - 700;
                        mWindowParams.y = (int) y + 650;
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
                        mUIDraggable = new ImageView(context);

                        mUIDraggable.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.drag1));


                        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        mWindowManager.addView(mUIDraggable, mWindowParams);


                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mDraggedItemType = 1;
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.setDragSpacing(200);
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentXPos = 150;

                        //call thing to collapse groups
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.startedDraggingCircuit();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (mUIDraggable != null) {
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mUIDraggable.getLayoutParams();
                            layoutParams.x = (int) x - 350;
                            layoutParams.y = (int) y + 650;
                            WindowManager mWindowManager = (WindowManager) getActivity()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            mWindowManager.updateViewLayout(mUIDraggable, layoutParams);
                            /*
                             * y < 0 indicates we are out of the bounds of the addition view, and in the bounds of the listview
                             */
                            if (y < 0) {
                                //Log.d(logTag, "Y is less than 0, mAddingItem = " + mAddingItem);
                                if (!mAddingItem) {
                                    mWorkspaceListView.beginItemAddition(ExpandableListView.PACKED_POSITION_TYPE_GROUP,
                                            (int) (mWorkspaceListView.getBottom() - y),
                                            event,
                                            mUIDraggable.getHeight(),
                                            NO_CIRCUIT_TO_COPY,
                                            NO_EXERCISE_TO_COPY);
                                    mAddingItem = true;
                                    //add item initiate
                                } else {
                                    //drag item
                                    //Log.d(logTag, "should call touch event in listview...");
                                    mWorkspaceListView.onTouchEvent(event);
                                }
                            }

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentYPos = (int) y + 1300;
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.dragHandling(false);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        //Log.d(logTag, "Up called in move in circuit");
                        if (mUIDraggable != null) {
                            mUIDraggable.setVisibility(View.GONE);
                            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                            wm.removeView(mUIDraggable);
                            mUIDraggable.setImageDrawable(null);
                            mUIDraggable = null;

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.closeUI();
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.placeNewCircuit();

                            if (mAddingItem) {
                                //Cleanup code for item addition goes here
                                mWorkspaceListView.finishItemAddition();
                                mAddingItem = false;
                            }

                        }
                    }
                }
                return true;
            }
        });
        //todo here

        mMakeDuplicate = (TextView) v.findViewById(R.id.duplicate_item);
        mMakeDuplicate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Nothing to duplicate
                if (mCopyCircuit == null && mCopyExercise == null) return true;

                final int action = event.getAction();
                final float y = event.getY();
                final float x = event.getX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        Log.d(logTag, "make duplicate x: " + mMakeDuplicate.getX() + " make duplicate y: " + mMakeDuplicate.getY());
                        Log.d(logTag, "make duplicate height: " + mMakeDuplicate.getHeight() + " make duplicate width: " + mMakeDuplicate.getWidth());
                        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
                        mWindowParams.x = (int) x - 700;
                        Log.d(logTag, "");
                        mWindowParams.y = (int) y + 650;

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
                        mUIDraggable = new ImageView(context);

                        mUIDraggable.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.drag1));


                        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        mWindowManager.addView(mUIDraggable, mWindowParams);


                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mDraggedItemType = 1;
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.setDragSpacing(200);
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentXPos = 150;

                        //call thing to collapse groups
                        //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.startedDraggingCircuit();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (mUIDraggable != null) {
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mUIDraggable.getLayoutParams();
                            layoutParams.x = (int) x - 350;
                            layoutParams.y = (int) y + 650;
                            WindowManager mWindowManager = (WindowManager) getActivity()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            mWindowManager.updateViewLayout(mUIDraggable, layoutParams);


                            if (y < 0) {
                                //Log.d(logTag, "Y is less than 0, mAddingItem = " + mAddingItem);
                                if (!mAddingItem) {

                                    int type = ExpandableListView.PACKED_POSITION_TYPE_NULL;
                                    Circuit copyCircuit = NO_CIRCUIT_TO_COPY;
                                    Exercise copyExercise = NO_EXERCISE_TO_COPY;

                                    if (mCopyCircuit != null) {
                                        type = ExpandableListView.PACKED_POSITION_TYPE_GROUP;
                                        copyCircuit = mCopyCircuit;
                                        mCopyCircuit = WorkoutData.getCopyCircuit(mCopyCircuit);
                                    }

                                    if (mCopyExercise != null) {
                                        type = ExpandableListView.PACKED_POSITION_TYPE_CHILD;
                                        copyExercise = mCopyExercise;
                                        mCopyExercise = WorkoutData.getCopyExercise(mCopyExercise);
                                    }

                                    mWorkspaceListView.beginItemAddition(type,
                                            (int) (mWorkspaceListView.getBottom() - y),
                                            event,
                                            mUIDraggable.getHeight(),
                                            copyCircuit,
                                            copyExercise);

                                    mAddingItem = true;
                                    //add item initiate
                                } else {
                                    //drag item
                                    //Log.d(logTag, "should call touch event in listview...");
                                    mWorkspaceListView.onTouchEvent(event);
                                }
                            }

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.mCurrentYPos = (int) y + 1300;
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.dragHandling(false);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        Log.d(logTag, "Up called in move in circuit");
                        if (mUIDraggable != null) {
                            mUIDraggable.setVisibility(View.GONE);
                            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                            wm.removeView(mUIDraggable);
                            mUIDraggable.setImageDrawable(null);
                            mUIDraggable = null;

                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.closeUI();
                            //((WorkspaceActivity) getActivity()).ListFragment.mWorkspaceListView.placeNewCircuit();

                            if (mAddingItem) {
                                //Cleanup code for item addition goes here
                                mWorkspaceListView.finishItemAddition();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo)menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
        Log.d(logTag, "onCreateContextMenu() group: " + group + " child: " + child);

            /*
             * Possibly a hack, fixes an issue where the context menu would pop up when a circuit
             * view's drag view would be long pressed.  Potential race condition?
             */
        if(mWorkspaceListView.isDragInProgress()) return;

        if(WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(group)) return;

        if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
            if(mWorkout.get(group).getExercise(child).getName().equals("test")) return;
            Log.d(logTag, "child context");
            menu.setHeaderTitle("Exercise options:");
            menu.add(Menu.NONE, WorkspaceFragment.CHILD_DELETE_ID, Menu.NONE, "Delete");
            menu.add(Menu.NONE, WorkspaceFragment.CHILD_COPY_ID, Menu.NONE, "Copy");
            menu.add(Menu.NONE, WorkspaceFragment.CHILD_SWAP_ID, Menu.NONE, "Swap");
            menu.add(Menu.NONE, WorkspaceFragment.CHILD_SWAP_ALL_ID, Menu.NONE, "Swap All");
        }

        if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
            if(!mWorkout.get(group).isOpen()) return;


            Log.d(logTag, "group context");
            menu.setHeaderTitle("Circuit options:");
            menu.add(Menu.NONE, WorkspaceFragment.GROUP_DELETE_ID, Menu.NONE, "Delete");
            menu.add(Menu.NONE, WorkspaceFragment.GROUP_COPY_ID, Menu.NONE, "Copy");
            menu.add(Menu.NONE, WorkspaceFragment.GROUP_RENAME_ID, Menu.NONE, "Rename");
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
        Intent i;
        switch(item.getItemId()){

            /*
             * Child context menu handling.
             */
            case CHILD_DELETE_ID:
                mListAdapter.removeChild(group, child);
                if(!mWorkout.get(group).isOpen()) mListAdapter.removeGroup(group);
                mListAdapter.notifyDataSetChanged();
                break;
            case CHILD_COPY_ID:
                mCopyCircuit = null;
                mCopyExercise = WorkoutData.getCopyExercise(mWorkout.get(group).getExercise(child));
                break;
            case CHILD_SWAP_ID:
                mExerciseToSwap = mWorkout.get(group).getExercise(child);
                WorkoutData.get(mContext).setSwapHandler(mSingleSwapHandler);
                i = new Intent(getActivity(), BrowseActivity.class);
                startActivity(i);
                break;
            case CHILD_SWAP_ALL_ID:
                mExerciseToSwap = mWorkout.get(group).getExercise(child);
                WorkoutData.get(mContext).setSwapHandler(mSwapAllHandler);
                i = new Intent(getActivity(), BrowseActivity.class);
                startActivity(i);
                break;

            /*
             * Group context menu handling.
             */
            case GROUP_DELETE_ID:
                mListAdapter.removeGroup(group);
                mListAdapter.notifyDataSetChanged();
                break;
            case GROUP_COPY_ID:
                mCopyExercise = null;
                mCopyCircuit = WorkoutData.getCopyCircuit(mWorkout.get(group));
                break;
            case GROUP_RENAME_ID:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume(){
        Log.d(logTag, "listfrag resume");
        if(mListAdapter == null)
            mListAdapter = new WorkspaceExpandableListAdapterMKIII(mContext);
        mWorkspaceListView.setAdapter(mListAdapter);
        mListAdapter.hideKeypad();
        mWorkspaceListView.init();

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
        if (exercise == -1) {
            if (circuitOpenStatus)
                exercise = WorkoutData.get(mContext).getWorkout().get(circuitVal).getExercises().size() - 2;
            else
                exercise = 0;
        }
        mWorkspaceListView.setSelectedChild(circuitVal, exercise, false);
        mWorkspaceListView.post(new Runnable() {
            @Override
            public void run() {
                mWorkspaceListView.smoothScrollBy(-300, 0);
            }
        });

    }

    public WorkspaceExpandableListAdapterMKIII getAdapter(){
        return mListAdapter;
    }
}

