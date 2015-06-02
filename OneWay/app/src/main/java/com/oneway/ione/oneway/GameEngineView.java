package com.oneway.ione.oneway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


/**
 * This is the engine class for generating the random pathSwipeFinder using SurfaceView,
 * a frame by frame loop thread,runOnUiThread for communicating with main activity,
 * OnTouch Events Listener
 *
 *
 * @author  Marcelino Yax on  1/26/2015.
 */

public class GameEngineView extends SurfaceView implements SurfaceHolder.Callback{

    //=================================
    //Declaring All Necessary Variables
    //=================================

    //private static final String TAG = "GameEngineView"; // for logging errors

    // Key used to persistently store the value of best score
    private static final String scoreBest_key = "com.oneway.marcel.oneway.bestscore";
    private static final String levelCurrent_key = "com.oneway.marcel.oneway.currentlevel";

    private SharedPreferences saveHighBest; //Instantiate a SharedPreferences object for storing
    //best score

    //Variable for Displaying value for Level, Score and Bes in main activity
    private Integer saveUserCurrentLevel; //To hold the user current level
    private Integer userCurrentBestLevel; //To hold the user current best level
    private Integer showCurrentLevel; //To hold the user current level

    //Object and Variables needed to control the game loop
    private MemoVectorThread memoThread; // Thread to control the game loop
    private Activity activity; // To display animated Level,Score and Best in main activity GUI thread
    private boolean dialogIsDisplayed = false; //used inside surfaceCreated()

    //Variables needed for generating the random pathSwipeFinder
    private int n; //Number of lines for the pathSwipeFinder
    private int nRandomTime; //new value of n for generating new level of n, if n=12, nRandomTime could be 9,10,11
    private int initial_x; //x coordinate in pixel for the initial point
    private int initial_y; //y coordinate in pixel for the initial point
    private int save_x; //Save the x coordinate in pixel for the initial point
    private int save_y; //Save the y coordinate in pixel for the initial point
    private int new_x; //New x cooridinate in pixel from the random generator
    private int new_y; //New y coordinate in pixel from the random generator
    private int addingPlus; //Constant Speed in pixel for animating the pathSwipeFinder
    private Random getRandom;//Instantiate a random object


    //Declare variables need to calculate values for drawing pathSwipeFinder
    private int initial_x_draw; //Hold the initial x-coordinate in pixel for each line
    private int initial_y_draw; //Hold the initial y-coordinate in pixel for each line
    private int speed_X; //Hold the constant change of x-coordinate in pixel for animation
    private int speed_Y; //Hold the constant change of y-coordinate in pixel for animation
    private int final_x_draw; //Hold the final x-coordinate in pixel of each line
    private int final_y_draw;//Hold the fina y-coordinate in pixel of each line
    private int m; // Number for retrieving x and y coordinate from sequencex and sequencey array
    private int count_Drawing; //Number for drawing each line after finishing animation
    private int xFinalValueinArrayList;//x Value in arrayList when index is n
    private int yFinalValueinArrayList;//y Value in arrayList when index is n


    /**Declare boolean needed inside updatePositions() method
     * allow to calculate values for drawing direction-->up,down, right, left line
     **/
    private boolean go_allow;//Allow to go inside the statement for calculating values for the 4-directions
    private boolean goes_right; //Allow to calculate values for the right line
    private boolean goes_left; //Allow to calculate values for the left line
    private boolean goes_down; //Allow to calculate values for down line
    private boolean goes_up; //Allow to calculate value for up line

    /**Declare boolean needed inside drawGameElements() method
     * allow to draw direction-->up,down, right, left line
     **/
    private boolean draw_right_left_up_down_canvas;//Allow to draw each animated direction line on canvas
    private boolean go_draw; //allow to draw line after animating each direction line
    private boolean drawFinalCircle; //Allow to draw final coordinate point as a circle after animation
    private boolean compareInitialFinalCircle;//Compare initial and final circle size

    //Variable for controlling timer and looping
    private boolean gameOver; // is the game over?
    private boolean loopTimerDrawing;//Allow to go inside the statement for turning on or off thread
    private boolean timeLeftAllow;//Allow to turn off thread
    private boolean loopForDrawing;//Allow to turn on thread so user can draw on screen
    private boolean killThread; //Allow to kill thread in newGame() when thread is on from loopForDrawing


    // Paint variables used when drawing each item on the screen
    private Paint textPaint;            // Paint used to draw text
    private Paint backgroundSecond;      // Paint used to clear the drawing area
    private Paint backgroundFirst;      //Paint used to show the drawing game area
    private Paint lineGrid;             //Paint for Background grid line
    private Paint linePaintAnimation;   //Paint for the line in animation and swiping finger
    private Paint circleInitial;        //Paint for the circle at initial point
    private Paint circleFinal;          //Paint for the circle at final point
    private Paint circleWinFinal;       //Paint for redraw final circle wih line color
    private Paint circleAnimationAndFinger;  //Paint for circle in animation and finger swipe
    private Paint paintSwipeFinger; //Paint for drawing line after animation and lines from finger swipe
    private Paint showLoseLine; //Paint for showing the actual line when lose

    //Declare pathSwipeFinder object:
    // needed for user swipe finger
    private Path pathSwipeFinder;

    //Draw line after animation
    private Path drawPathAfterAnimation; //Hold animated pathSwipeFinder which is displayed after animation

    //Show the actual pathSwipeFinder when you lose
    private Path showPathWhenLose; //Holding pathSwipeFinder which is showed after losing


    //Declare variables for drawing line when user swipes finger up,down,,left and right
    private int startXcoFingerDown; // Hold initial x coordinate when finger down
    private int startYcoFingerDown; //Hold initial y coordinate when finger down
    private int finalXcoFingerUp; //Hold final x coordinate when finger up
    private int finalYcoFingerUp; //Hold final y coordinate when finger up
    private int holdX_for_path;  //Hold x coordinate when finger swipe
    private int holdY_for_path; //Hold y coordinate when finger swipe
    private int differenceFingerX;  //Hold x difference between finger down and finger up
    private int differenceFingerY;  //Hold y difference between finger down and finger up

    //Declare boolean for allow touching screen after drawing animation
    private boolean allowTouch; //Allow the user to touch the screen
    private boolean swipeDrawLine; //Allow to draw the line from finger in drawGameElements() method
    private boolean allowCheckingWinLose;//Allow to check if user win or lose once in the main loop

    /*
    * ArrayList holding x and y coordinate of the
    *lines forming the pathSwipeFinder generated by the random generator
    *The initial points in array is the center of the square space game
    */
    private ArrayList<Integer> sequencex = new ArrayList<Integer>(); //Hold x coordinate for each line
    private ArrayList<Integer> sequencey = new ArrayList<Integer>(); //Hold y coordinate for each line


    /*
    * ArrayList holding x and y line coordinate swiped by the user finger
    * which is compared to ArrayList sequencex and sequencey to find
    * if user follows the right pathSwipeFinder
    * */
    private ArrayList<Integer> holdUserLineX=new ArrayList<Integer>();//Hold x coordinate for each line swipe by user
    private ArrayList<Integer> holdUserLineY=new ArrayList<Integer>(); //Hold y coordinate for each line swipe by user

    //Declare parameters needed for drawing the grid game space
    //All variables are in pixels
    private int xShift; //Hold left margin
    private int yShift; //Hold top margin
    private int holdWidth; //Holding Screen width and height for game space
    private int dePixel; //Hold each grid(line size forming the small grid)
    private int halfdePixel; //Hold half of each grid size (half of the line)
    private int finalYShiftDown; //Hold value needed to shift all grid down so you can display anything on the top
    private int finalHalfYShiftDown; //the half of finalYShiftDown
    private int finalHeight; //Hold final height of the space for drawing
    private int finalYHeight; //Hold final y coordinate for width in y axis
    private int setMarginLeftRight; //set the margin space for left and right side for different screen size

    //Set the maximum screen size and animation speed
    private int finalSpeedAnimation; //Changes this number to change animation speed
    private final int finalMaxiumWidthPixel=600; //Maximum screen size required to reset parameters

    //Set text size, line stroke width and circle radius
    private int setTextSize;//Set the text size for different screen size
    private int setStrokeWidthLine; //Set the line width for different screen size
    private int setCircleRadius;//Set the the circle size for different screen size

    //Holding the number of time line passes over final point
    private int numberOfTimeOverFinal;//Check number of time lines pass over final point
    private int numberOfTimeUserOverFinal;//Check the number of time the user pass over the final point

    private final int  initialMiniumN=4; //The n to begin with
    private final int numberOfAllowN=23; //Maximum number of n

    //Instantiate TextView
    // Reference is given from main activity
    private TextView displayLevelGoal;//instantiate for displaying goal level
    private TextView displayLevelMe;//instantiate for displaying user level
    private TextView displayLevelBest;//instantiate for displaying best user level
    private int totalScoreSum; //Holding the total score
    private int totalLevelGoal; //Holding total goal level


    //===========================================
    // GameEngineView() public constructor
    //===========================================

    public GameEngineView(Context context, AttributeSet attrs){


        super(context, attrs); // call superclass constructor

        //Log.e(TAG, "this is GameEngineView constructor");

        activity = (Activity) context; // Store reference to MainActivity
        //needed for resetting TextView and animation

        // Register SurfaceHolder.Callback listener
        getHolder().addCallback(this);
        setFocusable(true); //Set game space focusable


        //Instantiate Random
        getRandom = new Random();

        // Instantiate all Paint variables as Paint objects
        //these objects are configured in method onSizeChanged
        textPaint = new Paint(); //Text
        backgroundFirst=new Paint(); //First White Background
        backgroundSecond = new Paint(); //Second game grid background
        lineGrid=new Paint(); //Line grid
        linePaintAnimation =new Paint(); //Line in animation
        circleInitial=new Paint(); //Initial circle in red
        circleFinal =new Paint(); //Final circle as blue
        circleWinFinal=new Paint(); //Final circle for winner
        circleAnimationAndFinger =new Paint(); //Circle for animation and swipe finger
        paintSwipeFinger = new Paint(Paint.ANTI_ALIAS_FLAG); //Line for Animation and Swipe finger
        showLoseLine=new Paint(Paint.ANTI_ALIAS_FLAG);//Line after user loses

        //Instantiate pathSwipeFinder object
        drawPathAfterAnimation=new Path(); //Path after animation
        pathSwipeFinder = new Path(); //path for user swipe finger
        showPathWhenLose=new Path(); //show actual path Swipe Finder when you lose


        //getSharereferences has to have reference to MainActivity or to Context to be used
        saveHighBest = activity.getSharedPreferences(scoreBest_key, 0); //Initialize SharedPreferences
        userCurrentBestLevel = saveHighBest.getInt(scoreBest_key, 0);//Get save value from it and set to variable
        saveUserCurrentLevel=saveHighBest.getInt(levelCurrent_key,0);//Get saved current level
        //toDifficultLevel=saveHighBest.getInt(changeDifficultLevel,0); //To Know user moves to difficult level
        //Log.e("user saved level"," "+saveUserCurrentLevel);



        //Level is 1 when app launched

        //Check current user level
        if(saveUserCurrentLevel==0){

            n=initialMiniumN;  //Initial number of line generated from random generator
            showCurrentLevel=0;//Set level when app is launched

        }else{

            n=saveUserCurrentLevel+4; //Set n value
            showCurrentLevel=saveUserCurrentLevel;//Set level when app is launched
        }

        //Calculate the final total score and level
        totalScoreSum=0;
        for(int countScore=initialMiniumN; countScore<numberOfAllowN+1; countScore++){
            //Find total goal score
            totalScoreSum=totalScoreSum+countScore;
        }
        //When level is 12, n is 15, so the difference is 3. Find total level by subtracting
        //3 from the maximum allowed n, for this case n is 15, and level is 15-3=12

        totalLevelGoal=numberOfAllowN-3; //Total final goal level

    } // end CannonView constructor


    //=====================================================================================
    //Override onSizeChanged () method--a callback method for View objects called only once
    //=====================================================================================

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        //Set animation speed, text size,and other parameters
        // depending on the device width screen
        if(w>finalMaxiumWidthPixel){

            finalSpeedAnimation=7;//speed for animation
            setTextSize=100; //text size
            setStrokeWidthLine=6; //stroke width
            setMarginLeftRight=28;//total margin=left margin + right margin
            setCircleRadius =14; //circle radius
            //Log.e("faster speed","faster speed");
        }else{
            finalSpeedAnimation=4; //speed for animation
            setTextSize=50; //text size
            setStrokeWidthLine=4; //stroke width
            setMarginLeftRight=24; ////total margin=left margin + right margin
            setCircleRadius =8; //circle radius
            //Log.e("low speed","low speed");
        }


        //Log.e(TAG, "this is onSizeChanged() method");
        //Log.e("numer of index m", "value of w is " + w+" value of h is "+h);

        holdWidth=w; //Hold screen width
        finalHeight=h; //Hold Final game space height
        int numOfSquareinGrid=10; //Number of square in game space
        int showGridWidth=w-setMarginLeftRight; //Actual game space
        dePixel=(int)(showGridWidth/10); //Actual line size
        halfdePixel=dePixel/2; //Half of actual line size
        int left_Width=w-(dePixel*numOfSquareinGrid);//Re-calculate actual total margin
        //int left_Height=h-(dePixel*n_two);

        xShift=(int)left_Width/2; //Actual left margin
        yShift= xShift; //Actual right margin

        //Values needed to initialize other parameters
        int totalHalfSpaceGame=(int)(dePixel*numOfSquareinGrid)/2;
        int centerX=xShift+totalHalfSpaceGame;

        //Shifting down and final initial coordinate
        finalHalfYShiftDown=h-(numOfSquareinGrid*dePixel)-yShift;//Shift down
        finalYShiftDown=finalHalfYShiftDown/2 +20; //Final shift down
        finalYHeight=finalYShiftDown+holdWidth-2*xShift;//Final width in y coordinate

        //final x and y game space center coordinate
        save_x=centerX; //final x coordinate center
        save_y=finalYShiftDown+totalHalfSpaceGame; //final y coordinate center


        //Log.e("numer of index m", "xShift "+xShift+"  Yshift "+yShift);
        //Log.e("center of x and y", "center x "+save_x+"  center y "+save_y);

        // Configure Paint objects for drawing game elements
        textPaint.setTextSize(setTextSize); // Set text size
        textPaint.setAntiAlias(true); // Smoothes the text

        backgroundFirst.setColor(Color.WHITE); //Set background color as white

        backgroundSecond.setColor(Color.rgb(235, 234, 224)); // Set game space grid background color


        //Configure Gird Lines
        lineGrid.setStrokeWidth(setStrokeWidthLine);//Set grid line Stroke Width
        lineGrid.setColor(Color.rgb(195,207,180)); //set grid line color

        //Configure Line for Animation
        linePaintAnimation.setStyle(Paint.Style.STROKE);//Set Animation Line Stroke
        linePaintAnimation.setStrokeWidth(setStrokeWidthLine); //Set Animation Line Width Stroke
        linePaintAnimation.setColor(Color.rgb(242, 97, 255));//Set Animation Line color

        //Configure Line from User Swipe Finger
        paintSwipeFinger.setStyle(Paint.Style.STROKE);
        paintSwipeFinger.setStrokeWidth(setStrokeWidthLine);
        paintSwipeFinger.setAntiAlias(false);
        paintSwipeFinger.setColor(Color.rgb(242, 97, 255));

        //Configure Line for Actual Path when User Lose
        showLoseLine.setStyle(Paint.Style.STROKE);
        showLoseLine.setStrokeWidth(setStrokeWidthLine);
        showLoseLine.setColor(Color.rgb(191,66,49));

        //Configure initial and final circles
        circleInitial.setColor(0xffc9171e); //Set initial circle in red
        circleFinal.setColor(Color.rgb(82, 191, 255)); //cSet final circle color as blue

        //Configure circle for swiping and re-color final circle
        circleAnimationAndFinger.setColor(Color.rgb(242, 97, 255));//Set circle-swiping finger as pink
        circleWinFinal.setColor(Color.rgb(242,97,255));//Re-color win circle color


        //Initialize other variables
        loopTimerDrawing=true; //Allow to go inside statement when app launches
        timeLeftAllow=true; //Allow to loop one time when app launches
        loopForDrawing=false; //No draw allow when app launching
        killThread=false; //No thread for drawing is started when app launches
        allowCheckingWinLose=false; //Finger is not drawing so no checking for win or lose
        go_allow=false;//Initially don't allow to update for

    } // end method onSizeChanged


    //================================================================================
    //receiveTextview() method--get TextView and Animation reference from main activity
    //================================================================================

    //    public void receiveTextview(TextView receiveTextLevelGoal,TextView receiveTextLevelMe,TextView receiveTextLevelBest,Animation receiveAnimRotate){
    public void receiveTextview(TextView receiveTextLevelGoal,TextView receiveTextLevelMe,TextView receiveTextLevelBest){
        /*receiveTextview() parameters are initialized from main activity*/

        //Instantiate Textview and Animation object
        //whose references are given from main activity

        displayLevelGoal =receiveTextLevelGoal; //Get goal level textview
        displayLevelMe=receiveTextLevelMe; //Get user level textview
        displayLevelBest=receiveTextLevelBest; //Get user best level textview

        displayLevelGoal.setText(String.valueOf(totalLevelGoal));//Display total level
        displayLevelMe.setText(String.valueOf(saveUserCurrentLevel)); //Display user current level
        displayLevelBest.setText(String.valueOf(userCurrentBestLevel)); //Display best level

        //rotateAnimScore=receiveAnimRotate; //Get rotate animation

    }//end receiveTextview() method


    //================================================================================
    //clearLevelScoreBest() method--called from menu to clear Level, Score and Best
    //================================================================================

    public void clearLevelScoreBest(){
        /*called from onOptionsItemSelected() menu to clear Level, Score and Best
        * and to display the reset variables to main activity
        * */

        //Reset Level, Score and Best to Initial
        showCurrentLevel=0; //Reset level to 1
        n =initialMiniumN; //Reset n to 4
        userCurrentBestLevel=0; //Reset variable holdind best level from share...

        //Display the reset variables in main activity with Animation
        showGameScoreLevel(2); //Call this method to display reset variables

    } //end clearLevelScoreBest() method

    public void clearOnlyMe(){

        showCurrentLevel=0;
        n=initialMiniumN;
        showGameScoreLevel(5);

    }//end method clearOnlyMe

    //================================================================================
    //newGame() method-- reset all the screen elements and start a new game
    //================================================================================

    public void newGame(){

        //timeLeft = 6; // start the countdown at  6 seconds
        //totalElapsedTime = 0.0; // set the time elapsed to zero

        //Log.e(TAG, "this is newGame() method");
        //Reset Path for drawing path for lose, Swiping and path after Animation
        showPathWhenLose.reset(); //Reset Path for path showing user loses
        pathSwipeFinder.reset(); //Reset Path for path from swiping finger
        drawPathAfterAnimation.reset(); //Reset Path for path after animation

        //Initialize variables needed inside updatePositions()
        goes_right = false;
        goes_left = false;
        draw_right_left_up_down_canvas = false;
        goes_down = false;
        goes_up = false;
        go_draw = false;

        //Initialize variable nRandomTime
        if(n>=12 &&n<=17){
            //Set nRandomTime to random value. Ex: if n==12, nRandomTime could be 9,10,11
            generate_nRandomTime_number(1);
        }else if(n>=18&&n<=23) {
            generate_nRandomTime_number(2);
        }else{
            nRandomTime=n;
        }//end if statement
       // Log.e("new value of n", "new value of n"+nRandomTime);
        //Terminate thread if it is running
        if(killThread){
            //Log.e(TAG, "I am killing thread");
            memoThread.setRunning(false); // terminate thread
            loopTimerDrawing=true; //Allow to go inside the statement for turning on or off thread
            killThread=false;      //
        } //end if statement

        timeLeftAllow=false; // Don't allow to terminate loop at the beginning of New Game
        loopForDrawing=true; //Allow to loop again for drawing path from swiping
        allowTouch=false; //Do not allow using the touch screen


        //Reset ArrayList, Variables for random generator and drawing path
        sequencex.clear(); //Reset ArrayList holding x coordinate for the path
        sequencey.clear(); //Reset ArrayList holding y coordinate for the path
        initial_x=save_x; //Set to initial x coordinate
        initial_y=save_y; //set to initial y coordinate
        generate_random_number(); //Generate all random x and y coordinate for the pah
        //and store into ArrayList
        m=0; //Set Index needed to retrieve value from ArrayList
        count_Drawing =0; //Count number of line drew by the animation until get to n
        go_draw=false; //Initially don't allow to draw the animation
        go_allow=true; //Allow to update value needed for drawing
        initial_x_draw=sequencex.get(m); //Initialize it to x value with index m=0
        initial_y_draw=sequencey.get(m); //Initialize it to y value with index m=0
        final_x_draw=sequencex.get(m+1); //Initialize it to x value with index m=1
        final_y_draw=sequencey.get(m+1); //Initialize it to y value with index m=1
        xFinalValueinArrayList=sequencex.get(nRandomTime);
        yFinalValueinArrayList=sequencey.get(nRandomTime);
        m=m+1; //The nex index is 1,2,3...and so on
        check_direction(); //Check if animation is going to up,down,left or right direction
        drawPathAfterAnimation.moveTo(save_x,save_y); //Set initial x and y coordinates for
        //drawing path after animation

        //Check how many time path pass over the final point
        numberOfTimeOverFinal=0; //Set initially to zero
        numberOfTimeUserOverFinal=0;//Set initially to zero
        int xFinalPoint=sequencex.get(nRandomTime); //Retrieve final x coordinate point
        int yFinalPoint=sequencey.get(nRandomTime); //Retrieve final y coordinate point

        //Check number of time passing over final point
        for(int checkOverPoint=1; checkOverPoint<nRandomTime-1; checkOverPoint++){

            //Check if passing over the final point
            if(sequencex.get(checkOverPoint).equals(xFinalPoint) && sequencey.get(checkOverPoint).equals(yFinalPoint)){
                numberOfTimeOverFinal++; //Add number of time passing over the final point
            }

        }//End for loop


        //Reset Variables for Swiping Finger
        swipeDrawLine=false; //Initially don't draw path from swiping finger on screen
        allowCheckingWinLose=false; //Initially don't allow to check for lose and win
        holdX_for_path=save_x; //Set initial x coordinate for drawing path from swiping finger
        holdY_for_path=save_y; //Set initial y coordinate for drawing path from swiping finger
        holdUserLineX.clear(); //Reset ArrayList storing x coordinate for path from swiping finger
        holdUserLineY.clear(); //Reset ArrayList storing y coordinate for path from swiping finger
        holdUserLineX.add(save_x); //Add initial x coordinate to ArrayList
        holdUserLineY.add(save_y); //Add initial y coordinate to ArrayList
        drawFinalCircle=false; //Do not allow drawing final circle until animation end

        //check if final point get to initial point
        //if yes, draw smaller final circle because yo don't them to completely overlap
        //user want to distinguish initial point and final point
        if (sequencex.get(0).equals(sequencex.get(nRandomTime)) && sequencey.get(0).equals(sequencey.get(nRandomTime))) {

            //Allow final position as a small blue circle
            compareInitialFinalCircle =true;

        } else {
            //Allow draw default final blue circle
            compareInitialFinalCircle =false;
        }


        // starting a new thread game after the last game ended
        if (gameOver) {
            gameOver = false;
            memoThread = new MemoVectorThread(getHolder()); // create thread
            memoThread.start(); // start the game loop thread
        } //end gameOver if statement

    } // end method newGame

    //================================================================================
    //updatePositions() method-- called repeatedly by the MemoVectorThread  to update game elements
    //================================================================================

    private void updatePositions() {

        //Log.e(TAG, "this is timeleft value"+timeLeft);

        //Check if allow to update value for animation and drawing
        if (go_allow){

            //Do update if m<=n
            if (m <= nRandomTime) {
                //Log.e("numer of index m", "value of m is " + m);

                //If animation going right, update values
                if (goes_right) {

                    speed_X = speed_X + addingPlus; //keep going to right direction

                    //If animation get to final point of each direction
                    // find the next direction
                    if (speed_X > final_x_draw) {

                        //Initialize again for the next direction
                        goes_right = false;
                        goes_left = false;
                        draw_right_left_up_down_canvas = false;
                        goes_down = false;
                        goes_up = false;

                        //If m<n, find the coordinate for the next direction
                        if (m < nRandomTime) {
                            initial_x_draw = sequencex.get(m); //Retrieve x coordinate with index m
                            initial_y_draw = sequencey.get(m); //Retrieve y coordinate with index m
                            final_x_draw = sequencex.get(m + 1); //Retrieve x coordinate with index m+1
                            final_y_draw = sequencey.get(m + 1); //Retrieve y coordinate with index m+1

                            //Add the initial direction-point to ArrayList for drawing after animation
                            drawPathAfterAnimation.lineTo(initial_x_draw,initial_y_draw);

                            m = m + 1; //Update m value by 1
                            go_draw = true; //Allow to draw line after animation
                            count_Drawing = count_Drawing + 1; //Update by 1
                            check_direction(); //Get animation direction: either up,down, left or right

                            //If goes right again, update the x coordinate for animation
                            if (goes_right) {
                                //Log.e("goes_right again","goes right_again");
                                speed_X = speed_X + addingPlus;
                            }
                        } //end m<n
                        else{
                            //Else path reach to final point,so stop update
                            go_allow=false; //stop update
                            count_Drawing = count_Drawing +1; //Final update to rich n
                            //Add final coordinate for drawing path after animation is done
                            drawPathAfterAnimation.lineTo(sequencex.get(nRandomTime),sequencey.get(nRandomTime));
                        }
                    } //end speed_X > final_x_draw
            } //end goes_right

            //If animation going left, update values
            if (goes_left) {

                speed_X = speed_X + addingPlus; //keep going to left direction

                //If animation get to final point of each direction
                // find the next direction
                if (speed_X < final_x_draw) {
                    //Initialize again for the next direction
                    goes_right = false;
                    goes_left = false;
                    draw_right_left_up_down_canvas = false;
                    goes_down = false;
                    goes_up = false;

                    //If m<n, find the coordinate for the next direction
                    if (m < nRandomTime) {

                        initial_x_draw = sequencex.get(m);//Retrieve x coordinate with index m
                        initial_y_draw = sequencey.get(m); //Retrieve y coordinate with index m
                        final_x_draw = sequencex.get(m + 1);//Retrieve x coordinate with index m+1
                        final_y_draw = sequencey.get(m + 1);//Retrieve y coordinate with index m+1

                        //Add the initial direction-point to ArrayList for drawing after animation
                        drawPathAfterAnimation.lineTo(initial_x_draw,initial_y_draw);

                        m = m + 1; //Update m value by 1
                        go_draw = true; //Allow to draw line after animation
                        count_Drawing = count_Drawing + 1; //Update by 1
                        check_direction();//Get animation direction: either up,down, left or right

                        //If goes left again, update the x coordinate for animation
                        if (goes_left) {

                            speed_X = speed_X + addingPlus;
                        }

                    }//end m<n
                    else{
                        //Else path reach to final point,so stop update
                        go_allow=false; //stop update
                        count_Drawing = count_Drawing +1; //Final update to rich n
                        //Add final coordinate for drawing path after animation is done
                        drawPathAfterAnimation.lineTo(sequencex.get(nRandomTime),sequencey.get(nRandomTime));
                    }
                }//end speed_X > final_x_draw
            } //end goes_left

            //check if going down and update values
            if (goes_down) {

                speed_Y = speed_Y + addingPlus;  //keep going to down direction

                //If animation get to final point of each direction
                // find the next direction
                if (speed_Y > final_y_draw) {

                    //Initialize again for the next direction
                    goes_right = false;
                    goes_left = false;
                    draw_right_left_up_down_canvas = false;
                    goes_down = false;
                    goes_up = false;

                    //If m<n, find the coordinate for the next direction
                    if (m < nRandomTime) {

                        initial_x_draw = sequencex.get(m); //Retrieve x coordinate with index m
                        initial_y_draw = sequencey.get(m); //Retrieve y coordinate with index m
                        final_x_draw = sequencex.get(m + 1); //Retrieve x coordinate with index m+1
                        final_y_draw = sequencey.get(m + 1); //Retrieve y coordinate with index m+1

                        //Add the initial direction-point to ArrayList for drawing after animation
                        drawPathAfterAnimation.lineTo(initial_x_draw,initial_y_draw);

                        m = m + 1; //Update m value by 1
                        go_draw = true; //Allow to draw line after animation
                        count_Drawing = count_Drawing + 1; //Update by 1
                        check_direction(); //Get animation direction: either up,down, left or right

                        //If goes down again, update the x coordinate for animation
                        if (goes_down) {

                            speed_Y = speed_Y + addingPlus;
                        }
                    }//end m<n
                    else{

                        //Else path reach to final point,so stop update
                        go_allow=false; //stop update
                        count_Drawing = count_Drawing +1; //Final update to rich n
                        //Add final coordinate for drawing path after animation is done
                        drawPathAfterAnimation.lineTo(sequencex.get(nRandomTime),sequencey.get(nRandomTime));

                    }

                }//end speed_X > final_x_draw
            } //end goes_down


            //check if going up and update values
            if (goes_up) {

                speed_Y = speed_Y + addingPlus;  //keep going to up direction
                // Log.e("goes_up again", "goes goes_up again");

                //If animation get to final point of each direction
                // find the next direction
                if (speed_Y < final_y_draw) {

                    //Initialize again for the next direction
                    goes_right = false;
                    goes_left = false;
                    draw_right_left_up_down_canvas = false;
                    goes_down = false;
                    goes_up = false;

                    //If m<n, find the coordinate for the next direction
                    if (m < nRandomTime) {

                        initial_x_draw = sequencex.get(m); //Retrieve x coordinate with index m
                        initial_y_draw = sequencey.get(m); //Retrieve y coordinate with index m
                        final_x_draw = sequencex.get(m + 1); //Retrieve x coordinate with index m+1
                        final_y_draw = sequencey.get(m + 1); //Retrieve y coordinate with index m+1

                        //Add the initial direction-point to ArrayList for drawing after animation
                        drawPathAfterAnimation.lineTo(initial_x_draw,initial_y_draw);

                        m = m + 1; //Update m value by 1
                        go_draw = true; //Allow to draw line after animation
                        count_Drawing = count_Drawing + 1; //Update by 1
                        check_direction(); //Get animation direction: either up,down, left or right

                        //If goes up again, update the x coordinate for animation
                        if (goes_up) {

                            speed_Y = speed_Y + addingPlus;
                        }

                    }//end m<n
                    else{

                        //Else path reach to final point,so stop update
                        go_allow=false; //stop update
                        count_Drawing = count_Drawing +1; //Final update to rich n
                        //Add final coordinate for drawing path after animation is done
                        drawPathAfterAnimation.lineTo(sequencex.get(nRandomTime),sequencey.get(nRandomTime));
                    }
                }//end speed_X > final_x_draw
            } //end goes_up

        } //allow to go to n level
        else {
            //Stop drawing for Animation
            draw_right_left_up_down_canvas = false;

        }
    }//End go_allow


    // Allow to terminate thread for animation and start
    //again the same thread for swiping
    if(loopTimerDrawing) {
        if (timeLeftAllow) {
            timeLeftAllow=false;////Don't allow to come inside the statement again
            gameOver = true; // Allow to start a new thread again
            memoThread.setRunning(false); // Terminate thread

            //Allow the same thread to run again for
            //drawing line from swiping finger
            if (loopForDrawing) {
                // Log.e(TAG, "I am doing allowing redraw again");
                memoThread.setRunning(true); // Run thread
                loopTimerDrawing = false; //Don't allow to come inside the statement again
                loopForDrawing=false;//Don't allow to come inside the statement again
                killThread=true; //Allow to terminate thread when you start a new game
                allowTouch=true;//Allow user to swipe finger for drawing line
                pathSwipeFinder.moveTo(save_x, save_y);//Path from swiping finger starts
                // at center of space game
            } //end loopForDrawing
        }//End timeLeftAllow
    }//loopTimerDrawing

} // end method updatePositions

    //================================================================================
    // drawGameElements method-->called repeatedly by the MemoVectorThread  to draw game
    //                           elements with the given Canvas
    //================================================================================

    private void drawGameElements(Canvas canvas){

        // Clear the background to White color
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),backgroundFirst);

        //Set the game space background to backgroundSecond
        canvas.drawRect(xShift, finalYShiftDown, canvas.getWidth()-xShift, holdWidth-2*xShift+finalYShiftDown, backgroundSecond);

        //Draw horizontal background grid line
        for(int k=0; k<holdWidth- xShift; k=k+dePixel) {

            //Add and subtract 2 pixel to xshift because you want to cover the open corner, the joint of each vertical and horizontal line for the grid
            canvas.drawLine(xShift-2, finalYShiftDown+k, holdWidth+2- xShift, finalYShiftDown+k, lineGrid);
        }
        //Draw vertical background grid line
        for(int l=0; l<holdWidth- yShift; l=l+dePixel) {
            canvas.drawLine(xShift+l, finalYShiftDown, xShift+l,holdWidth-2*xShift+finalYShiftDown, lineGrid);
        }

        //Draw initial position as a small red circle
        canvas.drawCircle(save_x,save_y, setCircleRadius,circleInitial);

        // Draw the line animation, speed_X and speed_Y are changing
        if(draw_right_left_up_down_canvas) {
            canvas.drawLine(initial_x_draw, initial_y_draw, speed_X, speed_Y, linePaintAnimation);//Draw animation line
            canvas.drawCircle(speed_X, speed_Y, setCircleRadius, circleAnimationAndFinger); //Draw animation circle
        }//end if statement

        //Draw the line as static after animation completed at each direction
        if(go_draw) {
            canvas.drawPath(drawPathAfterAnimation, paintSwipeFinger);//Draw static path after animation
            //After all animation completed,
            if(count_Drawing==nRandomTime){
                drawFinalCircle=true;//Allow to draw the blue final circle
                showPathWhenLose.set(drawPathAfterAnimation);//Transfer drawPathAfterAnimation path to
                //showPathWhenLose path
                drawPathAfterAnimation.reset();//Reset drawPathAfterAnimation animation to empty
                timeLeftAllow=true; //Allow to terminate thread and starts it gain for swiping finger
                go_draw=false;
            }//end if statement

        } //end go_draw
        if(drawFinalCircle) {

            //check if final point get to initial point
            //if yes, draw smaller final circle because yo don't them to completely overlap
            //user want to distinguish initial point and final point
            if (compareInitialFinalCircle) {

                //Draw final position as a small blue circle
                canvas.drawCircle(xFinalValueinArrayList, yFinalValueinArrayList, setCircleRadius-2, circleFinal);
            } else {
                //Draw default final blue circle
                canvas.drawCircle(xFinalValueinArrayList, yFinalValueinArrayList, setCircleRadius, circleFinal);
            }

            //Draw path from swiping finger and check win and lose
            if (swipeDrawLine) {

                //Change color for initial circle
                canvas.drawCircle(save_x, save_y, setCircleRadius, circleWinFinal);

                //Draw circle and path from swiping finger
                canvas.drawCircle(holdX_for_path, holdY_for_path, setCircleRadius, circleAnimationAndFinger); //Draw circle
                canvas.drawPath(pathSwipeFinder, paintSwipeFinger); //Draw path

                //This is a frame by frame loop game so only
                // allow to check win and lose when user swipes finger
                if(allowCheckingWinLose) {

                    /*Description:
                    * First check if user get to the final point, if yes then check if user satisfies
                    * the number of time allow to pass over this final point, if yes then check if user
                    * follow the correct path from animation,if yes announce win, else lose
                    * */

                    //Check if user get to the final point
                    if (sequencex.get(nRandomTime).equals(holdX_for_path) && sequencey.get(nRandomTime).equals(holdY_for_path)) {

                        //Log.e(TAG, "checking the numberofuser and number of timer over lien " + numberOfTimeUserOverFinal + "  " + numberOfTimeOverFinal);

                        //Check user satisfies the number of time passes over the final point
                        if (numberOfTimeUserOverFinal == numberOfTimeOverFinal) {

                            /* Note:
                             * To use equals(), the length of each array has to be the same,
                             * so check if length is the same before comparing array with equal()
                            */

                            //Check path length from animation and user is the same
                            if (sequencex.size() == holdUserLineX.size()) {
                                //Log.e(TAG, "both size are equal");

                                //Check if content in each array is the same
                                if (sequencex.equals(holdUserLineX) && sequencey.equals(holdUserLineY)) {
                                    // Log.e(TAG, "every content is the same you win");
                                    //Announce win
                                    canvas.drawCircle(xFinalValueinArrayList, yFinalValueinArrayList, setCircleRadius, circleWinFinal);//Change final circle color
                                    //Update User and Best Level
                                    showCurrentLevel=showCurrentLevel+1;
                                    canvas.drawText(getResources().getString(
                                            R.string.win)+" "+String.valueOf(showCurrentLevel), holdWidth / 4-dePixel, holdWidth/2, textPaint);//Display "You win"
                                    allowTouch = false; //Stop allowing user from swiping finger
                                    memoThread.setRunning(false); // Terminate thread
                                    //Update TextView values in main UI with animation
                                    showGameScoreLevel(1);//1-->first option in the method
                                    n=n+1;
                                    if(showCurrentLevel==20){
                                        showGameScoreLevel(4);
                                    }

                                } else {
                                    // Log.e(TAG, "every content is not the same you lose");
                                    //Content in both array not the same, announce lose
                                    allowTouch = false; //Stop allowing user from swiping finger
                                    memoThread.setRunning(false); // Terminate thread
                                    canvas.drawCircle(save_x, save_y, setCircleRadius, circleInitial);//Change initial circle color
                                    canvas.drawPath(showPathWhenLose, showLoseLine);//Display actual path
                                    canvas.drawCircle(xFinalValueinArrayList,yFinalValueinArrayList, setCircleRadius, circleInitial);//Change final circle color
                                    checkLoseLevel();//Update current level and n
                                    if(showCurrentLevel==0) {
                                        canvas.drawText(getResources().getString(
                                                R.string.loseZero), (holdWidth / 2) - dePixel - halfdePixel, holdWidth / 2, textPaint);//Display "You lose"
                                    }else{

                                        canvas.drawText(getResources().getString(
                                                R.string.lose) + " " + String.valueOf(showCurrentLevel+1), (holdWidth / 4) - dePixel, holdWidth / 2, textPaint);//Display "You lose"
                                    }
                                    showGameScoreLevel(3);//3-->third option in the method
                                }
                            } else {
                                // Log.e(TAG, "you lose from equal size");
                                allowTouch = false;//Stop allowing user from swiping finger
                                memoThread.setRunning(false); // Terminate thread
                                canvas.drawPath(showPathWhenLose, showLoseLine);//Display actual path
                                canvas.drawCircle(save_x, save_y, setCircleRadius, circleInitial);//Display actual path
                                canvas.drawCircle(xFinalValueinArrayList,yFinalValueinArrayList, setCircleRadius, circleInitial);//Change final circle color
                                checkLoseLevel(); //Update current level and n
                                if(showCurrentLevel==0) {
                                    canvas.drawText(getResources().getString(
                                            R.string.loseZero), (holdWidth / 2) - dePixel - halfdePixel, holdWidth / 2, textPaint);//Display "You lose"
                                }else{

                                    canvas.drawText(getResources().getString(
                                            R.string.lose) + " " + String.valueOf(showCurrentLevel+1), (holdWidth / 4) - dePixel, holdWidth / 2, textPaint);//Display "You lose"
                                }
                                showGameScoreLevel(3);//3-->third option in the method
                            }

                        } else {

                            numberOfTimeUserOverFinal++;//Allow to pass one more time over final point
                        }

                    } //end check sequence

                    allowCheckingWinLose=false;//This allow code be executed only when user swipes finger
                    //and not in every frame
                }//end allowCheckingWinLose
            }//end swipeDrawLine
        }//end drawFinalCircle

    } // end method drawGameElements

    //================================================================================
    //showGameScoreLevel() method-->called from drawGameElements() to update TextView score and level
    //                              values with animation in the main GUI
    //================================================================================

    private void showGameScoreLevel(final int chooseWhatToShow)
    {

        /*
        * Note:
        * To update elements in main GUI, you have to use runOnUiThread() thread
        * */

        //Do update on main GUI thread
        activity.runOnUiThread(
                new Runnable() {
                    public void run()
                    {

                        switch (chooseWhatToShow) {

                            case 1:

                                //the high score and then store

                                //displayLevelMe.startAnimation(rotateAnimScore);//Set score with animation
                                displayLevelMe.setText(String.valueOf(showCurrentLevel));//Update best with new value

                                SharedPreferences.Editor highLevelEditor = saveHighBest.edit();
                                highLevelEditor.putInt(levelCurrent_key, showCurrentLevel);
                                highLevelEditor.commit();

                                //Check if user level is higher than the best level stored in sharePreferences
                                if (showCurrentLevel>userCurrentBestLevel) {
                                    //Update score with its new value in SharePreferences
                                    SharedPreferences.Editor highScoreEditor = saveHighBest.edit();
                                    highScoreEditor.putInt(scoreBest_key, showCurrentLevel);
                                    highScoreEditor.commit();
                                    userCurrentBestLevel=showCurrentLevel;
                                    displayLevelBest.setText(String.valueOf(showCurrentLevel));//Update best with new value
                                }
                                break;
                            case 2:
                                //Reset Level,Score,and Best to initial with animation
                                displayLevelMe.setText(String.valueOf(showCurrentLevel));//Set score with new value
                                displayLevelBest.setText(String.valueOf(showCurrentLevel));//Set level with new value

                                //Update score with its new value in SharePreferences
                                SharedPreferences.Editor highScoreEditor = saveHighBest.edit();
                                highScoreEditor.putInt(scoreBest_key, showCurrentLevel);
                                highScoreEditor.putInt(levelCurrent_key, showCurrentLevel);
                                highScoreEditor.commit();


                                break;
                            case 3:
                                //Substract Level by one
                                displayLevelMe.setText(String.valueOf(showCurrentLevel));//Set score with new value

                                //Update score with its new value in SharePreferences
                                SharedPreferences.Editor subsLevelEditor = saveHighBest.edit();
                                subsLevelEditor.putInt(levelCurrent_key, showCurrentLevel);
                                subsLevelEditor.commit();
                                break;
                            case 4:
                                //Show dialog fragment
                                // DialogFragment to display quiz stats and start new quiz
                                final DialogFragment gameResult =
                                        new DialogFragment()
                                        {
                                            // create an AlertDialog and return it
                                            @Override
                                            public Dialog onCreateDialog(Bundle bundle)
                                            {
                                                // create dialog displaying String resource for dialogTitle
                                                AlertDialog.Builder builder =
                                                        new AlertDialog.Builder(getActivity());
                                                builder.setTitle(getResources().getString(R.string.dialogTitle));

                                                // display Message
                                                builder.setMessage(getResources().getString(
                                                        R.string.messageFinalLevel));

                                                builder.setPositiveButton(R.string.reset_game_new,
                                                        new DialogInterface.OnClickListener(){
                                                            // called when "New Game" Button is pressed
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                dialogIsDisplayed = false;
                                                                clearLevelScoreBest();
                                                            }
                                                        } // end anonymous inner class
                                                ); // end call to setPositiveButton

                                                return builder.create(); // return the AlertDialog
                                            } // end method onCreateDialog
                                        }; // end DialogFragment anonymous inner class

                                gameResult.setCancelable(false);//modal dialog
                                gameResult.show(activity.getFragmentManager(),"congratulation");
                                break;
                            case 5:
                                //Reset Level,Score,and Best to initial with animation
                                displayLevelMe.setText(String.valueOf(showCurrentLevel));//Set score with new value

                                //Update score with its new value in SharePreferences
                                SharedPreferences.Editor highScoreEditors = saveHighBest.edit();
                                highScoreEditors.putInt(levelCurrent_key, showCurrentLevel);
                                highScoreEditors.apply();

                        }//end switch
                    }
                } // end Runnable
        ); // end call to runOnUiThread
    } // end method showGameScoreLevel

    private void checkLoseLevel(){

        //Check allowed n is not below 4
        if(n<=initialMiniumN){
            //Reset variables to initial
            showCurrentLevel=0;
            n=initialMiniumN;
        }else{
            //Bring Score down by n, and Level by 1
            showCurrentLevel=showCurrentLevel-1; //bring level by 1
            n=n-1;  //Set Level down by one level
        }

    }//end method checkLoseLevel

    //================================================================================
    //stopGame() method-->called by GameFragment's onPause method stops the game
    //================================================================================

    public void stopGame(){
        // Log.e(TAG, "this is stopGame()");
        if (memoThread != null) {
            memoThread.setRunning(false); // Tell thread to terminate
        }

    }//End stopGame()

    //================================================================================
    //sufaceChanged(),surfaceCreated(),surfaceDestroyed()-->3-methods of
    //SurfaceHolder.Callback you have to implement
    //================================================================================

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,int width, int height){

        // Log.e(TAG, "this is surfaceChanged()");
        //You don't do anything when the Surface size change since we have set screen orientation
        //as portrait in manifest file
    }

    // Called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        //Log.e(TAG, "this is surfaceCreated()");
        //Here you run the first frame, only one fame, loop-game for drawing grid and initial circle
        // after the surface is created
        if (!dialogIsDisplayed){

            memoThread = new MemoVectorThread(holder); // create thread
            memoThread.setRunning(true); // start game running--start first gaming by assigning
            //true in setRunning()
            memoThread.start(); // start the game loop thread---or start the thread class
            // Log.e(TAG, "just created a new thread");

        }
    }

    // Called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

        //Log.e(TAG, "this is surfaceDestroyed()");
        // Ensure that thread terminates properly
        boolean retry = true;
        memoThread.setRunning(false); // Terminate memoThread

        while (retry)
        {
            try
            {
                //join()-->Blocks the current Thread (Thread.currentThread()) until the receiver
                // finishes its execution and dies.
                memoThread.join(); // wait for memoThread to finish
                retry = false;
                //Log.e(TAG, "thread is finished");
            }
            catch (InterruptedException e)
            {
                // Log.e(TAG, "Thread interrupted", e);
            }
        }

    } // end method surfaceDestroyed

    //================================================================================
    //onTouchEvent() method-->called when the user touches the screen in this Activity
    //================================================================================

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {

        //Check if onTouchEvent is allowed to proceed  after animation
        if(allowTouch) {

            int action = e.getAction();//Get the index of type of action

            switch (action) {

                //Finger is Down on screen
                case MotionEvent.ACTION_DOWN:
                    startXcoFingerDown =(int)e.getX();//Get finger x coordinate
                    startYcoFingerDown =(int)e.getY();//Get finger y coordinate
                    break;
                //Finger is moving on screen
                case MotionEvent.ACTION_MOVE:
                    //When finger is moving, do nothing
                    break;
                //Finger is UP
                case MotionEvent.ACTION_UP:
                    //When finger is up, get its x and y coordinate
                    finalXcoFingerUp =(int)e.getX();//Get finger x coordinate
                    finalYcoFingerUp =(int)e.getY(); //Get finger y coordinate

                    swipeDrawLine=true;//Allow to draw path from user swipe finger
                    allowCheckingWinLose=true; //Allow to check for lose and win

                    //Determine if a swipe occurs
                    //Find difference in pixel between finger down and up coordinates
                    differenceFingerX = finalXcoFingerUp - startXcoFingerDown;//Diff between x coordinates
                    differenceFingerY = finalYcoFingerUp - startYcoFingerDown; //Diff between y coordinates

                    //Determine if swipe occurs for left or right
                    if(Math.abs(differenceFingerX)>Math.abs(differenceFingerY)){

                        //Check if difference is big enough to be considered a swipe:
                        // difference>20 pixels
                        if(Math.abs(differenceFingerX)>20){
                            //Right Swipe occurs
                            if(differenceFingerX >0){
                                //Log.e(TAG, "Swiping to Right");
                                //Check x coordinate for path is inside game space
                                if(holdX_for_path <=holdWidth-dePixel-xShift) {
                                    pathSwipeFinder.lineTo(holdX_for_path + dePixel, holdY_for_path);//Path from swiping finger right
                                    holdX_for_path = holdX_for_path + dePixel; //Update x coordinate from user swipe finger
                                    holdUserLineX.add(holdX_for_path);//Save the user finger x coordinate
                                    holdUserLineY.add(holdY_for_path); //Save the user finger y coordinate

                                }
                            }else{ //Left Swipe occurs

                                //Check x coordinate for path is inside game space
                                if(holdX_for_path >=xShift+halfdePixel) {
                                    pathSwipeFinder.lineTo(holdX_for_path - dePixel, holdY_for_path);//Path from swiping finger left
                                    holdX_for_path = holdX_for_path - dePixel; //Update x coordinate from user swipe finger
                                    holdUserLineX.add(holdX_for_path);//Save the user finger x coordinate
                                    holdUserLineY.add(holdY_for_path); //Save the user finger y coordinate
                                    //Log.e(TAG, "Swiping to left");

                                }
                            } //End differenceFingerX >0

                        }//end Math.abs(differenceFingerX)>20

                    }else{ //Determine if swipe occurs for up or down
                        if(Math.abs(differenceFingerY)>20){

                            //Down Swipe occurs
                            if(differenceFingerY >0){

                                //Check y coordinate for path is inside game space
                                if(holdY_for_path <=finalYHeight-halfdePixel) {
                                    pathSwipeFinder.lineTo(holdX_for_path, holdY_for_path + dePixel);//Path from swiping finger down
                                    holdY_for_path = holdY_for_path + dePixel;//Update y coordinate from user swipe finger
                                    holdUserLineX.add(holdX_for_path);//Save the user finger x coordinate
                                    holdUserLineY.add(holdY_for_path); //Save the user finger y coordinate
                                    // Log.e(TAG, "Swiping Down");
                                }

                            }else{//Up Swipe occurs

                                //Check y coordinate for path is inside game space
                                if(holdY_for_path >=finalYShiftDown+halfdePixel) {
                                    pathSwipeFinder.lineTo(holdX_for_path, holdY_for_path - dePixel);//Path from swiping finger up
                                    holdY_for_path = holdY_for_path - dePixel;//Update y coordinate from user swipe finger
                                    holdUserLineX.add(holdX_for_path);//Save the user finger x coordinate
                                    holdUserLineY.add(holdY_for_path); //Save the user finger y coordinate

                                    //Log.e(TAG, "Swiping Up");
                                }
                            } //End differenceFingerY >0

                        } //End Math.abs(differenceFingerY)

                    } //End checking Math.abs(differenceFingerX)>Math.abs(differenceFingerY)

                    break;

            }//end switch statement

        } //end if allowTouch

        return true;

    } // end method onTouchEvent()

    //================================================================================
    //MemoVectorThread() class-->this is the frame by frame game loop,
    //running updatePositions() and drawGameElements() repeatedly
    //================================================================================

    private class MemoVectorThread extends Thread
    {

        private SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public MemoVectorThread(SurfaceHolder holder)  //the argument given to constructor is SurfaceHolder
        {
            surfaceHolder = holder;
            // setName("memoThread"); //Sets the name of the Thread.
        }

        // changes running state
        public void setRunning(boolean running)
        {
            threadIsRunning = running; //see the default value above
        }

        // controls the game loop
        @Override
        public void run() //Calls the run() method of the Runnable object the receiver holds.
        {
            Canvas canvas = null; // used for drawing
            //Log.e(TAG, "this is run()");
            while (threadIsRunning)
            {
                try
                {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas(null); //locking our canvas and Start editing the pixels in the surface.
                    //Start editing the pixels in the surface.

                    // lock the surfaceHolder for drawing
                    synchronized(surfaceHolder)
                    {
                        updatePositions(); // update game state
                        drawGameElements(canvas); // draw using the canvas

                    }
                }
                finally
                {

                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);

                }
            } // end while
        } // end method run
    } // end nested class CannonThread

    //================================================================================
    //generate_nRandomTime_number() method-->this generate new value for n
    //================================================================================
    private void generate_nRandomTime_number(int changeValuen){

        //Random rand_value=new Random();
        //int selectValue=rand_value.nextInt(2);//Random number could be 0,1,2

        int selectValue=getRandom.nextInt(2);//Random number could be 0,1

        if(changeValuen==1) {
            switch (selectValue) {

                case 0:
                    //nRandomTime=n-5
                    nRandomTime = n - 5;
                    break;
                case 1:
                    //nRandomTime=n-4
                    nRandomTime = n - 4;
                    break;
            } //End switch
        }else if(changeValuen==2){

            switch (selectValue) {

                case 0:
                    //nRandomTime=n-8
                    nRandomTime = n - 8;
                    break;
                case 1:
                    //nRandomTime=n-7
                    nRandomTime = n - 7;
                    break;
            } //End switch

        } //end else if


    } //end method generate_nRandomTime_number()

    //================================================================================
    //generate_random_number() method-->this generate the x and y coordinates for the path
    //================================================================================

    private void generate_random_number(){

        sequencex.add(save_x);//Add initial x coordinate into ArrayList
        sequencey.add(save_y); //Add initial y coordinateinto ArrayList
        int step_x=0; //Auxiliary  Variable
        int step_y=0; //Auxiliary variable
        int p_xel=dePixel;//line length in pixel between each coordinate

        //Generate n number of coordinates forming the path
        for(int i=0; i<nRandomTime; i++) {

            int index = getRandom.nextInt(4);//Get a number, 0,1,2,3 uniformly distribute over 0-4
            //variable index could be 0,1,2,3
            //0=Right direction coordinate
            //1=Down direction coordinate
            //2=Left direction  coordinate
            //3=Up direction coordinate

            switch (index){

                case 0:
                    //right direction coordinate
                    step_x=p_xel;
                    step_y=0;
                    break;
                case 1:
                    //Down direction coordinate
                    step_x=0;
                    step_y=p_xel;
                    break;
                case 2:
                    //Left direction coordinate
                    step_x=-1*p_xel;
                    step_y=0;
                    break;
                case 3:
                    //Right direction coordinate
                    step_x=0;
                    step_y=-1*p_xel;
                    break;

            } //End switch

           /*  Note:
           *   The path is formed of many lines
           *      line initial point--------------line final point
           *   (initial_x,initial_y)-------------(new_x,new_y)
           *
           * */
            new_x=initial_x+step_x;//New x coordinate
            new_y=initial_y+step_y;//New Y coordinate

            /*Note: Compensate for Repetition
            * (1) Overlap occurs, the next new_x and new_y have same values as initial_x and initial_y
            *  EX: first line travels right, second line overlap the first line, consequently lines cannot be distinguish
            *  Solution: generate new random that only gives one direction among 3 directions
            *  EX: first line travels right, second line compensated by either continue traveling right, up or down
            * (2) line crosses one of the 4 boundary of the grid game space
            *  Ex: first line travels right and second line travels right too and crosses right side of game space
            *  Solution: generate a new random that only gives one direction among 2 directions
            *  Ex: first line travels right, and second line travels right too and cross, so second line compensated by either
            *      travels up or down
            *  (3) First line travels right, second line crosses both right and up side, so second
            *    line compensation is traveling down
            * */

            //Compensate if there is repetition or cross boundary
            if(i>0) {

                //Compensate for repetition
                //Check for line overlapping
                if (new_x == sequencex.get(i - 1) && new_y == sequencey.get(i - 1)) {
                    // Log.e("checking compensation", "I am inside checking " + "I am inside");

                    if (sequencex.get(i) > sequencex.get(i - 1)) {
                        // Log.e("checking compensation", "I am inside checking " + "iam right");
                        //if line travels right compensates by keep traveling to right, up or down
                        int getDirection[] = generate_3_random(1);//get coordinates of one direction among 3 directions
                        new_x = initial_x+getDirection[0];//New x compensated coordinate
                        new_y = initial_y+getDirection[1];//New y compensated coordinate

                    } else if (sequencex.get(i) < sequencex.get(i - 1)) {
                        // Log.e("checking compensation", "I am inside checking " + "iam left");
                        //if line travels left compensate by keep traveling to left, up or down
                        int getDirection[] = generate_3_random(2);//get coordinates of one direction among 3 directions
                        new_x = initial_x+getDirection[0];//New x compensated coordinate
                        new_y =initial_y +getDirection[1];//New y compensated coordinate

                    } else if (sequencey.get(i) < sequencey.get(i - 1)) {
                        // Log.e("checking compensation", "I am inside checking " + "iam up");
                        //if line travels up compensate by keep traveling up, right or left
                        int getDirection[] = generate_3_random(3);//get coordinates of one direction among 3 directions
                        new_x = initial_x+getDirection[0];//New x compensated coordinate
                        new_y = initial_y+getDirection[1];//New y compensated coordinate

                    } else if (sequencey.get(i) > sequencey.get(i - 1)) {
                        //if line travels down continue traveling down,right, or left
                        //Log.e("checking compensation", "I am inside checking " + "iam down");
                        int getDirection[] = generate_3_random(4);//get coordinates of one direction among 3 directions
                        new_x = initial_x+getDirection[0]; //New x compensated coordinate
                        new_y = initial_y+ getDirection[1];//New y compensated coordinate

                    }

                } //End checking repetition compensation

                //Cross Boundary Compensation
                //Check for cross boundary
                if((new_x> holdWidth - xShift)||new_x<xShift){

                    //if line crosses right side
                    if(new_x>holdWidth-xShift){
                        //Log.e("checking bouncing", "I am going through right side " + "right side");
                        //if line travels to right compensated by traveling up or down
                        if (sequencex.get(i) > sequencex.get(i - 1)){ //line travels to right

                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getWallDirection[] = generate_Wall_random(1); //1-->Traveling either up or down
                                new_x = initial_x + getWallDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getWallDirection[1]; //New y compensated coordinate
                                // Log.e("right side bouncing", "I am correcting " + "up or down");
                            } else if((sequencey.get(i)-finalYShiftDown)<halfdePixel+2){
                                //Go Down
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y +dePixel; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "down");

                            }else{
                                //Go Up
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y +-dePixel; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "up");

                            }

                        } //end compensation for crossing right side
                        else if(sequencey.get(i)>sequencey.get(i-1)){
                            //if line travels down, keep travelling down or left
                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(1); //1-->traveling either down or left
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "down or left");
                            } else{
                                //else travel to left
                                new_x = initial_x + -dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "left_2 for right side");
                            }

                        }else{
                            //if line travels up, keep traveling up or left
                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(2); //1-->traveling either up or left
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "up or left");
                            }else{
                                //else travel to left
                                new_x = initial_x + -dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                //Log.e("right side bouncing", "I am correcting " + "left_3 for right side");
                            }
                        }


                    }else{ //if line crosses left side

                        // Log.e("checking bouncing", "I am going through left side " + "left side");
                        if (sequencex.get(i) < sequencex.get(i - 1)) {

                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getWallDirection[] = generate_Wall_random(1);//1-->traveling either up or down
                                new_x = initial_x + getWallDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getWallDirection[1]; //New y compensated coordinate
                                // Log.e("left side bouncing", "I am correcting " + "up or down");
                            }else if((sequencey.get(i)-finalYShiftDown)<halfdePixel+2){
                                //travel down
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y +dePixel; //New y compensated coordinate
                                //Log.e("left side bouncing", "I am correcting " + "down");

                            }else{
                                //travel up
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y + -dePixel; //New y compensated coordinate
                                // Log.e("checking bouncing", "I am correcting " + "up");

                            }
                        }else if(sequencey.get(i)>sequencey.get(i-1)){ //line travels down
                            //line travels down, keep traveling either down or right
                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(3); //1-->traveling either down or right
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                // Log.e("left side bouncing", "I am correcting " + "down or right");
                            }else{
                                //else travel right
                                new_x = initial_x + dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                // Log.e("left side bouncing", "I am correcting " + "right_2 for left side");
                            }

                        }else{//line travels up
                            if(((sequencey.get(i)-finalYShiftDown)>halfdePixel+2)&&(Math.abs(sequencey.get(i)-finalYHeight)>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(4); //1-->traveling either up or right
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                // Log.e("left side bouncing", "I am correcting " + "up or right");
                            }else{
                                //else travel right
                                new_x = initial_x + dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                // Log.e("left side bouncing", "I am correcting " + "right_3 for left side");
                            }
                        }
                    }

                }else if((new_y> finalYHeight)||new_y<finalYShiftDown){

                    //if line crosses down or up side

                    if(new_y>finalYHeight){
                        //Log.e("down side bouncing", "I am going through down side " + "down side");

                        //line travels down, so travels either left or right
                        if (sequencey.get(i) > sequencey.get(i - 1)) { //line travels down

                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getWallDirection[] = generate_Wall_random(2); //2-->traveling either right or left
                                new_x = initial_x + getWallDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getWallDirection[1]; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "left or right");
                            }else if((sequencex.get(i)-xShift)<halfdePixel+2){
                                //travel right
                                new_x = initial_x +dePixel ; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                // Log.e("down side bouncing", "I am correcting " + "right");

                            }else{
                                //travel left
                                new_x = initial_x +-dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "left");

                            }
                        }else if(sequencex.get(i)>sequencex.get(i-1)){
                            //line travels to right
                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(4); //1-->traveling either up or right
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "up or right");
                            }else{

                                //else travel up
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y + -dePixel; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "up_2 for down side");
                            }

                        }else{

                            //line travels to left
                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(2); //1-->traveling either up or left
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "up or left");
                            }else{

                                //else travel up
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y + -dePixel; //New y compensated coordinate
                                //Log.e("down side bouncing", "I am correcting " + "up_3 for down side");
                            }

                        }

                    }else{ //if line crosses up side, travels either right or left

                        // Log.e("checking bouncing", "I am going through up side " + "up side");
                        if (sequencey.get(i) < sequencey.get(i - 1)) {
                            //travel right or left

                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getWallDirection[] = generate_Wall_random(2);//2-->traveling either right or left
                                new_x = initial_x + getWallDirection[0];  //New x compensated coordinate
                                new_y = initial_y + getWallDirection[1]; //New y compensated coordinate
                                //Log.e("up side bouncing", "I am correcting " + "left or right");
                            }else if((sequencex.get(i)-xShift)<halfdePixel+2){
                                //Go right
                                new_x = initial_x +dePixel ; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                // Log.e("up side bouncing", "I am correcting " + "right");

                            }else{ //Go left
                                new_x = initial_x +-dePixel; //New x compensated coordinate
                                new_y = initial_y; //New y compensated coordinate
                                //Log.e("upside bouncing", "I am correcting " + "left");

                            }


                        }else if(sequencex.get(i)>sequencex.get(i-1)){
                            //line travels to right
                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(3); //1-->traveling either down or right
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                //Log.e("up side bouncing", "I am correcting " + "down or right");
                            }else{
                                //travel down
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y +dePixel; //New y compensated coordinate
                                //Log.e("up side bouncing", "I am correcting " + "down_2 for up side");

                            }

                        }else{
                            //line travels to left
                            if(((sequencex.get(i)-xShift)>halfdePixel+2)&&(Math.abs(sequencex.get(i)-(holdWidth-xShift))>halfdePixel+2)) {
                                //get coordinates of one direction among 2 directions
                                int getTwoDirection[] = generate_Two_random(1); //1-->traveling either down or left
                                new_x = initial_x + getTwoDirection[0]; //New x compensated coordinate
                                new_y = initial_y + getTwoDirection[1]; //New y compensated coordinate
                                // Log.e("up side bouncing", "I am correcting " + "down or left");
                            }else{
                                //travel down
                                new_x = initial_x; //New x compensated coordinate
                                new_y = initial_y +dePixel; //New y compensated coordinate
                                //Log.e("up side bouncing", "I am correcting " + "down_3 for up side");
                            }
                        }

                    }

                }
                //end compensating cross boundary



            } //end if (i>0)


            sequencex.add(new_x); //Add new x coordinate to ArrayList
            sequencey.add(new_y); //Add new y coordinate to ArrayList
            initial_x=new_x; //Update initial_x
            initial_y=new_y; //Update initial_y


        }//end for loop


    } //end method generate_random_number()

    //================================================================================
    // generate_3_random method-->this generate and return the x and y coordinates of
    // one direction from 3 directions
    //================================================================================

    private int[] generate_3_random(int selectDirection) {

        //selectDirection:
        //1-->travel right
        //2-travel left
        //3-travel up
        //4-travel down

        int r_step_x = 0;
        int r_step_y = 0;
        int r_p_xel = dePixel;

        //Random rr = new Random();//Instantiate a random object
        //int indexs = rr.nextInt(3); //Get a number, 0,1,2 uniformly distribute over 0-3

        int indexs = getRandom.nextInt(3); //Get a number, 0,1,2 uniformly distribute over 0-3
        //Line travels right, line travels either to right, up or down
        if(selectDirection==1) {
            switch (indexs) {

                case 0:
                    //travel right
                    r_step_x = r_p_xel;
                    r_step_y = 0;
                    break;
                case 1:
                    //travel up
                    r_step_x = 0;
                    r_step_y =r_p_xel;
                    break;
                case 2:
                    //travel down
                    r_step_x = 0;
                    r_step_y = -r_p_xel;
                    break;

            }//end switch
        }else if(selectDirection==2){ //line travels left, line travels either left or up or down
            switch (indexs) {

                case 0:
                    //travel left
                    r_step_x = -r_p_xel;
                    r_step_y = 0;
                    break;
                case 1:
                    //travel up
                    r_step_x = 0;
                    r_step_y =r_p_xel;
                    break;
                case 2:
                    //travel down
                    r_step_x = 0;
                    r_step_y = -r_p_xel;
                    break;

            }//end switch

        }else if(selectDirection==3){ //line travels up, line travels either up, left or right
            switch (indexs) {

                case 0:
                    //travel right
                    r_step_x = r_p_xel;
                    r_step_y = 0;
                    break;
                case 1:
                    //travel left
                    r_step_x = -r_p_xel;
                    r_step_y =0;
                    break;
                case 2:
                    //travel up
                    r_step_x = 0;
                    r_step_y = -r_p_xel;
                    break;

            }//end switch

        }else if(selectDirection==4) {//line travels down, line travels either down, left or right
            switch (indexs) {

                case 0:
                    //travel right
                    r_step_x = r_p_xel;
                    r_step_y = 0;
                    break;
                case 1:
                    //travel left
                    r_step_x = -r_p_xel;
                    r_step_y = 0;
                    break;
                case 2:
                    //travel down
                    r_step_x = 0;
                    r_step_y = r_p_xel;
                    break;

            }//end switch


        }//end if

        return new int[]{r_step_x, r_step_y}; //Return new x and y coordinate

    } //end method generate_3_random()

    //================================================================================
    // generate_Wall_random method-->this generate and return the x and y coordinates of
    // one direction from 2 directions
    //================================================================================

    private int[] generate_Wall_random(int selectWallDirection){

        //selectWallDirection:
        //1-->travel right
        //2-->travel left
        //3-->travel up
        //4-->travel down

        int wall_step_x = 0;
        int wall_step_y = 0;
        int wall_p_xel = dePixel;

        //Random wall_r = new Random();//Instantiate a random object
        //int wall_index = wall_r.nextInt(2); //Get a number, 0,1 uniformly distribute over 0-2

        int wall_index = getRandom.nextInt(2); //Get a number, 0,1 uniformly distribute over 0-2

        //line travels right or left, line travels either up or down
        if(selectWallDirection==1) {
            switch (wall_index) {

                case 0:
                    //travel down
                    wall_step_x = 0;
                    wall_step_y =wall_p_xel;
                    break;
                case 1:
                    //travel up
                    wall_step_x = 0;
                    wall_step_y =-wall_p_xel;
                    break;

            }
        }else if(selectWallDirection==2){ //line travels up or down, line travels either left or right
            switch (wall_index) {

                case 0:
                    //travel right
                    wall_step_x = wall_p_xel;
                    wall_step_y =0;
                    break;
                case 1:
                    //travel left
                    wall_step_x = -wall_p_xel;
                    wall_step_y =0;
                    break;

            }

        }
        return new int[]{wall_step_x, wall_step_y}; //Return new x and y coordinate

    } //end generate_Wall_random method

    //================================================================================
    // generate_Two_random() method-->this generate and return the x and y coordinates of
    // one direction from 2 directions
    //================================================================================

    private int[] generate_Two_random(int selectTwoDirection){

        //1-->travel right
        //2-->travel left
        //3-->travel up
        //4-->travel down

        int two_step_x = 0;
        int two_step_y = 0;
        int two_p_xel = dePixel;

        //Random two_r = new Random();//Instantiate a random object
        //int two_index = two_r.nextInt(2); //Get a number, 0,1 uniformly distribute over 0-2

        int two_index = getRandom.nextInt(2); //Get a number, 0,1 uniformly distribute over 0-2

        if(selectTwoDirection==1) { //for right side: line travels down, line travel either left, or down
            switch (two_index) {

                case 0:
                    //travel down
                    two_step_x = 0;
                    two_step_y=two_p_xel;
                    break;
                case 1:
                    //travel left
                    two_step_x= -two_p_xel;
                    two_step_y =0;
                    break;

            }
        }else if(selectTwoDirection==2){ //for right side: line travels up, line travels either left or up
            switch (two_index) {

                case 0:
                    //travel up
                    two_step_x = 0;
                    two_step_y=-two_p_xel;
                    break;
                case 1:
                    //travel left
                    two_step_x=-two_p_xel;
                    two_step_y=0;
                    break;

            }

        }else if(selectTwoDirection==3) { //for left side: line travels down, line travels either down or right
            switch (two_index) {

                case 0:
                    //travel right
                    two_step_x = two_p_xel;
                    two_step_y = 0;
                    break;
                case 1:
                    //travel down
                    two_step_x = 0;
                    two_step_y = two_p_xel;
                    break;

            }
        }else if(selectTwoDirection==4) { //for left side: line travels up, line travels either up or right
            switch (two_index) {

                case 0:
                    //travel right
                    two_step_x = two_p_xel;
                    two_step_y = 0;
                    break;
                case 1:
                    //travel up
                    two_step_x =0;
                    two_step_y = -two_p_xel;
                    break;

            }
        }

        return new int[]{two_step_x, two_step_y}; //Return new x and y coordinate

    } //end generate_Wall_random method

    //================================================================================
    // check_direction() method-->this check the direction for the animation, line travels
    // either right, left, up or down
    //================================================================================

    private void check_direction() {

        speed_X = initial_x_draw;
        speed_Y = initial_y_draw;
        //check if direction is right or left
        if(initial_y_draw==final_y_draw){

            //check if direction is right
            if(final_x_draw>initial_x_draw) {
                addingPlus = finalSpeedAnimation; //animation speed to right
                goes_right= true; //direction is right
                draw_right_left_up_down_canvas =true; //allow draw for right and left direction
            }else{ //check if direction is left
                addingPlus = -finalSpeedAnimation; //animation speed to the left
                goes_left = true; //direction is right
                draw_right_left_up_down_canvas =true; //allow draw for right and left direction

            }

        } //check if direction is up or down
        else if(initial_x_draw==final_x_draw){
            //check if direccion is down
            if(final_y_draw>initial_y_draw) {
                addingPlus = finalSpeedAnimation;
                goes_down = true;
                draw_right_left_up_down_canvas =true;
            }else{ //check if direction is up
                addingPlus = -finalSpeedAnimation;
                goes_up = true;
                draw_right_left_up_down_canvas =true;
            }
        }
    }//end check_direction() method


}//End GameEngineView class
