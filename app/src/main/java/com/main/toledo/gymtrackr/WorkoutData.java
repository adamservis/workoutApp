package com.main.toledo.gymtrackr;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Adam on 2/11/2015.
 * This singleton will be used to display data on the workspace.
 */

public class WorkoutData {
    //private HashMap<String, ArrayList<Exercise>> WorkoutMap;
    private static DatabaseWrapper sDatabaseWrapper = new DatabaseWrapper();
    private ArrayList<Circuit> mWorkout = new ArrayList<Circuit>();
    private int mPlanId;
    private String m_name;
    private static WorkoutData sWorkspaceData;
    private Circuit mFirstPlaceholderCircuit;
    private Circuit mLastPlaceholderCircuit;
    private final static String GENERIC_NAME = "Generic 01";
    private final static String PLACEHOLDER_CIRCUIT_NAME = "...";
    //state data
    //BrowseCreate Transition
    private String mAddedExerciseName;
    private int mBrowseTransition = 0;
    private String mLastFilter1 = null;
    private String mLastFilter2 = null;
    //workspace
    private int mState;
    //private String mPlanName;
    private int mStateCircuit;
    private boolean mStateCircuitOpen;
    private int mBrowseState = 0;

    //detail state data
    private int mDetailTransition;
    private int mDetailCircuit;
    private int mDetailExercise;

    //history default list values
    private int mHistoryDividerHeight;
    private Drawable mHistoryDivider;
    private boolean firstLoad = true;

    private WorkspaceFragment.swapHandler mSwapHandler;

    private WorkoutData(Context appContext){
        //adds initial values
        initialize();
    }

    public void initialize(){
        /*
        Circuit c = new Circuit();
        c.setOpenStatus(false);
        Exercise e = new Exercise();
        c.add(e);
        mWorkout.add(c);
        */
        mWorkout.clear();
        mFirstPlaceholderCircuit = WorkoutData.getNewPlaceholderCircuit();
        mLastPlaceholderCircuit = WorkoutData.getNewPlaceholderCircuit();
        //doStubs();
        addPlaceholders();
    }
    /*
     * DATABASE ACCESS METHODS
     */
    public void savePlan(){
        sDatabaseWrapper.saveEntirePlan(crapNewPlan());
    }

    public void saveHistory(){
        sDatabaseWrapper.addExerciseToHistory(crapHistory());
    }

    public void createNewPlanWithName(String planName){
        Plan p = new Plan();
        p.setName(planName);
        Circuit_temp[] circuits = new Circuit_temp[0];
        p.setCircuits(circuits);
        sDatabaseWrapper.saveEntirePlan(p);
    }

    public String[] pullPlanList(){
        return sDatabaseWrapper.loadPlanNames();
    }

    public void loadPlan(String planName){
        eatPlan(sDatabaseWrapper.loadEntirePlan(planName), true);
    }
    /*
     * END DATABASE ACCESS METHODS
     */
    public boolean isCircuitAtPositionPlaceholder(int position){
        if(position > mWorkout.size()-1){
            return false;
        }
        return mWorkout.get(position) == mFirstPlaceholderCircuit || mWorkout.get(position) == mLastPlaceholderCircuit;
    }

    public static WorkoutData get(Context c){
        if (sWorkspaceData == null){
            sWorkspaceData = new WorkoutData(c.getApplicationContext());
        }
        return sWorkspaceData;
    }
    public static Circuit getNewOpenCircuitWithName(String name){
        Circuit c = new Circuit();
        c.setName(name);
        c.setOpenStatus(true);
        c.setExpanded(false);
        c.add(new Exercise());
        c.setType(Circuit.CircuitType.DATA);
        return c;
    }


    public static Circuit getClosedCircuit(){
        Circuit c = new Circuit();
        c.setOpenStatus(false);
        c.setType(Circuit.CircuitType.DATA);
        return c;
    }

    public static Circuit getNewPlaceholderCircuit(){
        Circuit circuit = new Circuit();
        circuit.setOpenStatus(false);
        circuit.setName(PLACEHOLDER_CIRCUIT_NAME);
        circuit.setType(Circuit.CircuitType.PLACEHOLDER);
        return circuit;
    }

    public ArrayList<Circuit> getWorkout(){
        return mWorkout;
    }


    public void setSwapHandler(WorkspaceFragment.swapHandler handler){
        mSwapHandler = handler;
    }

    public void swap(Exercise exerciseToSwap){
        if(mSwapHandler != null){
            mSwapHandler.swap(exerciseToSwap);
            mSwapHandler = null;
        }
    }

    /*

    */

    public static Exercise getCopyExercise(Exercise originalExercise){
        Exercise copyExercise = new Exercise();
        copyExercise.setName(originalExercise.getName());
        copyExercise.setEquipment(originalExercise.getEquipment());
        copyExercise.setMuscleGroup(originalExercise.getMuscleGroup());
        copyExercise.setId(originalExercise.getId());
        for(Metric m : originalExercise.getMetrics()){
            Metric metric = new Metric();
            metric.setType(m.getType());
            switch(m.getType()){
                case TIME:
                    metric.setMetricIntValue(m.getMetricIntValue());
                    break;
                case WEIGHT:
                    metric.setMetricIntValue(m.getMetricIntValue());
                    break;
                case REPS:
                    metric.setMetricIntValue(m.getMetricIntValue());
                    break;
                case OTHER:
                    metric.setMetricStringValue(m.getMetricStringValue());
                    break;
            }
            copyExercise.addMetrics(metric);
        }
        return copyExercise;
    }

    public static Circuit getCopyCircuit(Circuit originalCircuit){
        Circuit copyCircuit = new Circuit();
        copyCircuit.setName(originalCircuit.getName());
        copyCircuit.setOpenStatus(originalCircuit.isOpen());
        for(Exercise e: originalCircuit.getExercises()){
            //Log.d("singleton", "Adding exercise " + e.getName() + " to copy circuit");
            copyCircuit.add(getCopyExercise(e));
        }
        return copyCircuit;
    }

    public static Exercise getGenericExercise(){
        Exercise generic = new Exercise();
        generic.setName(GENERIC_NAME);

        Metric reps = new Metric();
        reps.setType(metricType.REPS);

        Metric time = new Metric();
        time.setType(metricType.TIME);

        Metric weight = new Metric();
        weight.setType(metricType.WEIGHT);

        generic.addMetrics(weight);
        generic.addMetrics(reps);
        generic.addMetrics(time);

        return generic;
    }
    /*

    */
    public void removePlaceholders(){
        mWorkout.remove(mWorkout.size() - 1);
        mWorkout.remove(0);
    }
    public void addPlaceholders(){
        mWorkout.add(0, mFirstPlaceholderCircuit);
        mWorkout.add(mLastPlaceholderCircuit);
    }
    //newPlan
    public Plan crapNewPlan(){
        //removePlaceholders();
        Plan plan = new Plan();
        //minus one for the placeholder at end
        plan.setName(m_name);
        plan.setPlanId(mPlanId);
        Circuit_temp[] circuits = new Circuit_temp[mWorkout.size() - 1];

        //for each circuit
        //plan.setPlanId(mPlanId);
        for(int i = 0; i < mWorkout.size() - 1; i++){

            //if(!mWorkout.get(i).isOpen() && mWorkout.get(i).getExercises().get(0).getName().equals(GENERIC_NAME)) continue;

            Circuit_temp cTemp = new Circuit_temp();
            circuits[i] = cTemp;
            if (mWorkout.get(i).getName() != null) {
                circuits[i].setName(mWorkout.get(i).getName());
            }
            circuits[i].setOpen(mWorkout.get(i).isOpen());
            circuits[i].setSequence(i);
            //setup to go through exercises
            int numExercises = mWorkout.get(i).getExercises().size();
            ArrayList<Exercise> exerciseArrayList;
            exerciseArrayList = mWorkout.get(i).getExercises();
            //instantiates exercise array to put into plan
            if(mWorkout.get(i).isOpen()) { //handles open circuit case
                //open circuit exercises equal to size - 1 because of placeholder value
                Exercise[] exercisesArray = new Exercise[numExercises - 1];
                for(int j = 0; j < numExercises; j++) {
                    //get the exercises from the circuit, excluding the placeholder used
                    //in open circuits
                    if( j != numExercises - 1){
                        //puts metric stubs into DB speak
                        exercisesArray[j] = exerciseArrayList.get(j);
                        exercisesArray[j].setTime(-1);
                        exercisesArray[j].setWeight(-1);
                        exercisesArray[j].setRepetitions(-1);
                        for(Metric m : exerciseArrayList.get(j).getMetrics()){
                            switch(m.getType()) {
                                case WEIGHT:
                                    exercisesArray[j].setWeight(m.getMetricIntValue());
                                    break;
                                case REPS:
                                    exercisesArray[j].setRepetitions(m.getMetricIntValue());
                                    break;
                                case TIME:
                                    exercisesArray[j].setTime(m.getMetricIntValue());
                                    break;
                                case OTHER:
                                    //we don't support other yet
                                    break;
                                default:

                                    break;
                            }
                        }
                    }
                }
                circuits[i].setExercises(exercisesArray);
            } else { //handles closed circuit case
                //closed circuit is array of one
                Exercise[] exercisesArray = new Exercise[1];

                exercisesArray[0] = exerciseArrayList.get(0);
                exercisesArray[0].setTime(-1);
                exercisesArray[0].setWeight(-1);
                exercisesArray[0].setRepetitions(-1);

                circuits[i].setExercises(exercisesArray);
                for(Metric m : exerciseArrayList.get(0).getMetrics()){
                    switch(m.getType()) {
                        case WEIGHT:
                            exercisesArray[0].setWeight(m.getMetricIntValue());
                            break;
                        case REPS:
                            exercisesArray[0].setRepetitions(m.getMetricIntValue());
                            break;
                        case TIME:
                            exercisesArray[0].setTime(m.getMetricIntValue());
                            break;
                        case OTHER:
                            //we don't support other yet
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        plan.setCircuits(circuits);
        //debug shit
        //end debug shit
        //addPlaceholders();
        return plan;
    }

    public void eatPlan(Plan p, boolean workout_from_plan_flag ){
        this.clear();
        //mPlanId = p.getPlanId();
        mPlanId = p.getPlanId();
        m_name = p.getName();

        Circuit_temp[] circuits = p.getCircuits();
        if(p.getCircuits().length != 0) {
            boolean sorted = false;
            //sort in case it's not sorted

            while (!sorted) {
                boolean test = true;
                for (int i = 0; i < circuits.length; i++) {
                    if (i != circuits.length - 1) {
                        if (circuits[i].getSequence() > circuits[i + 1].getSequence()) {
                            Circuit_temp c_temp = circuits[i];
                            circuits[i] = circuits[i + 1];
                            circuits[i + 1] = c_temp;
                            test = false;
                        }
                    } else {
                        sorted = test;
                    }
                }
            }
        }
        //Log.d("EAT PLAN TESTS", "SORT COMPLETED");

        for (Circuit_temp c_old : circuits){
            Circuit c_new = new Circuit();
            c_new.setOpenStatus(c_old.isOpen());
            c_new.setName(c_old.getName());
            c_new.setId(c_old.getCircuitId());
            Exercise[] exercises = c_old.getExercises();
            for(Exercise e : exercises){
                /*
                Metric weight = new Metric();
                weight.setType(metricType.WEIGHT);
                Metric reps = new Metric();
                reps.setType(metricType.REPS);
                e.addMetrics(weight);
                e.addMetrics(reps);
                */
                if(workout_from_plan_flag){
                    //Log.d("PLAN METRIC", "addSeparatePlanMetrics called");
                    e.addSeparatePlanMetrics();
                }
                //Log.d("SAVE TESTS", "FROM RETREIVED PLAN.  EXERCISE NAME: " + e.getName() + " EXERCISE ID: " + e.getId());
                c_new.add(e);
            }
            if(c_new.isOpen()){
                c_new.add(new Exercise());
            }
            if(!(!c_new.isOpen() && c_new.getExercises().size()== 0))
                                                  mWorkout.add(c_new);
        }
        //Log.d("EAT PLAN TESTS", "COPY COMPLETED");
        initialize();
    }

    public ExerciseHistory[] crapHistory(){
        //Log.d("SAVE TESTS", "SAVE TO HISTORY CALLED");
        ArrayList<ExerciseHistory> tempExerciseHolder = new ArrayList<>();
        //Pull all workout exercises into temp array
        for(Circuit c : mWorkout){
            for (Exercise e : c.getExercises()){
                if (e.isSaveToHistorySet()){
                    //Log.d("SAVE TESTS", e.getName() + " " + e.getId() + " IS BEING DIGESTED INTO EH");
                    int weight, reps;
                    Date d = new Date();
                    int time = -1;
                    String other = null;
                    weight = -1;
                    reps = -1;
                    ArrayList<Metric> metrics = e.getMetrics();
                    for(Metric m : metrics){
                        switch(m.getType()) {
                            case WEIGHT:
                                weight = m.getMetricIntValue();
                                break;
                            case REPS:
                                reps = m.getMetricIntValue();
                                break;
                            case TIME:
                                time = m.getMetricIntValue();
                                break;
                            case OTHER:
                                //we don't support other yet
                                break;
                            default:
                                //Log.d("TERRIBLE THINGS", "SOMETHING TERRIBLE HAPPENED WHEN WORKOUTDATA TRIED TO CRAP");
                                break;
                        }
                    }

                    ExerciseHistory eh = new ExerciseHistory(
                        d, //date
                        weight,     //weight
                        reps,       //reps
                        e.getId(),          //exercise id
                        mPlanId,     //plan id
                        time,
                        other,
                        e.getName()
                    );

                    tempExerciseHolder.add(eh);
                }
            }
        }
        //Convert to array for db placement
        ExerciseHistory[] exerciseHistory = new ExerciseHistory[tempExerciseHolder.size()];
        exerciseHistory = tempExerciseHolder.toArray(exerciseHistory);

        return exerciseHistory;
    }

    public boolean isEmpty(){
        boolean empty = true;
        if(mWorkout.get(0).getExercises().size()>1){
            empty = false;
        }
        if(mWorkout.size() > 1){
            empty = false;
        }
        return empty;
    }

    public void exerciseRemoved(int id){
        ArrayList<Integer> circuitsToRemove = new ArrayList<>();

        for(int i = 0; i< mWorkout.size();i++) {
            Circuit c = mWorkout.get(i);         //for each circuit
            for (int j = c.getSize()-1; j>=0; j--) {   //for each exercise
                Exercise e = c.getExercise(j);
                if (e.getId() == id) {  //if the exercise id = the removed exercise id
                    c.getExercises().remove(e);
                    if(!c.isOpen()){ //if the circuit is open
                        circuitsToRemove.add(i); //add its index to a list
                    }
                }
            }
        }
        for(int i = circuitsToRemove.size()-1; i>=0; i--){
            int j = circuitsToRemove.get(i);
            mWorkout.remove(j);
        }
    }
    /*
    public void clearCheckedExercises(){
        for(Circuit c : mWorkout){
            ArrayList<Exercise> exercises = c.getExercises();
            int numExercises = exercises.size();
            for(int j = numExercises-1; j>=0; j--){
                if (exercises.get(j).isSaveToHistorySet()){
                    exercises.remove(j);
                }
            }
        }
        int length = mWorkout.size() - 1;
        for(int i = length-1; i>=0; i-- ){
            Circuit c = mWorkout.get(i);
            int numExercises = c.getExercises().size();
            if(!c.isOpen()&&(numExercises == 0))
                mWorkout.remove(i);
        }
    }
    */
    public void clear(){
        mWorkout.clear();
        m_name = "";
        mPlanId = -1;
        //mToggledExercise = null;
    }

    public void setWorkoutState(int state){
        mState = state;
    }

    public int getState(){
        return mState;
    }

    //public void setWorkoutPlanName(String name){mPlanName = name;}

    //public String getWorkoutPlanName(){return mPlanName;}

    public void setBrowseState(int state){mBrowseState = state;}

    public int getBrowseState(){return mBrowseState;}

    //public void setStateCircuit(int i){mStateCircuit = i;}

    public int getStateCircuit(){return mStateCircuit;}

    //public void setStateCircuitOpenStatus(boolean status){mStateCircuitOpen = status;}

    public boolean isStateCircuitOpen(){return mStateCircuitOpen;}

    //DETAIL TRANSITION METHODS

    public void setDetailTransition(int transition){
        mDetailTransition = transition;

    }
    public int getDetailTransition(){
        return mDetailTransition;
    }

    public void setDetailCircuit(int circuit){
        mDetailCircuit = circuit;
    }

    public int getDetailCircuit(){return mDetailCircuit;}

    public void setDetailExercise(int exercise){mDetailExercise = exercise;}

    public int getDetailExercise(){return mDetailExercise;}

    //BROWSE TRANSITION METHODS

    public int getBrowseTransition(){return mBrowseTransition;};
    public void setBrowseTransition(int transition){mBrowseTransition = transition;}

    public void setExerciseCreated(String exerciseName){mAddedExerciseName = exerciseName;}
    public String getExerciseCreated(){return mAddedExerciseName;}

    public void setLastFilter1(String lastFilter){mLastFilter1 = lastFilter;}
    public String getLastFilter1(){return mLastFilter1;}

    public void setLastFilter2(String lastFilter){mLastFilter2 = lastFilter;}
    public String getLastFilter2(){return mLastFilter2;}

    //HISTORY THINGS

    //public void setFirstLoad(boolean b){firstLoad = b;}
    //public boolean getFirstLoad(){return firstLoad;}

    //public void setDividerHeight(int height){mHistoryDividerHeight = height;}
    //public int getDividerHeight(){return mHistoryDividerHeight;}

    //public void setHistoryDivider(Drawable divider){mHistoryDivider = divider;}
    //public Drawable getHistoryDivider(){return mHistoryDivider;}

}
/*

NOW ENTERING CONVENIENCE METHOD PURGATORY

private void doStubs(){
        Circuit circuitOne = new Circuit();
        circuitOne.setOpenStatus(true);
        circuitOne.setName("Circuit One");
        circuitOne.add(new Exercise(0, "1", 0, 0, 0, 0, 0, -1));
        circuitOne.add(new Exercise(1, "2", 0, 0, 0, 0, 0, -1));
        circuitOne.add(new Exercise(2, "3", 0, 0, 0, 0, 0, -1));
        circuitOne.add(new Exercise(3, "4", 0, 0, 0, 0, 0, -1));
        circuitOne.add(new Exercise());

        Circuit circuitTwo = new Circuit();
        circuitTwo.setOpenStatus(true);
        circuitTwo.setName("Circuit Two");
        circuitTwo.add(new Exercise(0, "1", 0, 0, 0, 0, 0, -1));
        circuitTwo.add(new Exercise(1, "2", 0, 0, 0, 0, 0, -1));
        circuitTwo.add(new Exercise(2, "3", 0, 0, 0, 0, 0, -1));
        circuitTwo.add(new Exercise(3, "4", 0, 0, 0, 0, 0, -1));
        circuitTwo.add(new Exercise());

        Circuit circuitThree = new Circuit();
        circuitThree.setOpenStatus(true);
        circuitThree.setName("Circuit Three");
        circuitThree.add(new Exercise(0, "1", 0, 0, 0, 0, 0, -1));
        circuitThree.add(new Exercise(1, "2", 0, 0, 0, 0, 0, -1));
        circuitThree.add(new Exercise(2, "3", 0, 0, 0, 0, 0, -1));
        circuitThree.add(new Exercise(3, "4", 0, 0, 0, 0, 0, -1));
        circuitThree.add(new Exercise());

        Circuit circuitFour = new Circuit();
        circuitFour.setOpenStatus(true);
        circuitFour.setName("Circuit Four");
        circuitFour.add(new Exercise(0, "1", 0, 0, 0, 0, 0, -1));
        circuitFour.add(new Exercise(1, "2", 0, 0, 0, 0, 0, -1));
        circuitFour.add(new Exercise(2, "3", 0, 0, 0, 0, 0, -1));
        circuitFour.add(new Exercise(3, "4", 0, 0, 0, 0, 0, -1));
        circuitFour.add(new Exercise());

        Circuit circuitFive = new Circuit();
        circuitFive.setOpenStatus(true);
        circuitFive.setName("Circuit Five");
        circuitFive.add(new Exercise(0, "1", 0, 0, 0, 0, 0, -1));
        circuitFive.add(new Exercise(1, "2", 0, 0, 0, 0, 0, -1));
        circuitFive.add(new Exercise(2, "3", 0, 0, 0, 0, 0, -1));
        circuitFive.add(new Exercise(3, "4", 0, 0, 0, 0, 0, -1));
        circuitFive.add(new Exercise());

        mWorkout.add(mFirstPlaceholderCircuit);
        mWorkout.add(circuitOne);
        mWorkout.add(circuitTwo);
        mWorkout.add(circuitThree);
        mWorkout.add(circuitFour);
        mWorkout.add(circuitFive);
        mWorkout.add(mLastPlaceholderCircuit);
    }

    //adds a new open circuitworkspaceListView
    public void addCircuit(int circuitNumber){
        Circuit c = new Circuit();
        c.setName("Circuit " + circuitNumber);
        c.add(new Exercise());
        c.setOpenStatus(true);
        mWorkout.add(circuitNumber, c);
    }
    //adds a closed circuit, e.g. a circuit with only one exercise
    public void addClosedCircuit(Exercise e, int circuitNumber){
        Circuit c = new Circuit();
        c.setOpenStatus(false);
        c.add(e);
        mWorkout.add(circuitNumber, c);
    }

    public void placeClosedCircuitWithExercise(int circuitPosition, Exercise exercise){
        Circuit c = new Circuit();
        c.setOpenStatus(false);
        c.add(exercise);
        mWorkout.add(circuitPosition, c);
    }
        public void setToggledExerciseExplicit(Exercise e){
        mToggledExercise = e;
    }

    public void clearToggledExercise(){ mToggledExercise = null;}

    public boolean isAnExerciseToggled(){
        if (mToggledExercise == null){
            return false;
        }
        return true;
    }

    public Exercise getToggledExercise(){return mToggledExercise;}


    public void addExerciseToOpenCircuit(Exercise e, int circuitNumber){

        int circuitSize = mWorkout.get(circuitNumber).getSize();
        //adds exercise to second to last position
        mWorkout.get(circuitNumber).add(circuitSize - 1, e);
        //mWorkout.get(circuitNumber).isNotLast();

        if (mWorkout.get(circuitNumber).getName() == "Placeholder"){
            mWorkout.get(circuitNumber).setName("Circuit " + circuitNumber);
            Circuit c = new Circuit();
            mWorkout.add(c);
        }
    }

        public static Circuit getClosedCircuitWithExercise(Exercise exercise){
        Circuit circuit = new Circuit();
        circuit.setOpenStatus(false);
        circuit.add(exercise);
        circuit.setType(Circuit.CircuitType.DATA);
        return circuit;
    }
        public void placeNewCircuit(int circuit) {
        Circuit c = new Circuit();
        c.setName("New Circuit");
        c.add(new Exercise());
        c.setOpenStatus(true);
        mWorkout.add(circuit, c);
    }
 */