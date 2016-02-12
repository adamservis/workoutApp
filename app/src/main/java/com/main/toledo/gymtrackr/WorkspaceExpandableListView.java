package com.main.toledo.gymtrackr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam on 2/22/2015.
 * WARNING:  THIS CODE IS BUTT
 * UPDATE: 5/13/2015
 * BUTT FACTOR REDUCED BY 60%
 * UPDATE: 5/19/2015 BUTT FACTOR REDUCED BY AN ADDITIONAL 15%
 * UPDATE: 6/27/2015 MOSTLY NOT BUTT
 */
public class WorkspaceExpandableListView extends animatedExpandableListView {

    /*
    VARS TO BE USED FOR SPECIAL ANIMATIONS

    private final static int OPEN_TO_OPEN = 0;
    private final static int FROM_CLOSED_UP_TO_OPEN = 1;
    private final static int FROM_CLOSED_DOWN_TO_OPEN = 2;
    private final static int FROM_OPEN_UP_TO_CLOSED = 3;
    private final static int FROM_OPEN_DOWN_TO_CLOSED = 4;
    private int mViewPosition;
    private View mLastView = null;
    private View mSecondToLastView = null;
    private ArrayList<View> mViewStorage = new ArrayList<>();
    private int ANIMATION_FLAG = OPEN_TO_OPEN;
    private boolean mAnimating = false;
    private long mAnimationStartTime;
    private float mPixelDistanceToAnimate;
    private float mAnimationVelocity;
    private int[] mLastViewCoords;
    */

    private final String logTag = "WorkspaceExpandableList";

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;
    private final int LINE_THICKNESS = 15;

    private ArrayList<Circuit> Workout = new ArrayList<>();

    private Context mContext;

    //SCROLL STUFF
    private boolean mDragMode = false;
    private boolean mIsMobileScrolling = false;
    private boolean mIsWaitingForScrollFinish = false;
    private int mSmoothScrollAmountAtEdge = 0;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    //DRAG COUNTDOWN VARS
    private boolean mDragInProgress = false;
    public int mDownY;
    public int mLastEventY;
    private int mTotalOffset = 0;

    //HoverView
    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    //pointer id
    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private final long INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;

    //SWAP VARS
    final static public int INVALID_POSITION = -1;
    final private int ABOVE = 1;
    final private int BELOW = 2;

    //private boolean mSwapInProgress = false;

    private int ABOVE_VALID_GROUP = INVALID_POSITION;
    private int ABOVE_VALID_POSITION = INVALID_POSITION;
    private boolean ABOVE_GROUP_IS_OPEN = false;

    private int BELOW_VALID_POSITION = INVALID_POSITION;
    private int BELOW_VALID_GROUP = INVALID_POSITION;
    private boolean BELOW_GROUP_IS_OPEN = false;

    private int CURRENT_CHILD = INVALID_POSITION;
    private int CURRENT_GROUP = INVALID_POSITION;
    private boolean CURRENT_GROUP_IS_OPEN = false;

    private HashMap<Circuit, Boolean> mExpansionMemory;

    private int DRAGGED_ITEM_TYPE;
    private boolean HIDE_MOBILE_VIEW_FLAG = false;
    //TYPE
    private boolean mShouldRemoveObserver;
    private WorkspaceExpandableListAdapterMKIII mAdapter;

    //ITEM ADDITION VARS
    private boolean mAddingCircuitFlag = false;
    private Circuit mCircuitToAdd = null;
    //private boolean COPY_CIRCUIT_FLAG = false;
    private WorkspaceExpandableListAdapterMKIII.dragListener mListener = new WorkspaceExpandableListAdapterMKIII.dragListener(){
        @Override
        public void OnDragHandleLongClickedListener(final View selectView, int type){
            dragSetup(selectView, type);
        }
    };

    public WorkspaceExpandableListView(Context context, AttributeSet attrs) {

        super(context, attrs);
        mContext = context;
        Workout = WorkoutData.get(mContext).getWorkout();
        //WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //Display display = wm.getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);
        //SCREENWIDTH = size.x;
        //SCREENHEIGHT = size.y;

        setOnScrollListener(mScrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mSmoothScrollAmountAtEdge = (int)(SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        //mAdapter = (WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter();
        //Log.d(logTag, "Listview constructed");
    }

    public void init(){
        restoreListExpansion();
        setGroupIndicator(null);
        if(mAdapter == null){
            mAdapter = (WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter();
        }
        mAdapter.setDragListener(mListener);
    }

    public void beginItemAddition(int type, int listY, MotionEvent event, float uiDraggableHeight, Circuit copyCircuit, Exercise copyExercise){
        //printWorkoutData();
        mActivePointerId = event.getPointerId(0);
        DRAGGED_ITEM_TYPE = type;
        mDownY = listY - (int)uiDraggableHeight;
        //Log.d(logTag, "uidraggable height: " + uiDraggableHeight);
        mDragInProgress = true;
        mDragMode = true;
        int position = pointToPosition(0, listY);

        //In the event we get an invalid position ( -1 ) from point to position,
        //decrease the Y value by an offset and attempt to get a view at that position
        //repeat until we recieve a valid position
        //CAUTION: ASSUMES WE WILL ABSOLUTELY RECEIVE A VALID POSITION

        while(position == -1){
            listY = listY - 10;
            position = pointToPosition(0, listY);
        }

        CURRENT_GROUP = getPackedPositionGroup(getExpandableListPosition(position));
        CURRENT_CHILD = getPackedPositionChild(getExpandableListPosition(position));
        CURRENT_GROUP_IS_OPEN = Workout.get(CURRENT_GROUP).isOpen();
        //Log.d(logTag, "Last position on list is G: " + CURRENT_GROUP + " C: " + CURRENT_CHILD);
        setOnScrollListener(null);  //disable scrolling for item placement
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            Exercise exerciseToAdd;
            if(copyExercise == WorkspaceFragment.NO_EXERCISE_TO_COPY){
                exerciseToAdd = WorkoutData.getGenericExercise();
            } else {
                exerciseToAdd = copyExercise;
            }
            if (Workout.get(CURRENT_GROUP).isOpen() && CURRENT_CHILD != -1) {
                //add item to open circuit
                //Log.d(logTag, "!!!!!!!!!!!!!!!!!!!!!!!!adding item to open circuit, current group " + CURRENT_GROUP + " current child " + CURRENT_CHILD);
                mMobileItemId = mAdapter.addChild(CURRENT_GROUP, CURRENT_CHILD, exerciseToAdd);
                mAdapter.notifyDataSetChanged();
                restoreListExpansion();
            } else {
                //add item to closed circuit
                //if(WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(CURRENT_GROUP))
                mAdapter.addGroup(CURRENT_GROUP, WorkoutData.getClosedCircuit());
                mMobileItemId = mAdapter.addChild(CURRENT_GROUP, 0, exerciseToAdd);
                CURRENT_CHILD = 0;
                CURRENT_GROUP_IS_OPEN = false;
                mAdapter.notifyDataSetChanged();
                //Log.d(logTag, "!!!!!!!!!!!!!!!!!!!!!!!!adding item to closed circuit, id = " + mMobileItemId + " current group: " + CURRENT_GROUP);
                //printWorkoutData();
                restoreListExpansion();
            }
            /*
            post(new Runnable() {
                @Override
                public void run() {
                    startAdditionDrag();
                }
            });
            */
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {

                    observer.removeOnPreDrawListener(this);

                    View v = getViewForID(mMobileItemId);

                    if (v != null)
                        v.setVisibility(View.INVISIBLE);

                    return true;
                }
            });
            getNeighborPositions();
            //printTargetLocations();
            updateNeighborIDsForCurrentPosition();
            //printTargetIds();
            mTotalOffset = 0;


        } else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){

            if(copyCircuit == WorkspaceFragment.NO_CIRCUIT_TO_COPY){
                mCircuitToAdd = WorkoutData.getNewOpenCircuitWithName("AddedCircuit");
            } else {
                mCircuitToAdd = copyCircuit;
                //COPY_CIRCUIT_FLAG = true;
            }
            ABOVE_VALID_GROUP = CURRENT_GROUP;
            mTotalOffset = 0;
            if(WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(ABOVE_VALID_GROUP)){
                //Above thing is placeholder, add before
                //ABOVE_VALID_GROUP--;
                addCircuit();
                return;
            }

            ABOVE_GROUP_IS_OPEN = CURRENT_GROUP_IS_OPEN;
            if(ABOVE_GROUP_IS_OPEN) {
                mAboveItemId = mAdapter.getGroupId(ABOVE_VALID_GROUP);
            } else {
                mAboveItemId = mAdapter.getChildId(ABOVE_VALID_GROUP, 0);
            }

            mAddingCircuitFlag = true;

            //Log.d(logTag, "In group addition code.  above group = " + ABOVE_VALID_GROUP + "above id" + mAboveItemId);

            //Log.d(logTag, "Above position is placeholder: " + WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(ABOVE_VALID_GROUP));

        }
    }
    /*
    private void startAdditionDrag(){
        View selectedView = getViewForID(mMobileItemId);

        getNeighborPositions();
        updateNeighborIDsForCurrentPosition();

        if(selectedView==null){
            moveElement(ABOVE);
            mAdapter.notifyDataSetChanged();
            post(new Runnable() {
                @Override
                public void run() {
                    startAdditionDrag();
                }
            });
            return;
        }

        mDragMode = true;
        mTotalOffset = 0;
        selectedView.setVisibility(INVISIBLE);
        //Log.d(logTag, "Above item Id" + mAboveItemId);
        mDragInProgress = true;
    }
    */
    public void printTargetIds(){
        Log.d(logTag, "above id: " + mAboveItemId + " current id: " + mMobileItemId + " below id: " + mBelowItemId);
    }
    public void printWorkoutData(){
        int cCount = 0;
        int eCount;
        for(Circuit c : Workout){
            Log.d(logTag, "-" + cCount + "-Circuit: " + c.getName());
            cCount++;
            eCount = 0;
            for(Exercise e : c.getExercises()){
                Log.d(logTag, "--" + eCount + "-Exercise: " + e.getName());
                eCount++;
            }
        }
    }
    public void printListViewData(){
        //todo
        int first = getFirstVisiblePosition();
        int last = getLastVisiblePosition();
        Log.d(logTag, "Visible views:");
        for(int pos = first; pos<=last; pos++){
            long explstpos = getExpandableListPosition(pos);
            long id;
            if(getPackedPositionType(explstpos) == PACKED_POSITION_TYPE_CHILD){
                id = mAdapter.getChildId(getPackedPositionGroup(explstpos), getPackedPositionChild(explstpos));
            } else {
                id = mAdapter.getGroupId(getPackedPositionGroup(explstpos));
            }
            Log.d(logTag, "Group: " + getPackedPositionGroup(explstpos) + " Child: " + getPackedPositionChild(explstpos) + " View ID: " + id);
        }
    }

    public void finishItemAddition(){
        post(new Runnable() {
            @Override
            public void run() {
                //Log.d(logTag, "finishItemAddtion()");
                mDragInProgress = false;
                mDragMode = false;
                mActivePointerId = INVALID_POINTER_ID;
                mAddingCircuitFlag = false;
                if (getViewForID(mMobileItemId) != null) {
                    getViewForID(mMobileItemId).setVisibility(VISIBLE);
                } else {
                    Log.d(logTag, "MOBILE ID = NULL, ITEM NOT MADE VISIBLE");
                }
                setOnScrollListener(mScrollListener);
                invalidate();
                //Log.d(logTag, "Final item addition");
                //printWorkoutData();
            }
        });
    }
    private void saveGroupExpansionState(){
        //Log.d(logTag, "saveGroupExpansionState()");
        mExpansionMemory = new HashMap<>();
        for(Circuit c : Workout){
            //Log.d(logTag, "Expanded: " + c.isExpanded());
            mExpansionMemory.put(c, c.isExpanded());
        }
    }

    private void collapseAllGroups(){
        int numCircuits = Workout.size();
        for(int i = 0; i<numCircuits; i++){
            Circuit circuit = Workout.get(i);
            boolean expanded = isGroupExpanded(i);
            if (circuit.isOpen() && expanded) collapseGroup(i);
        }
    }

    private void dragSetup(final View selectView, int type){

        //Log.d(logTag, "dragSetup()");
        long expLstPos = getExpandableListPosition(getPositionForView(selectView));
        CURRENT_GROUP = getPackedPositionGroup(expLstPos);
        CURRENT_CHILD = getPackedPositionChild(expLstPos);

        CURRENT_GROUP_IS_OPEN = Workout.get(CURRENT_GROUP).isOpen();

        DRAGGED_ITEM_TYPE = type;

        final WorkspaceExpandableListAdapterMKIII adapter = mAdapter;

        if(type == PACKED_POSITION_TYPE_CHILD){
            mMobileItemId = adapter.getChildId(CURRENT_GROUP, CURRENT_CHILD);
            startDrag(selectView);
        } else if(type == PACKED_POSITION_TYPE_GROUP){

            mMobileItemId = adapter.getGroupId(CURRENT_GROUP);
            //Log.d(logTag, "CURRENT GROUP = " + CURRENT_GROUP);

            //store top Y position
            final int top = getViewForID(mMobileItemId).getTop();
            //Log.d(logTag, "Old top = " + top);

            //collapse all open groups, save old expanded state to mExpansionMemory for
            //later restoration
            saveGroupExpansionState();
            /*
             * DIRTY HACK USED TO FIX A RACE CONDITION ENCOUNTERED WHEN WORKSPACE CONTAINS A SMALL NUMBER
             * OF COLLAPSED GROUPS.  WHEN WORKOUTDATA IS COMPRISED ONLY OF 2-3 CIRCUITS THAT ARE COLLAPSED
             * PLUS THE TWO PLACEHOLDER CIRCUITS, START DRAG WOULD BE CALLED BEFORE UI ALTERATION IS COMPLETE
             * RESULTING IN UNPREDICTABLE DRAG BEHAVIOR.
             */
            for(int i = 0; i<Workout.size(); i++){
                expandGroup(i);
            }
            collapseAllGroups();

            adapter.toggleListPadding(true);
            //restore list position
            final WorkspaceExpandableListView listView = this;
            final ViewTreeObserver observer = getViewTreeObserver();
            mShouldRemoveObserver = false;

            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public boolean onPreDraw() {

                    //Log.d(logTag, "on predraw fired");

                    if (!mShouldRemoveObserver) {
                        //setSelectionFromTop(CURRENT_GROUP, top);
                        //Log.d(logTag, "first pass");
                        setSelectionFromTop(getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)), top);
                        mShouldRemoveObserver = true;
                        //printListViewData();
                        //invalidate();
                        //adapter.notifyDataSetChanged();
                        return false;
                    }
                    //Log.d(logTag, "second pass");

                    //Log.d(logTag, "Setting selection for group drag: " + (getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)) + getFirstVisiblePosition()));


                    mShouldRemoveObserver = false;

                    //int newTop = getViewForID(mMobileItemId).getTop();
                    //Log.d(logTag, "New top: " + newTop);
                    /*
                    if (newTop != top) {
                        adapter.toggleListPadding(true);
                        setSelectionFromTop(getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)), top);
                        Log.d(logTag, "Terrible things");
                        return false;
                    } else {
                                            observer.removeOnPreDrawListener(this);
                    mShouldRemoveObserver = false;
                    }
                    */
                    /*
                    post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    */

                    //printListViewData();
                    observer.removeOnPreDrawListener(this);
                    listView.startDrag(getViewForID(mMobileItemId));

                    return true;

                }
            });
        }
    }

    public boolean isDragInProgress(){
        return mDragMode;
    }
    private void startDrag(View selectedView){
        Log.d(logTag, "startDrag");
        //printListViewData();
        //Log.d(logTag, "mobileid + " + mMobileItemId);
        mDragMode = true;
        //Log.d(logTag, "mDragMode is now true: " + mDragMode);
        mTotalOffset = 0;

        mHoverCell = getAndAddHoverView(selectedView);

        selectedView.setVisibility(INVISIBLE);
        getNeighborPositions();
        updateNeighborIDsForCurrentPosition();
        //printTargetIds();
        //Log.d(logTag, "Above item Id" + mAboveItemId);
        mDragInProgress = true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(logTag, "!!!!!!!!!!!!!!!!!mDragMode: " + mDragMode);
        if (mDragMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: //mose if moved
                    //Log.d(logTag, "*********Move");
                    if (mActivePointerId == INVALID_POINTER_ID) {
                        //Log.d(logTag, "INVALID POINTER ID");
                        break;
                    }

                    int pointerIndex = event.findPointerIndex(mActivePointerId);

                    mLastEventY = (int) event.getY(pointerIndex);
                    int deltaY = mLastEventY - mDownY;
                    //Log.d(logTag, "now in listview touch event, move.  mDragInProgress = " + mDragInProgress);
                    if (mDragInProgress) {
                        if(mHoverCellCurrentBounds!=null) {
                            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left,
                                    mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
                            mHoverCell.setBounds(mHoverCellCurrentBounds);

                            mIsMobileScrolling = false;
                            handleMobileCellScroll();
                            handleCellSwitch();
                        } else {
                            handleAdditionCellSwitch();
                        }

                        invalidate();

                        return false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    //Log.d(logTag, "*********Action_cancel");
                    touchEventsCancelled();
                    break;
                case MotionEvent.ACTION_UP: //mouse button is released
                    //Log.d(logTag, "*********Action_up");
                    touchEventsEnded();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                    //Log.d(logTag, "*********Action_pointer_up");
                    pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                            MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = event.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        touchEventsEnded();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        //Log.d(logTag, "touch event in interceptor");
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            mDownY = (int) event.getY();
            //Log.d(logTag, "mDownY set in onInterceptTouchEvent");
            mActivePointerId = event.getPointerId(0);
            onTouchEvent(event);
        } else {
            onTouchEvent(event);
        }
        return false;
    }

    private void addCircuit(){
        //printListViewData();
        mMobileItemId = mAdapter.addGroup(ABOVE_VALID_GROUP, mCircuitToAdd);
        mCircuitToAdd = null;
        //if(!COPY_CIRCUIT_FLAG) mAdapter.addChild(ABOVE_VALID_GROUP, 0 , new Exercise());
        //COPY_CIRCUIT_FLAG = false;
        Log.d(logTag, "Added circuits id: " + mMobileItemId);
        mAdapter.notifyDataSetChanged();
        mAddingCircuitFlag = false;
        CURRENT_GROUP = ABOVE_VALID_GROUP;
        CURRENT_CHILD = INVALID_POSITION;
        CURRENT_GROUP_IS_OPEN = true;
        getNeighborPositions();
        updateNeighborIDsForCurrentPosition();

        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {

                observer.removeOnPreDrawListener(this);
                //printListViewData();
                View v = getViewForID(mMobileItemId);

                if (v != null)
                    v.setVisibility(View.INVISIBLE);

                return true;
            }
        });

        //Log.d(logTag, "Workout contents post circuit add: ");
        //printWorkoutData();
    }

    private void printTargetLocations(){
        Log.d(logTag, "above group/position/open: " + ABOVE_VALID_GROUP + "/" + ABOVE_VALID_POSITION + "/" + ABOVE_GROUP_IS_OPEN);
        Log.d(logTag, "current group/position/open: " + CURRENT_GROUP + "/" + CURRENT_CHILD + "/" + CURRENT_GROUP_IS_OPEN);
        Log.d(logTag, "below group/position/open: " + BELOW_VALID_GROUP + "/" + BELOW_VALID_POSITION + "/" + BELOW_GROUP_IS_OPEN);
    }

    private void handleAdditionCellSwitch(){

        //Log.d(logTag, "handleAdditionCellSwitch()");
        //printTargetLocations();
        int deltaYTotal = mDownY + mLastEventY;

        View belowView = getViewForID(mBelowItemId);
        final View mobileView = getViewForID(mMobileItemId);
        View aboveView = getViewForID(mAboveItemId);
        //if(aboveView!=null)
        //Log.d(logTag, "Delt y Totes: " + deltaYTotal + " Above view top: " + aboveView.getTop() + " mAboveID: " + mAboveItemId);
        //Log.d(logTag, "Below is null " + (belowView==null));

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop()) && !WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(BELOW_VALID_GROUP);
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop()) && !WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(ABOVE_VALID_GROUP);
        //i like the use above and below thing
        if (isBelow || isAbove) {

            if(mAddingCircuitFlag){
                //Log.d(logTag, "Adding circuit...");
                addCircuit();
                //Now we transition between adding and dragging...
                return;
            }
            //Log.d(logTag, "Done adding circuit...");
            //Log.d(logTag, "Handleadditioncellswitch: is below: " + isBelow + "; is above: " + isAbove);
            //Log.d(logTag, "moving from circuit-group: " + CURRENT_GROUP + "-" + CURRENT_CHILD);
            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            final long currentId = mMobileItemId;

            //Log.d(logTag, "handleCellSwitch(), is below or above, switchItemID = " + switchItemID);
            View switchView = isBelow ? belowView : aboveView;

            //final int originalItem = getPositionForView(mobileView);
            /*
            if (switchView == null) {
                getNeighborPositions();
                updateNeighborIDsForCurrentPosition();
                return;
            }
            */
            ///old current
            //((WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter()).debugIDs();

            //debugPosition(getExpandableListPosition(getChildCount()-1+getFirstVisiblePosition()));
            //final View lastView = getChildAt(getChildCount() -1); //Used for special case animations
            //final View secondToLastView = getChildAt(getChildCount() - 2); //Used for special case animations
            //lastView.setHasTransientState(true);

            if(isBelow) moveElement(BELOW);

            if(isAbove) moveElement(ABOVE);
            //((WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter()).debugIDs();
            ///new current
            //for(Circuit c: Workout)
            //    for(Exercise e : c.getExercises())
            //       Log.d(logTag, c.getName() + " " + e.getName());

            if(mobileView!=null)mobileView.setVisibility(View.VISIBLE);

            mAdapter.notifyDataSetChanged();
            if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_CHILD)restoreListExpansion();

            final int switchViewStartTop = switchView.getTop();


            //Log.d(logTag, "handleCellSwitch(), getViewForID called on id: " + mMobileItemId);
            //testflag = true;
            //mobileView = getViewForID(mMobileItemId);
            //testflag = false;
            //Log.d(logTag, "handleCellSwitch(), MOBILE VIEW INVISIBLE!");
            //mobileView.setVisibility(View.INVISIBLE);
            //Alteration 6/5
            //mobileView.setVisibility(View.VISIBLE);
            //switchView.setVisibility(View.INVISIBLE);
            /*
            Log.d(logTag, "getNeighborPositions() called in handleAdditionCellSwitch");
            Log.d(logTag, "Current item ID: " + currentId);
            Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);

            Log.d(logTag, "AboveGroup: "
                    + ABOVE_VALID_GROUP
                    + " AboveChild "
                    + ABOVE_VALID_POSITION);

            Log.d(logTag, "Above ID: " + mAdapter.getChildId(ABOVE_VALID_GROUP, ABOVE_VALID_POSITION));
            */
            getNeighborPositions();
            updateNeighborIDsForCurrentPosition();

            final ViewTreeObserver observer = getViewTreeObserver();
            if(DRAGGED_ITEM_TYPE != ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {

                        observer.removeOnPreDrawListener(this);

                        //testflag = true;

                        printListViewData();
                        Log.d(logTag, "current id: " + currentId);
                        View v = getViewForID(currentId);

                        if (v != null)
                            v.setVisibility(View.INVISIBLE);
                        else
                            Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 1 WELV");

                        View switchView = getViewForID(switchItemID);
                        if (switchView != null) {
                            //testflag = false;
                            int switchViewNewTop = switchView.getTop();
                            int delta = switchViewStartTop - switchViewNewTop;

                            switchView.setTranslationY(delta);

                            ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
                                    View.TRANSLATION_Y, 0);
                            animator.setDuration(MOVE_DURATION);
                            animator.start();
                            //ANIMATION_FLAG = OPEN_TO_OPEN;
                        }
                        return true;
                    }
                });
            } else {
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {

                        observer.removeOnPreDrawListener(this);

                        View v = getViewForID(currentId);

                        if (v != null)
                            v.setVisibility(View.INVISIBLE);
                        else
                            Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 2 WELV");

                        return true;
                    }
                });
            }
        }
    }

    private void handleCellSwitch() {
        //Log.d(logTag, "handleCellSwitch()");
        //printTargetLocations();
        final int deltaY = mLastEventY - mDownY;
        //Log.d(logTag, "deltaY = " + mLastEventY + " - " + mDownY + " = " + deltaY);
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;
        //printWorkoutData();
        //printListViewData();
        View belowView = getViewForID(mBelowItemId);
        final View mobileView = getViewForID(mMobileItemId);//, true);
        View aboveView = getViewForID(mAboveItemId);

        //if(mobileView == null) Log.d(logTag, "mobile view is null.. mobile id = " + mMobileItemId);
        //if(mobileView == null)Log.d(logTag, "mobile null + mobile id: " +mMobileItemId);
        /*
        if(aboveView == null)
            Log.d(logTag, "above null + above id: " + mAboveItemId);
        else
            Log.d(logTag, "above top = " + aboveView.getTop());
        */
        //if(belowView == null)Log.d(logTag, "below null + below id: " + mBelowItemId);
        if (mobileView == null){
            HIDE_MOBILE_VIEW_FLAG = true;
            return;
        }
        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop()) && !WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(BELOW_VALID_GROUP);
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop()) && !WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(ABOVE_VALID_GROUP);
        //i like the use above and below thing
        if (isBelow || isAbove) {

            //Log.d(logTag, "is below: " + isBelow + "; is above: " + isAbove);
            //Log.d(logTag, "moving from circuit-group: " + CURRENT_GROUP + "-" + CURRENT_CHILD);
            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            final long currentId = mMobileItemId;

            //Log.d(logTag, "handleCellSwitch(), is below or above, switchItemID = " + switchItemID);
            View switchView = isBelow ? belowView : aboveView;

            //final int originalItem = getPositionForView(mobileView);
            /*
            if (switchView == null) {
                getNeighborPositions();
                updateNeighborIDsForCurrentPosition();
                return;
            }
            */
            ///old current
            //((WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter()).debugIDs();

            //debugPosition(getExpandableListPosition(getChildCount()-1+getFirstVisiblePosition()));
            //final View lastView = getChildAt(getChildCount() -1); //Used for special case animations
            //final View secondToLastView = getChildAt(getChildCount() - 2); //Used for special case animations
            //lastView.setHasTransientState(true);
            //printTargetLocations();
            if(isBelow) moveElement(BELOW);

            if(isAbove) moveElement(ABOVE);
            //((WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter()).debugIDs();
            ///new current
            //for(Circuit c: Workout)
            //    for(Exercise e : c.getExercises())
            //       Log.d(logTag, c.getName() + " " + e.getName());

            mobileView.setVisibility(View.VISIBLE);

            mAdapter.notifyDataSetChanged();

            if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_CHILD)restoreListExpansion();

            mDownY = mLastEventY; //if mHoverCellCurrentBounds is null we are adding an item not repositioning one

            final int switchViewStartTop = switchView.getTop();


            //Log.d(logTag, "handleCellSwitch(), getViewForID called on id: " + mMobileItemId);
            //testflag = true;
            //mobileView = getViewForID(mMobileItemId);
            //testflag = false;
            //Log.d(logTag, "handleCellSwitch(), MOBILE VIEW INVISIBLE!");
            //mobileView.setVisibility(View.INVISIBLE);
            //Alteration 6/5
            //mobileView.setVisibility(View.VISIBLE);
            //switchView.setVisibility(View.INVISIBLE);
            //Log.d(logTag, "getNeightborPositions() called in handleCellSwitch()");
            getNeighborPositions();
            updateNeighborIDsForCurrentPosition();

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {

                    observer.removeOnPreDrawListener(this);
                    //Log.d(logTag, "Current item ID: " + currentId);
                    //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);
                    /*
                    Log.d(logTag, "CurrentGroup: "
                            + CURRENT_GROUP
                            + " BelowGroup "
                            + BELOW_VALID_GROUP);
                    */

                    //testflag = true;


                    View v = getViewForID(currentId);

                    if (v != null)
                        v.setVisibility(View.INVISIBLE);
                    else
                        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 3 WELV");

                    View switchView = getViewForID(switchItemID);

                    //testflag = false;

                    mTotalOffset += deltaY;
                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    switchView.setTranslationY(delta);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
                            View.TRANSLATION_Y, 0);
                    animator.setDuration(MOVE_DURATION);
                    animator.start();
                    //ANIMATION_FLAG = OPEN_TO_OPEN;
                    return true;
                }
            });
        }
    }
    /*
    private void debugPosition(long explstpos){
        int child = getPackedPositionChild(explstpos);
        int group = getPackedPositionGroup(explstpos);
        Log.d(logTag, "DEBUG, CHILD: " + child + " GROUP: " + group);
    }
    */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        }
        /*
        if(mLastView !=null){
            canvas.translate(0, mLastView.getTop());
            mLastView.draw(canvas);
            canvas.translate(0, -mLastView.getTop());
        }

        if(mAnimating){
            int animationOffset = (int)(mAnimationVelocity * (System.currentTimeMillis() - mAnimationStartTime));
            Log.d(logTag, "Animation time = " + (System.currentTimeMillis() - mAnimationStartTime) + "Animation Offset" + animationOffset + " Anim velocity" + mAnimationVelocity);
            mLastView.setTop(mLastViewCoords[0] - animationOffset);
            mLastView.setBottom(mLastViewCoords[1] - animationOffset);
            invalidate();
        }
        */
    }

    private void moveElement(int direction) {
        //Log.d(logTag, "MOVE CALLED.");
        int TARGET_GROUP;
        int TARGET_POSITION;
        boolean TARGET_GROUP_IS_OPEN;

        if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_CHILD) {
            Exercise temp = Workout.get(CURRENT_GROUP).getExercise(CURRENT_CHILD);

            switch (direction) {
                case ABOVE:

                    TARGET_GROUP = ABOVE_VALID_GROUP;
                    TARGET_POSITION = ABOVE_VALID_POSITION;
                    TARGET_GROUP_IS_OPEN = ABOVE_GROUP_IS_OPEN;

                    if (TARGET_GROUP_IS_OPEN) {
                        //Log.d(logTag, "Moving in open group.");
                        Workout.get(TARGET_GROUP).add(TARGET_POSITION, temp);
                        //Log.d(logTag, "Current group open status: " + CURRENT_GROUP_IS_OPEN);
                        if (CURRENT_GROUP_IS_OPEN) {
                            //target open, current open
                            Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD + 1);
                        } else {
                            //target open, current closed

                            mAdapter.removeGroup(CURRENT_GROUP);
                            //ANIMATION_FLAG = FROM_CLOSED_UP_TO_OPEN;
                        }
                        CURRENT_GROUP_IS_OPEN = true;//TARGET_GROUP_IS_OPEN;
                        CURRENT_GROUP = TARGET_GROUP;
                        CURRENT_CHILD = TARGET_POSITION;
                    } else {
                        //MOVING TO NEW CLOSED GROUP
                        //\\\WorkoutData.get(mContext).placeClosedCircuitWithExercise(TARGET_GROUP, temp);
                        //Log.d(logTag, "TG: " + TARGET_GROUP);
                        Circuit c = WorkoutData.getClosedCircuit();
                        mAdapter.addGroup(TARGET_GROUP, c);
                        c.add(temp);

                        if (CURRENT_GROUP_IS_OPEN) {
                            //target closed, current open
                            Workout.get(CURRENT_GROUP + 1).removeExercise(CURRENT_CHILD);
                            CURRENT_GROUP_IS_OPEN = false;
                            CURRENT_GROUP = TARGET_GROUP;
                            CURRENT_CHILD = 0;
                            //ANIMATION_FLAG = FROM_OPEN_UP_TO_CLOSED;
                        } else {
                            //target closed, current closed
                            Workout.get(CURRENT_GROUP + 1).removeExercise(CURRENT_CHILD);
                            //\\\Workout.remove(CURRENT_GROUP + 1);
                            mAdapter.removeGroup(CURRENT_GROUP + 1);
                            CURRENT_GROUP_IS_OPEN = false;
                            CURRENT_GROUP = TARGET_GROUP;
                            CURRENT_CHILD = 0;
                        }
                    }

                    break;
                case BELOW:

                    TARGET_GROUP = BELOW_VALID_GROUP;
                    TARGET_POSITION = BELOW_VALID_POSITION;
                    TARGET_GROUP_IS_OPEN = BELOW_GROUP_IS_OPEN;

                    if (TARGET_GROUP_IS_OPEN) {
                        //Target open

                        if (CURRENT_GROUP_IS_OPEN) {
                            //Target open, current open
                            Workout.get(TARGET_GROUP).add(TARGET_POSITION + 1, temp);
                            Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD);
                            CURRENT_CHILD = TARGET_POSITION;
                            CURRENT_GROUP = TARGET_GROUP;
                            CURRENT_GROUP_IS_OPEN = true;
                        } else {
                            //target open, current closed
                            if (Workout.get(TARGET_GROUP).isExpanded()) {
                                //open target group is expanded
                                Workout.get(TARGET_GROUP).add(0, temp);
                                Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD);
                                //\\\Workout.remove(CURRENT_GROUP);
                                mAdapter.removeGroup(CURRENT_GROUP);
                                CURRENT_GROUP_IS_OPEN = true;
                                //ANIMATION_FLAG = FROM_CLOSED_DOWN_TO_OPEN;
                            } else {
                                //open target group is collapsed
                                //\\\WorkoutData.get(mContext).placeClosedCircuitWithExercise(TARGET_GROUP + 1, temp);
                                Circuit c = WorkoutData.getClosedCircuit();
                                mAdapter.addGroup(TARGET_GROUP + 1, c);
                                c.add(temp);

                                Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD);
                                //\\\Workout.remove(CURRENT_GROUP);
                                mAdapter.removeGroup(CURRENT_GROUP);
                                CURRENT_GROUP_IS_OPEN = false;
                            /*
                            Log.d(logTag, "CLOSED CIRCUIT PLACED AT POSITION: "
                                    +(TARGET_GROUP + 1)
                                    +" ITEM ID IS: "
                                    +Workout.get(TARGET_GROUP + 1).getExercise(0).getStableID());

                            for(Circuit c : Workout){
                                Log.d(logTag, c.getName());
                                for(Exercise e : c.getExercises())
                                    Log.d(logTag, e.getName());
                            }
                            */
                                CURRENT_GROUP = TARGET_GROUP;
                                CURRENT_CHILD = 0;
                            }
                        }
                    } else {
                        //target group closed.
                        //Log.d(logTag, "moveElement() - moving from: " + CURRENT_GROUP + " - child:" + CURRENT_CHILD);
                        //Log.d(logTag, "moveElement() - moving to group: " + TARGET_GROUP + " - child:" + TARGET_POSITION);
                        if (CURRENT_GROUP_IS_OPEN) {
                            //\\\WorkoutData.get(mContext).placeClosedCircuitWithExercise(TARGET_GROUP, temp);
                            Circuit c = WorkoutData.getClosedCircuit();
                            mAdapter.addGroup(TARGET_GROUP, c);
                            c.add(temp);

                            Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD);
                            //open to closed
                            //ANIMATION_FLAG = FROM_OPEN_DOWN_TO_CLOSED;
                        } else {
                            //\\\WorkoutData.get(mContext).placeClosedCircuitWithExercise(TARGET_GROUP + 1, temp);
                            Circuit c = WorkoutData.getClosedCircuit();
                            mAdapter.addGroup(TARGET_GROUP + 1, c);
                            c.add(temp);

                            if(!WorkoutData.get(mContext).isCircuitAtPositionPlaceholder(CURRENT_GROUP)) {
                                //Log.d(logTag, "CG = " + CURRENT_GROUP + " CC = " + CURRENT_CHILD);
                                Workout.get(CURRENT_GROUP).removeExercise(CURRENT_CHILD);
                                mAdapter.removeGroup(CURRENT_GROUP);
                            }
                            //closed to closed
                            //Log.d(logTag, "CLOSED TO CLOSED");
                            //\\\Workout.remove(CURRENT_GROUP);
                        }
                        CURRENT_GROUP_IS_OPEN = false;
                        CURRENT_GROUP = TARGET_GROUP;
                        CURRENT_CHILD = TARGET_POSITION;
                        //Log.d(logTag, "CURRENT_GROUP: " + CURRENT_GROUP + " CURRENT_CHILD: "+ CURRENT_CHILD);
                    }

                    break;
            }
        } else if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_GROUP){
            Circuit temp = Workout.get(CURRENT_GROUP);
            switch (direction) {
                case ABOVE:
                    //adapter.removeGroup(CURRENT_GROUP);
                    //adapter.addGroup(ABOVE_VALID_GROUP, temp);
                    Workout.remove(CURRENT_GROUP);
                    Workout.add(ABOVE_VALID_GROUP, temp);
                    CURRENT_GROUP = CURRENT_GROUP - 1;
                    break;
                case BELOW:
                    //adapter.removeGroup(CURRENT_GROUP);
                    //adapter.addGroup(BELOW_VALID_GROUP, temp);
                    Workout.remove(CURRENT_GROUP);
                    Workout.add(BELOW_VALID_GROUP, temp);
                    CURRENT_GROUP = CURRENT_GROUP + 1;
                    break;
            }
        }
        //Log.d(logTag, "moveElement() CURRENT POSITIONS SET - CURRENT_GROUP: " + CURRENT_GROUP + " - CURRENT_CHILD: " + CURRENT_CHILD);
        //for(Circuit c: Workout)
         //   for(Exercise e : c.getExercises())
         //       Log.d(logTag, "IN MOVE ELEMENT - " + c.getName() + " " + e.getName());
        //Log.d(logTag, "moveElement() BELOW POSITIONS SET - BELOW_GROUP: " + BELOW_VALID_GROUP + " - BELOW_CHILD: " + BELOW_VALID_POSITION);
    }

    private BitmapDrawable getAndAddHoverView(View v) {

        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();
        //Log.d(logTag, "getandaddhoverview - w: " + w + "; h: " + h + "; top: " + top + "; left: " + left);
        Bitmap b = getBitmapWithBorder(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }

    /** Draws a black border over the screenshot of the view passed in. */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(Color.BLACK);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void updateNeighborIDsForCurrentPosition() {
        //above id
        if (CURRENT_GROUP == INVALID_POSITION) return;

        if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_CHILD) {

            if (CURRENT_CHILD == INVALID_POSITION) {
                //group header
                mAboveItemId = INVALID_ID;
                mBelowItemId = INVALID_ID;
                return;
            }

            if (CURRENT_GROUP_IS_OPEN) {
                //current group is open
                if (CURRENT_CHILD == 0) {
                    //switch view id is group header id
                    //\\\mAboveItemId = Workout.get(CURRENT_GROUP).getStableID();
                    //Log.d(logTag, "Group ID call 3");
                    mAboveItemId = mAdapter.getGroupId(CURRENT_GROUP);
                } else {
                    //regular group scenario use above
                    //\\\mAboveItemId = Workout.get(ABOVE_VALID_GROUP).getExercise(ABOVE_VALID_POSITION).getStableID();
                    mAboveItemId = mAdapter.getChildId(ABOVE_VALID_GROUP, ABOVE_VALID_POSITION);
                }
            } else {
                //current group is closed
                //if top group is invalid (Current is first)
                if (ABOVE_VALID_GROUP == INVALID_POSITION) {
                    mAboveItemId = INVALID_ID;
                } else {
                    //swap with above valid group
                    Circuit c = Workout.get(ABOVE_VALID_GROUP);
                    if (c.isOpen() && !c.isExpanded()) {
                        //group header id
                        //\\\mAboveItemId = c.getStableID();
                        //Log.d(logTag, "Group ID call 4");
                        mAboveItemId = mAdapter.getGroupId(ABOVE_VALID_GROUP);
                    } else {
                        //child id
                        //\\\mAboveItemId = c.getExercise(ABOVE_VALID_POSITION).getStableID();
                        mAboveItemId = mAdapter.getChildId(ABOVE_VALID_GROUP, ABOVE_VALID_POSITION);
                    }
                }
            }
            //below id
            if (!CURRENT_GROUP_IS_OPEN) {
                //closed group
                if (BELOW_GROUP_IS_OPEN) {
                    //switch item is group header
                    //\\\mBelowItemId = Workout.get(BELOW_VALID_GROUP).getStableID();
                    //Log.d(logTag, "Group ID call 5");
                    mBelowItemId = mAdapter.getGroupId(BELOW_VALID_GROUP);
                } else {
                    //below group is closed get next item
                    //\\\mBelowItemId = Workout.get(BELOW_VALID_GROUP).getExercise(BELOW_VALID_POSITION).getStableID();
                    mBelowItemId = mAdapter.getChildId(BELOW_VALID_GROUP, BELOW_VALID_POSITION);
                }

            } else {
                //from open circuit
                if (!BELOW_GROUP_IS_OPEN) {
                    //to closed circuit.  return final item
                    //\\\mBelowItemId = Workout.get(CURRENT_GROUP).getExercise(CURRENT_CHILD + 1).getStableID();
                    mBelowItemId = mAdapter.getChildId(CURRENT_GROUP, CURRENT_CHILD + 1);
                } else {
                    //otherwise return next
                    //\\\mBelowItemId = Workout.get(BELOW_VALID_GROUP).getExercise(BELOW_VALID_POSITION).getStableID();
                    mBelowItemId = mAdapter.getChildId(BELOW_VALID_GROUP, BELOW_VALID_POSITION);
                }
            }
        } else if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_GROUP){
            //ABOVE ITEM ID
            if(ABOVE_VALID_POSITION == INVALID_POSITION){
                //Log.d(logTag, "Group ID call 6");
                mAboveItemId = mAdapter.getGroupId(ABOVE_VALID_GROUP);
            } else {
                mAboveItemId = mAdapter.getChildId(ABOVE_VALID_GROUP, ABOVE_VALID_POSITION);
            }

            //BELOW ITEM ID
            if(BELOW_VALID_POSITION == INVALID_POSITION){
                //Log.d(logTag, "Group ID call 7");
                mBelowItemId = mAdapter.getGroupId(BELOW_VALID_GROUP);
            } else {
                mBelowItemId = mAdapter.getChildId(BELOW_VALID_GROUP, BELOW_VALID_POSITION);
            }
        }
        //Log.d(logTag, "<<<<<<<<<<<<<<<ID VALUES FROM GET NEIGHBORIDS>>>>>>>>>>>>>>>>>>.  BELOW: " + mBelowItemId + " ABOVE: " + mAboveItemId + " CURRENT: " + mMobileItemId);
    }
    /*
    public View getViewForID (long itemID){
        //Log.d(logTag, "getViewForId() looking for id: " + itemID);
        WorkspaceExpandableListAdapterMKIII adapter =
                (WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter();

        int firstVisible = getFirstVisiblePosition();
        for(int i = 0; i < getChildCount(); i++) {

            Long expLstPos = getExpandableListPosition(firstVisible + i);
            int group = getPackedPositionGroup(expLstPos);
            int type = getPackedPositionType(expLstPos);
            int child = getPackedPositionChild(expLstPos);
            //we're gonna get the position of the list item in exp list speak
            //then check that id vs the id of what we think the id should be
            //if(testflag)Log.d(logTag, "getViewForID - looking for id: " + itemID);
            //Log.d(logTag, "getViewForID - i: " + i);
            //Log.d(logTag, "getViewForID - explstpos: " + expLstPos);
            //if(testflag)Log.d(logTag, "getViewForID - LOOKING FOR VIEW AT GROUP: " + group + " + CHILD: " + child);
            if (type == PACKED_POSITION_TYPE_GROUP){
                //Log.d(logTag, "getViewForID - type is group");
                //Log.d(logTag, "getViewForID, group: " + group + " found, id == " + Workout.get(group).getStableID());
                //\\\if(Workout.get(group).getStableID() == itemID) {
                if(adapter.getGroupId(group) == itemID){
                    //if(testflag)Log.d(logTag, "FOUND GROUP! ID: " + itemID);
                    return getChildAt(i);
                }

            } else if (type == PACKED_POSITION_TYPE_CHILD){
                //Log.d(logTag, "getViewForID, group/child/name " + group +"/" + child + "/" + Workout.get(group).getExercise(child).getName() + " found, id == " + Workout.get(group).getExercise(child).getStableID() + " - i : " + i);
                //\\\if(Workout.get(group).getExercise(child).getStableID() == itemID) {
                if(adapter.getChildId(group, child) == itemID){
                    //if(testflag)Log.d(logTag, "FOUND CHILD! ID: " + itemID);

                    //if (testflag) return getChildAt(i - 1);
                    return getChildAt(i);
                }
            }
        }
        //Log.d(logTag, "getViewForId() returning null!!!");
        return null;
    }
    */
    //THIS METHOD SETS NEIGHBOR POSITIONS FOR A GROUP OR CHILD POSITION
    private void getNeighborPositions(){
        if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_CHILD) {
            //Log.d(logTag, "nPos child");
            boolean curGrpLast;

            curGrpLast = CURRENT_GROUP == Workout.size() - 1;

            //DEFAULT VALS

            ABOVE_VALID_GROUP = INVALID_POSITION;
            ABOVE_VALID_POSITION = INVALID_POSITION;
            ABOVE_GROUP_IS_OPEN = false;

            BELOW_VALID_GROUP = INVALID_POSITION;
            BELOW_VALID_POSITION = INVALID_POSITION;
            BELOW_GROUP_IS_OPEN = false;

            //LAST_GROUP = CURRENT_GROUP;
            //LAST_CHILD = CURRENT_CHILD;
            //LAST_GROUP_IS_OPEN = CURRENT_GROUP_IS_OPEN;
            //*****
            //
            //CODE TO GET NEXT POSITION OF ITEM
            //
            //****

            //CIRCUIT IS OPEN?
            if (CURRENT_GROUP_IS_OPEN) {
                Circuit curCircuit = Workout.get(CURRENT_GROUP);
                //Next item is last (name eq test)
                if (curCircuit.getExercise(CURRENT_CHILD + 1).getName().equals("test")) {
                    //next pos will be new closed circuit
                    BELOW_GROUP_IS_OPEN = false;
                    BELOW_VALID_POSITION = 0;
                    BELOW_VALID_GROUP = CURRENT_GROUP + 1;
                } else {
                    //next item is neighbor
                    BELOW_GROUP_IS_OPEN = true;
                    BELOW_VALID_POSITION = CURRENT_CHILD + 1;
                    BELOW_VALID_GROUP = CURRENT_GROUP;
                }
            } else {
                //in closed circuit
                //is last circuit?
                if (curGrpLast) {
                    //at end, do nothing
                    BELOW_VALID_GROUP = INVALID_POSITION;
                    BELOW_VALID_POSITION = INVALID_POSITION;
                } else {
                    //not at end
                    if (Workout.get(CURRENT_GROUP + 1).isOpen()) {
                        //Next circuit open
                        //add to beginning
                        BELOW_VALID_GROUP = CURRENT_GROUP + 1;
                        BELOW_VALID_POSITION = 0;
                        BELOW_GROUP_IS_OPEN = true;


                    } else {
                        //next circuit is closed
                        BELOW_VALID_GROUP = CURRENT_GROUP + 1;
                        BELOW_VALID_POSITION = 0;
                        BELOW_GROUP_IS_OPEN = false;
                    }
                }
            }

            //*****
            //
            //CODE TO GET PREV POSITION OF ITEM
            //
            //****
            //if item is first
            if (CURRENT_CHILD == 0) {
                if (CURRENT_GROUP_IS_OPEN) {
                    //first item in open circuit
                    //  make new closed
                    ABOVE_VALID_GROUP = CURRENT_GROUP;
                    ABOVE_VALID_POSITION = 0;
                    ABOVE_GROUP_IS_OPEN = false;
                } else {
                    //item in closed circuit
                    if (CURRENT_GROUP == 0) {
                        //  do nothing if first circuit
                        ABOVE_VALID_POSITION = INVALID_POSITION;
                        ABOVE_VALID_GROUP = INVALID_POSITION;
                    } else {
                        Circuit c = Workout.get(CURRENT_GROUP - 1);
                        if (c.isOpen()) {
                            //  add to bottom of prev open
                            if (c.isExpanded()) {
                                //prev group is expanded
                                ABOVE_VALID_GROUP = CURRENT_GROUP - 1;
                                ABOVE_VALID_POSITION = Workout.get(CURRENT_GROUP - 1).getSize() - 1;
                                ABOVE_GROUP_IS_OPEN = true;
                            } else {
                                //prev group is not expanded

                                ABOVE_VALID_GROUP = CURRENT_GROUP - 1;
                                ABOVE_VALID_POSITION = 0;
                                ABOVE_GROUP_IS_OPEN = false;
                            }
                        } else {
                            //  swap with prev closed
                            ABOVE_VALID_GROUP = CURRENT_GROUP - 1;
                            ABOVE_VALID_POSITION = 0;
                            ABOVE_GROUP_IS_OPEN = false;
                        }
                    }
                }
            } else {
                //swap with prev child
                ABOVE_VALID_GROUP = CURRENT_GROUP;
                ABOVE_VALID_POSITION = CURRENT_CHILD - 1;
                ABOVE_GROUP_IS_OPEN = true;
            }
            //Log.d(logTag, "above group/position/open: " + ABOVE_VALID_GROUP + "/" + ABOVE_VALID_POSITION + "/" + ABOVE_GROUP_IS_OPEN);
            //Log.d(logTag, "current group/position/open: " + CURRENT_GROUP + "/" + CURRENT_CHILD + "/" + CURRENT_GROUP_IS_OPEN);
            //Log.d(logTag, "below group/position/open: " + BELOW_VALID_GROUP + "/" + BELOW_VALID_POSITION + "/" + BELOW_GROUP_IS_OPEN);
        } else if(DRAGGED_ITEM_TYPE == PACKED_POSITION_TYPE_GROUP){
            boolean curGroupFirst = CURRENT_GROUP == 0;
            boolean curGroupLast = CURRENT_GROUP == Workout.size() - 1;

            //get above valid positions
            if(curGroupFirst){
                ABOVE_VALID_GROUP = INVALID_POSITION;
                ABOVE_VALID_POSITION = INVALID_POSITION;
            } else {
                ABOVE_VALID_GROUP = CURRENT_GROUP - 1;

                if(Workout.get(ABOVE_VALID_GROUP).isOpen()){
                    ABOVE_VALID_POSITION = INVALID_POSITION;
                    ABOVE_GROUP_IS_OPEN = true;
                } else {
                    ABOVE_VALID_POSITION = 0;
                    ABOVE_GROUP_IS_OPEN = false;
                }
            }

            if(curGroupLast){
                BELOW_VALID_GROUP = INVALID_POSITION;
                BELOW_VALID_POSITION = INVALID_POSITION;
            } else {
                BELOW_VALID_GROUP = CURRENT_GROUP + 1;

                if(Workout.get(BELOW_VALID_GROUP).isOpen()){
                    BELOW_VALID_POSITION = INVALID_POSITION;
                    BELOW_GROUP_IS_OPEN = true;
                } else {
                    BELOW_VALID_POSITION = 0;
                    BELOW_GROUP_IS_OPEN = false;
                }
            }
        }
    }

    private void touchEventsEnded () {
        /*
        for(Circuit c : Workout){
            Log.d(logTag, c.getName());
            for(Exercise e : c.getExercises())
                Log.d(logTag, e.getName());
        }
        */
        Log.d(logTag, "touchEventsEnded()");
        final View mobileView = getViewForID(mMobileItemId);
        if (mDragInProgress|| mIsWaitingForScrollFinish) {
            mDragInProgress = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.

            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            if(mHoverCellCurrentBounds!=null) {

                int top;
                if(mobileView == null){
                    top = mHoverCellCurrentBounds.top;
                } else {
                    top = mobileView.getTop();
                }

                mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, top);

                ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds",
                        sBoundEvaluator, mHoverCellCurrentBounds);
                hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        invalidate();
                    }
                });
                hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setEnabled(false);
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Log.d(logTag, "###################################DRAGGED ITEM MADE VISIBLE############################");
                        //TEST CODE

                        View view = getViewForID(mMobileItemId);
                        if (view!=null) view.setVisibility(VISIBLE);

                        //END TEST CODE
                        //mobileView.setVisibility(VISIBLE);
                        mDragMode = false;
                        mHoverCell = null;
                        mHoverCellCurrentBounds = null;
                        setEnabled(true);

                        if (mExpansionMemory != null) {
                            /*
                            for (int i = 0; i < mExpansionMemory.length; i++) {
                                if (mExpansionMemory[i]) expandGroup(i);
                            }
                            mExpansionMemory = null;
                            mAdapter.toggleListPadding(false);
                            setSelectionFromTop(getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)), top);
                            */
                            //todo
                            for (int i = 0; i < Workout.size(); i++){
                                if(mExpansionMemory.get(Workout.get(i))){
                                    expandGroup(i);
                                } else {
                                    collapseGroup(i);
                                }
                            }
                            mExpansionMemory = null;
                            mAdapter.toggleListPadding(false);
                            if(mobileView != null)
                                setSelectionFromTop(getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)), mobileView.getTop());
                            else
                                setSelection(getFlatListPosition(getPackedPositionForGroup(CURRENT_GROUP)));
                        }

                        mAboveItemId = INVALID_ID;
                        mMobileItemId = INVALID_ID;
                        mBelowItemId = INVALID_ID;
                        invalidate();
                    }
                });
                hoverViewAnimator.start();
            }
        } else {
            touchEventsCancelled();
        }
    }
    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled () {
       // Log.d(logTag, "touchEventsCancelled() mobileItemID: " + mMobileItemId);

        View mobileView = getViewForID(mMobileItemId);
        if (mDragMode) {
            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);

            mHoverCell = null;
            invalidate();
        }
        mDragMode = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }

    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };

    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }

    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            /*
             *  If HIDE_MOBILE_VIEW_FLAG is true, we need to set the mobile view to invisible at the
             *  earliest possible opportunity.
             */
            if(HIDE_MOBILE_VIEW_FLAG){
                View mobileView = getViewForID(mMobileItemId);
                if(mobileView != null){
                    mobileView.setVisibility(View.INVISIBLE);
                    HIDE_MOBILE_VIEW_FLAG = false;
                }
            }
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }
    /*
    private boolean checkIfValidPosition(int startPosition) {
        boolean okayToDrag = false;
        int groupPosition;
        int childPosition;
        Exercise e;
        Circuit c;
        if (startPosition != INVALID_POSITION) {
            if (getPackedPositionType(getExpandableListPosition(startPosition)) == PACKED_POSITION_TYPE_CHILD) {
                childPosition = getPackedPositionChild(getExpandableListPosition(startPosition));
                groupPosition = getPackedPositionGroup(getExpandableListPosition(startPosition));
                e = Workout.get(groupPosition).getExercise(childPosition);
                if (!e.getName().equals("test")) {
                    okayToDrag = true;
                }
            } else if (getPackedPositionType(getExpandableListPosition(startPosition)) == PACKED_POSITION_TYPE_GROUP) {
                groupPosition = getPackedPositionGroup(getExpandableListPosition(startPosition));
                c = Workout.get(groupPosition);
                int workoutLength = Workout.size();
                if (c.isOpen() || groupPosition != (workoutLength - 1)) {
                    okayToDrag = true;
                }
            }
        }
        return okayToDrag;
    }
    */
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener () {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
                    : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
                    : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            mScrollState = scrollState;
            isScrollCompleted();
        }

        /**
         * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview
         * is in a state of scrolling invoked by the hover cell being outside the bounds
         * of the listview, then this scrolling event is continued. Secondly, if the hover
         * cell has already been released, this invokes the animation for the hover cell
         * to return to its correct position after the listview has entered an idle scroll
         * state.
         */
        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (mDragMode && mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mDragMode && mMobileItemId != INVALID_ID) {
                    updateNeighborIDsForCurrentPosition();
                    handleCellSwitch();
                }
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mDragMode && mMobileItemId != INVALID_ID) {
                    updateNeighborIDsForCurrentPosition();
                    handleCellSwitch();
                }
            }
        }
    };

    public void  restoreListExpansion(){
        for (int i = 0; i < Workout.size(); i++){
            if(Workout.get(i).isExpanded()){
                expandGroup(i);
                //Log.d(logTag, "Expanding group i: " + i);
            } else {
                collapseGroup(i);
                //Log.d(logTag, "Collapsing group i: " + i);
            }
        }
    }
    /*
    public void removeCheckedItems() {

        int first = getFirstVisiblePosition();
        int last = getLastVisiblePosition();
        int viewsVisible = last - first;
        int childPosition;
        int groupPosition;
        ArrayList<View> viewsToRemove = new ArrayList<>();
        Exercise e;

        int startTime = 0;
        //NOTE GET CHILD RELATIVE
        //EXPLISTPOS OVERALL
        for (int i = 0; i <= viewsVisible + 1; i++) {
            if (getPackedPositionType(getExpandableListPosition(i + first)) == PACKED_POSITION_TYPE_CHILD) {

                //child
                childPosition = getPackedPositionChild(getExpandableListPosition(i + first));
                groupPosition = getPackedPositionGroup(getExpandableListPosition(i + first));
                e = Workout.get(groupPosition).getExercise(childPosition);
                if (e.isSaveToHistorySet()) {
                    //do animation

                    if (getChildAt(i) != null) {
                        viewsToRemove.add(0, getChildAt(i));
                    }
                }
            }
        }

        int length = viewsToRemove.size();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        for (int j = 0; j <= length-1; j++) {
            View viewToRemove = viewsToRemove.get(j);

            ObjectAnimator anim = ObjectAnimator.ofFloat(viewToRemove, "translationX", 0, -SCREENWIDTH);
            anim.setDuration(150);
            animators.add(anim);



            if (j == (length - 1)) {
                //we're animating at least one thing
                setEnabled(false);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        WorkoutData.get(mContext).clearCheckedExercises();
                        ((WorkspaceExpandableListAdapterMKIII) getExpandableListAdapter()).notifyDataSetChanged();
                        restoreListExpansion();
                        setEnabled(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                startTime = startTime + 100;
            }
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animators);
            set.start();
        }
        }
    */

    //
    }
/*
 switch(ANIMATION_FLAG){
                case OPEN_TO_OPEN:
                    Log.d(logTag, "Open to open");
                    observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {

                            observer.removeOnPreDrawListener(this);
                            //Log.d(logTag, "Current item ID: " + currentId);
                            //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);

    //testflag = true;


View v = getViewForID(currentId);

if (v != null)
        v.setVisibility(View.INVISIBLE);
        else
        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 414 WELV");

        View switchView = getViewForID(switchItemID);

        //testflag = false;

        mTotalOffset += deltaY;
        int switchViewNewTop = switchView.getTop();
        int delta = switchViewStartTop - switchViewNewTop;

        switchView.setTranslationY(delta);

        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
        View.TRANSLATION_Y, 0);
        animator.setDuration(MOVE_DURATION);
        animator.start();
        //ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        });

        break;
        case FROM_CLOSED_UP_TO_OPEN:
        Log.d(logTag, "FROM CLOSED UP TO OPEN");
        mViewPosition = getChildAt(getPositionForView(getViewForID(currentId)) - getFirstVisiblePosition() + 1).getTop();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
public boolean onPreDraw() {

        observer.removeOnPreDrawListener(this);
        //Log.d(logTag, "Current item ID: " + currentId);
        //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);

        //testflag = true;


        View v = getViewForID(currentId);

        if (v != null)
        v.setVisibility(View.INVISIBLE);
        else
        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 414 WELV");

        View switchView = getViewForID(switchItemID);

        //testflag = false;

        mTotalOffset += deltaY;

        int switchViewNewTop = switchView.getTop();
        int delta = switchViewStartTop - switchViewNewTop;

        switchView.setTranslationY(delta);

        ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(switchView,
        View.TRANSLATION_Y, 0);
        footerAnimator.setDuration(MOVE_DURATION);
        //animator.start();

        //get flat position for view

        Log.d(logTag, "" + (getPositionForView(v) - getFirstVisiblePosition()));


        //Get position for view returns flat list position for a child view in the dataset
        //This gives us the position of the view as visibile on the listview
        int childListPosition = getPositionForView(v) - getFirstVisiblePosition();

        //
        if(childListPosition + 2 >= getChildCount()){
        Log.d(logTag, "FROM CLOSED UP TO OPEN: do not need to fire special animation");
        footerAnimator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        //Special animation code

        AnimatorSet set = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();

        //Get offset
        int startOffset = childListPosition + 2;
        View firstViewToAnimate = getChildAt(startOffset);
        int offset = mViewPosition - firstViewToAnimate.getTop();
        //Log.d(logTag, "offset: " + offset);
        //Log.d(logTag, "Start index: " + startOffset);

        for(int i = startOffset; i < getChildCount(); i++){
        //Log.d(logTag, "i : " + i);
        View viewToAnimate = getChildAt(i);
        viewToAnimate.setTranslationY(offset);
        animators.add(ObjectAnimator.ofFloat(viewToAnimate,
        View.TRANSLATION_Y, 0));
        }

        set.playTogether(animators);
        set.start();
        footerAnimator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        });
        break;
        case FROM_CLOSED_DOWN_TO_OPEN:
        Log.d(logTag, "FROM CLOSED DOWN TO OPEN");
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
public boolean onPreDraw() {

        observer.removeOnPreDrawListener(this);
        //Log.d(logTag, "Current item ID: " + currentId);
        //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);
        //testflag = true;


        View v = getViewForID(currentId);

        if (v != null)
        v.setVisibility(View.INVISIBLE);
        else
        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 414 WELV");

        View switchView = getViewForID(switchItemID);

        //testflag = false;

        mTotalOffset += deltaY;
        int switchViewNewTop = switchView.getTop();
        int delta = switchViewStartTop - switchViewNewTop;

        switchView.setTranslationY(delta);

        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
        View.TRANSLATION_Y, 0);
        animator.setDuration(MOVE_DURATION);
        animator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        });

        break;
        case FROM_OPEN_UP_TO_CLOSED:
        //Log.d(logTag, "FROM OPEN UP TO CLOSED");
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
public boolean onPreDraw() {

        observer.removeOnPreDrawListener(this);
        //Log.d(logTag, "Current item ID: " + currentId);
        //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);
        //testflag = true;


        View v = getViewForID(currentId);

        if (v != null)
        v.setVisibility(View.INVISIBLE);
        else
        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 414 WELV");

        View switchView = getViewForID(switchItemID);

        //testflag = false;

        mTotalOffset += deltaY;
        int switchViewNewTop = switchView.getTop();
        int delta = switchViewStartTop - switchViewNewTop;

        switchView.setTranslationY(delta);

        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
        View.TRANSLATION_Y, 0);
        animator.setDuration(MOVE_DURATION);
        animator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        });

        break;
        case FROM_OPEN_DOWN_TO_CLOSED:
        Log.d(logTag, "FROM OPEN DOWN TO CLOSED");
        int currentViewPosition = getPositionForView(getViewForID(currentId)) - getFirstVisiblePosition();

//debug
//debugPosition(getExpandableListPosition(getPositionForView(getViewForID(currentId))));
//end debug
final View referenceView = getChildAt(currentViewPosition);
        //debugPosition(getExpandableListPosition(currentViewPosition + getFirstVisiblePosition()));
        mViewPosition = referenceView.getTop();
        //Log.d(logTag, "View position: " + mViewPosition);
        //mLastView = getChildAt(getChildCount() - 1);

        //debugPosition(getExpandableListPosition(getChildCount()-1+getFirstVisiblePosition()));
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
public boolean onPreDraw() {

        observer.removeOnPreDrawListener(this);
        //Log.d(logTag, "Current item ID: " + currentId);
        //Log.d(logTag, "PreDraw Switch item ID: " + switchItemID);
        //testflag = true;

        if (lastView.getParent() == null) {
        Log.d(logTag, "Need to add leftover view");
        mLastView = lastView;
        mSecondToLastView = secondToLastView;
        mAnimating = true;
        mAnimationStartTime = System.currentTimeMillis();
        mLastViewCoords = new int[]{mLastView.getTop(), mLastView.getBottom()};
        }

        View v = getViewForID(currentId);

        if (v != null)
        v.setVisibility(View.INVISIBLE);
        else
        Log.d(logTag, "VIEW IS NULL, TERRIBLE THINGS, LINE 414 WELV");

        View switchView = getViewForID(switchItemID);

        //testflag = false;

        mTotalOffset += deltaY;
        int switchViewNewTop = switchView.getTop();
        int delta = switchViewStartTop - switchViewNewTop;

        switchView.setTranslationY(delta);

        ObjectAnimator footerAnimator= ObjectAnimator.ofFloat(switchView,
        View.TRANSLATION_Y, 0);
        footerAnimator.setDuration(MOVE_DURATION);
        //Get position for view returns flat list position for a child view in the dataset
        //This gives us the position of the view as visibile on the listview
        int childListPosition = getPositionForView(v) - getFirstVisiblePosition();

        //
        if(childListPosition + 1 >= getChildCount()){
        Log.d(logTag, "FROM CLOSED UP TO OPEN: do not need to fire special animation");
        footerAnimator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        //Special animation code

        AnimatorSet set = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();

        //Get offset
        //debugPosition(getExpandableListPosition(childListPosition + 1 + getFirstVisiblePosition()));
        int startOffset = childListPosition + 1;
        View firstViewToAnimate = getChildAt(startOffset);
        int offset = mViewPosition - firstViewToAnimate.getTop();

        Log.d(logTag, "New top" + firstViewToAnimate.getTop());
        //Log.d(logTag, "offset: " + offset);
        //Log.d(logTag, "Start index: " + startOffset);

        for(int i = startOffset; i < getChildCount(); i++){
        //Log.d(logTag, "i : " + i);
        View viewToAnimate = getChildAt(i);
        viewToAnimate.setTranslationY(offset);
        animators.add(ObjectAnimator.ofFloat(viewToAnimate,
        View.TRANSLATION_Y, 0));
        }

        if(mLastView != null){
        mPixelDistanceToAnimate = offset;
        mAnimationVelocity = mPixelDistanceToAnimate / MOVE_DURATION;
        Log.d(logTag, "Anim velocity = " + mAnimationVelocity + " = " + mPixelDistanceToAnimate + " / " + MOVE_DURATION);

                                //Animator leftoverAnimator = ObjectAnimator.ofFloat(mLastView, View.TRANSLATION_Y, offset);
                                //leftoverAnimator.addListener(new AnimatorListenerAdapter() {
                                //    @Override
                                //    public void onAnimationEnd(Animator animation) {
                                //        super.onAnimationEnd(animation);
                                //        mLastView.setHasTransientState(false);
                                //        mLastView.setTranslationY(0f);
                                //        mLastView = null;
                                //        mSecondToLastView = null;
                                //        mAnimating = false;
                                //        Log.d(logTag, "ON ANIMATION END CALLED");
                                //    }
                                //});
                                //animators.add(leftoverAnimator);

        }

        set.playTogether(animators);
        set.addListener(new AnimatorListenerAdapter() {
@Override
public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mAnimating = false;
        mLastView = null;
        }
        });
        set.setDuration(MOVE_DURATION);
        set.start();
        footerAnimator.start();
        ANIMATION_FLAG = OPEN_TO_OPEN;
        return true;
        }
        });

        break;
default:
        Log.d(logTag, "TERRIBLE THINGS");
        }
 */

