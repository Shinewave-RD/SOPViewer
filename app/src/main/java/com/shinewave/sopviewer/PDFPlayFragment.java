package com.shinewave.sopviewer;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.OutlineActivityData;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link PDFPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PDFPlayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private IFragmentInteraction mListener;

    private RelativeLayout rlPullRect;
    private MuPDFReaderView mDocView;
    private MuPDFCore core;
    private GestureDetector mGestureDetector;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PDFPlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PDFPlayFragment newInstance(String param1, String param2) {
        PDFPlayFragment fragment = new PDFPlayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PDFPlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pdfplay, container, false);
        rlPullRect = (RelativeLayout) view.findViewById(R.id.rlPullRect);
        showPDF(mParam1);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IFragmentInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showPDF(String path)
    {
        try {
            core = openFile(path);
            if (core == null) {
                return;

            } else {
                mGestureDetector = new GestureDetector(getActivity(), onGestureListener);
                mDocView = new MuPDFReaderView(getActivity()) {
                    @Override
                    protected void onMoveToChild(int i) {
                        if (core == null)
                            return;
                        super.onMoveToChild(i);
                    }
                };
                mDocView.setAdapter(new MuPDFPageAdapter(getActivity(), null, core));
                mDocView.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return false;
                    }
                });
                rlPullRect.addView(mDocView);
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private MuPDFCore openFile(String path) {
        try {
            core = new MuPDFCore(getActivity(), path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            return null;
        }
        return core;
    }

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

    };

}
