package com.main.toledo.gymtrackr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by Adam on 2/15/2015.
 */
public class WorkspaceActivity extends ActionBarActivity {

    //WorkspaceExpandableListAdapterMKIII listAdapter;
    private WorkspaceFragment ListFragment;
    //private WorkspacePalletFragment PalletFragment ;
    private String planName;
    private Menu mOptionsMenu;
    int mode;
    //LOAD/START STATES
    final int PLAN = 1, WORKOUT = 2, WORKOUT_WITH_PLAN = 4, LOAD_PLAN = 5, FROM_DETAIL = 6;

    //BROWSE STATES
    final int NOT_BROWSE = 0, BROWSE_WORKOUT = 1, WORKOUT_BROWSE = 2;

    //boolean workout_from_plan_flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        mode = WorkoutData.get(this).getState();
        switch(mode){
            case PLAN:
                break;
            case LOAD_PLAN:
                DatabaseWrapper db = new DatabaseWrapper();
                planName = WorkoutData.get(this).getWorkoutPlanName();
                Plan planList = db.loadEntirePlan(planName);
                WorkoutData.get(this).eatPlan(planList, workout_from_plan_flag);
                WorkoutData.get(this).setWorkoutState(PLAN);
                mode = PLAN;
                break;
            case WORKOUT:
                break;
            case WORKOUT_WITH_PLAN:
                workout_from_plan_flag = true;
                DatabaseWrapper db2 = new DatabaseWrapper();
                planName = WorkoutData.get(this).getWorkoutPlanName();
                Plan planList2 = db2.loadEntirePlan(planName);
                WorkoutData.get(this).eatPlan(planList2, workout_from_plan_flag);
                WorkoutData.get(this).setWorkoutState(WORKOUT);
                break;
        }
        */
        setContentView(R.layout.w_activity_main);

        //PalletFragment = new WorkspacePalletFragment();
        ListFragment = new WorkspaceFragment();

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        //transaction.add(R.id.WorkspacePalletContainer, PalletFragment);
        transaction.add(R.id.WorkspaceFragmentContainer, ListFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        mOptionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_workspace, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent i;
        switch (item.getItemId()) {
            /*
            case R.id.action_toggle_edit:
                toggleEdit();
                return true;
            case R.id.save_changes:
                WorkspaceConfirmDialog dialog = new WorkspaceConfirmDialog();
                dialog.show(getFragmentManager(), "NameDialogFragment");
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
                */
            case R.id.plan_menu:
                i = new Intent(WorkspaceActivity.this, LoadActivity.class);
                startActivity(i);
                return true;
            case R.id.action_save_to_history:
                return true;
            case R.id.action_view_history:
                i = new Intent(WorkspaceActivity.this, historyActivity.class);
                startActivity(i);
                return true;
            case R.id.action_clear:
                WorkoutData.get(this).initialize();
                ListFragment.getAdapter().notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void save(){
        if (mode == PLAN) {
            //CODE FOR PLAN SAVE
            WorkoutData.get(this).savePlan();
        }
        if (mode == WORKOUT || mode == WORKOUT_WITH_PLAN) {
            //CODE FOR WORKOUT SAVE, EG EXPORT TO HISTORY
            //DatabaseWrapper db = new DatabaseWrapper();
            WorkoutData.get(this).saveHistory();
            //removeChecked();
        }
    }

    public int getAppMode(){return mode;}

    @Override
    public void onResume(){

        super.onResume();
        /*
        Log.d("workspaceActivity", "activity resume");
        ListFragment.updateAdapter();

        if(listAdapter==null) //todo should pry be in list fragment
            listAdapter = new WorkspaceExpandableListAdapterMKIII(this);

        listAdapter.hideKeypad();
        */
    }
    @Override
    public void onPause(){
        super.onPause();
        WorkoutData.get(this).setBrowseState(NOT_BROWSE);
    }
    /*
    public WorkspaceExpandableListAdapterMKIII getAdapter(){
        return this.listAdapter;
    }
    */

}
