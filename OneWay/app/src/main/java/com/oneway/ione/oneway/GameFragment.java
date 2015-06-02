package com.oneway.ione.oneway;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment {

    private GameEngineView memovectorView; // custom view to display the game

    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        // get the VectoryView
        memovectorView = (GameEngineView) view.findViewById(R.id.memoVectorView);

        return view;
    }

    // set up volume control once Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }


    // when MainActivity is paused, CannonGameFragment terminates the game
    @Override
    public void onPause()
    {
        super.onPause();
        memovectorView.stopGame(); // terminates the game
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


}
