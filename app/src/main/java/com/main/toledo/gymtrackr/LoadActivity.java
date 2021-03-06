package com.main.toledo.gymtrackr;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adam on 2/10/2015.
 */
public class LoadActivity extends ActionBarActivity {
    //fragments needed for the load activity
    //private String[] planArray; //stubs = {"CHEST", "BACK", "ARMS"};
    private ArrayList<String> mPlanList;
    private int actionToPerform;
    //Program State Constants
    final int PLAN = 1, WORKOUT = 2, WORKOUT_FROM_PLAN_FLAG = 3, WORKOUT_WITH_PLAN = 4, LOAD_PLAN = 5;
    //Error constants
    //final int OTHER = 10, INVALID_NAME_VALUE = 11, TAKEN_NAME_VALUE = 12;
    //
    int errorType;
    int slideVal;

    int SCREENWIDTH;
    int SCREENHEIGHT;
    //this is the stub list
    //private static ArrayList<Plan> workoutPlans = new ArrayList<>();
    //the mAdapter is responsible for populating the load list
    private LoadAdapter mAdapter;
    private String newPlanName;
    private boolean mCopyFlag;
    private WorkoutData mWorkoutData;
    //testvals
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.l_activity_load);
        /*

        PLAN DB CALL WILL GO HERE, GIVE US OUR INITIAL LIST OF PLANS
        MAKE WORKOUTPLANS REFLECT OUR DB STUFF AND WE'RE GOOD

         */
        mWorkoutData = WorkoutData.get(this);
        mPlanList = new ArrayList<String>();
        //convert array to list for dynamic stuffs
        for (String s : mWorkoutData.pullPlanList()){
            mPlanList.add(s);
        }
        //creates a list mAdapter for our stub exercises
        mAdapter = new LoadAdapter(this, 0, mPlanList);
        /*
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.loadListContainer, ListFragment);
        transaction.commit();
        */
        ListView loadListView = (ListView) findViewById(R.id.loadListView);
        loadListView.setAdapter(mAdapter);
        loadListView.setDivider(null);
        loadListView.setDividerHeight(0);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREENWIDTH = size.x;
        SCREENHEIGHT = size.y;
        slideVal = -(int)(.5 * SCREENWIDTH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_load, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_plan:
                showNameDialog();
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("New Plan Options");
                builder.setItems(new CharSequence[]
                                {"Create blank plan", "Create plan from workspace contents", "Cancel"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which) {
                                    case 0:
                                        mCopyFlag = false;
                                        showNameDialog();
                                        break;
                                    case 1:
                                        mCopyFlag = true;
                                        showNameDialog();
                                        break;
                                    case 2:

                                        break;
                                }
                            }
                        });
                builder.create().show();
                */
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setNewPlanName(String s){
        newPlanName = s;
    }

    public ArrayList<String> getPlanList(){return mPlanList; }

    public void setCopyFlag(boolean b){mCopyFlag = b;}

    public boolean getCopyflag(){return mCopyFlag;}

    public void createPlanFromWorkspace(){
        mCopyFlag = false;
        Plan p = WorkoutData.get(getApplicationContext()).crapNewPlan();
        p.setName(newPlanName);
        DatabaseWrapper db = new DatabaseWrapper();
        db.saveEntirePlan(p);
        mPlanList.clear();
        String[] planArray = db.loadPlanNames();
        //convert array to list for dynamic stuffs
        for (String s : planArray){
            mPlanList.add(s);
        }
    }

    public void createNewPlan(){

        WorkoutData.get(this).createNewPlanWithName(newPlanName);
        mPlanList.clear();
        //convert array to list for dynamic stuffs
        for (String s : WorkoutData.get(this).pullPlanList()){
            mPlanList.add(s);
        }
    }

    public void showNameDialog(){
        LoadNamePlanDialog dialog = new LoadNamePlanDialog();
        dialog.show(getSupportFragmentManager(), "NameDialogFragment");
    }

    public void showErrorDialog(int error){
        errorType = error;
        LoadErrorDialog dialog = new LoadErrorDialog();
        dialog.show(getSupportFragmentManager(), "ErrorDialogFragment");
    }

    public int getError(){
        return errorType;
    }

    public class LoadAdapter extends ArrayAdapter{

        private SwipableLinearLayout mTextViewHandle;

        public LoadAdapter(Context context, int resource, ArrayList<String> plans){
            super(context, resource, plans);
        }

        private swipeLayoutListener listener =
                new swipeLayoutListener() {

            public void CloseTextViewHandle(){
                if (mTextViewHandle != null){
                    mTextViewHandle.close();
                }
            }

            public void setTextViewHandle(SwipableLinearLayout l){
                mTextViewHandle = l;
            }

            public void clearHandle(){
                mTextViewHandle = null;
            }
        };

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            if ((convertView == null)) {
                convertView = getLayoutInflater()
                        .inflate(R.layout.l_list_item, null);
            }

            final String planName = (String)getItem(position);

            final SwipableLinearLayout swipableLinearLayout =
                    (SwipableLinearLayout)convertView.findViewById(R.id.swipeLayoutHandleLoad);
            swipableLinearLayout.setSwipeOffset(slideVal);
            swipableLinearLayout.setSwipeLayoutListener(listener);
            swipableLinearLayout.percentageToDragEnable(0f);

            TextView t = (TextView) convertView.findViewById(R.id.planName);
            t.setText(planName);
            if(swipableLinearLayout.getX() != 0){
                swipableLinearLayout.setX(0f);
                mTextViewHandle.setOpen(false);
                mTextViewHandle = null;
            }
            swipableLinearLayout.refreshIcon();

            ImageButton delete = (ImageButton) convertView.findViewById(R.id.deleteButton);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipableLinearLayout.resetPosition();
                    mTextViewHandle = null;
                    DatabaseWrapper db = new DatabaseWrapper();
                    db.deletePlan(planName);
                    mPlanList.remove(planName);
                    notifyDataSetChanged();
                }
            });

            ImageButton edit = (ImageButton) convertView.findViewById(R.id.editButton);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWorkoutData.savePlan();
                    Intent i = new Intent(getContext(), WorkspaceActivity.class);
                    startActivity(i);
                }
            });

            ImageButton workout = (ImageButton) convertView.findViewById(R.id.workoutButton);
            workout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWorkoutData.loadPlan(planName);
                    Intent i = new Intent(getContext(), WorkspaceActivity.class);
                    startActivity(i);
                }
            });

            return convertView;
        }

    }
}