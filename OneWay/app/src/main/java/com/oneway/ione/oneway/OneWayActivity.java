package com.oneway.ione.oneway;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class OneWayActivity extends Activity {

    private GameEngineView memoVectorView; // custom view to display the game
    //private static final String TAG = "Activity"; // for logging errors
    private TextView displayCurrentLevelGoal; //instantiate for displaying the level of the game
    private TextView displayCurrentLevelMe; //instantiate for displaying user level
    private TextView displayCurrentLevelBest; //instantiate for displaying best user level
    private static final String checkFirsTime = "com.oneway.marcel.oneway.checkFirstTimes";
    private SharedPreferences instructionFirstTime;//To save value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oneway);

        memoVectorView = (GameEngineView)findViewById(R.id.memoVectorView);//Get reference to view
        displayCurrentLevelGoal=(TextView) findViewById(R.id.displayLevelGoal);//Get reference to level goal Textview
        displayCurrentLevelMe=(TextView) findViewById(R.id.displayLevelMe);//Get reference to user level Textview
        displayCurrentLevelBest=(TextView)findViewById(R.id.displayLevelBest);//Get reference to best user level Textview

        memoVectorView.receiveTextview(displayCurrentLevelGoal,displayCurrentLevelMe,displayCurrentLevelBest);

        //Show the instruction if app is launched for the first time
        if(firstTimeShowInstruction()){

            showHowToPlay();//Show the instruction
        }


    }//End onCreate method()


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        switch (item.getItemId()){

            case R.id.action_info:
                //Log.e(TAG, "I am in menu item");
                //Help overlay to show "how to play" the game
                showHowToPlay();
                return true;

            case R.id.action_reset:
                memoVectorView.clearLevelScoreBest();
                return true;
            case R.id.action_me:
                memoVectorView.clearOnlyMe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//end switch
    }//End method onOptionsItemSelected()

    public void startMyGame(View v) {

        //Log.e(TAG, "this is pressing button");
        memoVectorView.newGame();
    }

    private void showHowToPlay(){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //Enable extended window features.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//Retrieve the current Window for the activity, and change bsckground
        dialog.setContentView(R.layout.pop_window_instruction); //Set the screen content to an explicit view
        dialog.setCanceledOnTouchOutside(true);//Sets whether this dialog is canceled when touched outside the window's bounds

        //For dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.popWindow);//Finds a child view with the given identifier.

        //Set a click listener to the dialog
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();//Dismiss this dialog, removing it from the screen.
            }
        });
        dialog.show(); //Start the dialog and display it on screen.

    }//end method showHowToPlay

    private boolean firstTimeShowInstruction(){

        instructionFirstTime=getSharedPreferences(checkFirsTime, 0); //Initialize SharedPreferences
        boolean ranBeforeLaunch=instructionFirstTime.getBoolean(checkFirsTime, false);

        if (!ranBeforeLaunch) {

            //This is the first time app is launched
            SharedPreferences.Editor editor = instructionFirstTime.edit();
            editor.putBoolean(checkFirsTime, true);
            editor.apply();

        }

        return !ranBeforeLaunch;
    }//end method firstTimeShowInstruction

}//End OneWayActivity
