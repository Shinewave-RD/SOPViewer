package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IFragmentInteraction}
 * interface.
 */
public class PlayItemFragment extends Fragment implements AbsListView.OnItemClickListener {

    protected static final String TAG = PlayItemFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private IFragmentInteraction mListener;

    private PlayItemAdapter adapter;
    private static Context ctext;
    public static int nowSeq = -1;
    public static List<PlayListItem> nowPlayItem;
    public static String inputName;
    public static int inputLoop = -1;
    private static String playName;

    public static EditText editName;
    public static EditText editLoop;

    public static final String FROM_PLAY_LIST = "PlayItem_";
    public static final String FROM_FILE_CANCEL = "FileBrowserCancel";
    public static final String FILE_BROWSER = "FileBrowser";

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static PlayItemFragment newInstance(int param1, String param2) {
        PlayItemFragment fragment = new PlayItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctext = getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            if (mParam2 != null && mParam2.startsWith(FROM_PLAY_LIST)) {
                try {
                    playName = mParam2.substring(FROM_PLAY_LIST.length());
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        // TODO: Change Adapter to display your content
        // = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playitem, container, false);

        editName = (EditText) view.findViewById(R.id.txt_Name);
        editLoop = (EditText) view.findViewById(R.id.txt_Loop);

        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        setupListViewAdapter();

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(adapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        setupAddButton(view);

        setupCancelButton(view);

        setupSaveButton(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IFragmentInteraction) activity;
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_PARAM1));
            ((MainActivity) activity).restoreActionBar();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private void setupListViewAdapter() {
        List<PlayListItem> pItemList = new ArrayList<>();

        if (mParam2.startsWith(FROM_PLAY_LIST)) {
            nowPlayItem = null;
            if (playName != null && !playName.equals("")) {
                try {
                    PlayList pList = getPlayItem(playName);
                    editName.setText(pList.playListName);
                    editLoop.setText(String.valueOf(pList.loop));
                    pItemList = pList.playListItem;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        } else if (mParam2.equals(FROM_FILE_CANCEL)) {
            pItemList = nowPlayItem;
            setupValue();
        } else {
            pItemList = nowPlayItem;
            setupValue();
            if (nowSeq != -1) {
                PlayListItem seqItem = pItemList.get(nowSeq);
                seqItem.setLocalFullFilePath(mParam2);
            }
        }
        adapter = new PlayItemAdapter(getActivity(), R.layout.play_item_info_view, pItemList);
    }

    private void setupAddButton(View v) {
        v.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                adapter.add(new PlayListItem(adapter.getItems().size(), ctext.getString(R.string.label_select_file), "", new ArrayList<Integer>(), 1));
            }
        });
    }

    private void setupCancelButton(View v) {
        v.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resetValues();

                MainActivity ma = (MainActivity) getActivity();
                ma.onFragmentInteraction(ctext.getString(R.string.nav_3_playlist));
                ma.onNavigationDrawerItemSelected(2);
            }
        });
    }

    private void setupSaveButton(View v) {
        v.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String dialogMsg;
                if (editName.getText().toString().trim().equals("") || editLoop.getText().toString().trim().equals("")) {
                    dialogMsg = ctext.getString(R.string.dialog_white_space);
                    showDialog(dialogMsg);
                    return;
                }

                if (adapter.getItems() == null || adapter.getItems().size() == 0) {
                    dialogMsg = ctext.getString(R.string.dialog_add_item);
                    showDialog(dialogMsg);
                    return;
                }

                PlayList pList = new PlayList();
                pList.playListName = editName.getText().toString();
                pList.loop = Integer.parseInt(editLoop.getText().toString());
                pList.playListItem = adapter.getItems();
                if (!pageValidate(pList.playListItem)) {
                    dialogMsg = ctext.getString(R.string.dialog_page_msg);
                    showDialog(dialogMsg);
                } else if (!fileValidate(pList.playListItem)) {
                    dialogMsg = ctext.getString(R.string.dialog_select_file);
                    showDialog(dialogMsg);
                } else {
                    PlayListManagerFragment.deletePlayList(playName);
                    int index = 0;
                    for (PlayListItem item : pList.playListItem) {
                        item.setSeq(index);
                        index++;
                    }
                    insertPlayList(pList);

                    resetValues();

                    MainActivity ma = (MainActivity) getActivity();
                    ma.onFragmentInteraction(ctext.getString(R.string.nav_3_playlist));
                    ma.onNavigationDrawerItemSelected(2);
                }
            }
        });
    }

    private boolean pageValidate(List<PlayListItem> pItem) {
        boolean isValidated = true;
        for (PlayListItem item : pItem) {
            if (item.getStrPages().trim().startsWith(",") || item.getStrPages().trim().startsWith("-") || item.getStrPages().trim().endsWith(",") || item.getStrPages().trim().endsWith("-") ||
                    item.getStrPages().trim().contains(",-") || item.getStrPages().trim().contains("-,") || item.getStrPages().trim().contains(",,") || item.getStrPages().trim().contains("--")) {
                isValidated = false;
                break;
            } else if (!item.getStrPages().trim().equals("")) {
                String[] tmpArr = item.getStrPages().trim().split(",");
                if (tmpArr.length > 0) {
                    for (String tmp : tmpArr) {
                        if (tmp.trim().equals("")) {
                            isValidated = false;
                            break;
                        } else if (tmp.contains("-")) {
                            try {
                                String[] intArr = tmp.trim().split("-", 2);
                                int start = Integer.parseInt(intArr[0]);
                                int end = Integer.parseInt(intArr[1]);
                                if (start == 0 || end == 0 || start >= end) {
                                    isValidated = false;
                                    break;
                                }
                            } catch (Exception e) {
                                isValidated = false;
                                break;
                            }
                        } else {
                            try {
                                int page = Integer.parseInt(tmp.trim());
                                if (page == 0) {
                                    isValidated = false;
                                    break;
                                }
                            } catch (Exception e) {
                                isValidated = false;
                                break;
                            }
                        }
                    }
                } else {
                    isValidated = false;
                    break;
                }
            }
        }
        return isValidated;
    }

    private boolean fileValidate(List<PlayListItem> pItem) {
        boolean isValidated = true;
        for (PlayListItem item : pItem) {
            if (item.getlocalFullFilePath().equals(ctext.getString(R.string.label_select_file))) {
                isValidated = false;
                break;
            }
        }
        return isValidated;
    }

    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(ctext.getString(R.string.dialog_alter));
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();
    }

    private void resetValues() {
        inputName = "";
        inputLoop = -1;
        playName = "";
        nowSeq = -1;
        nowPlayItem = null;
    }

    private void setupValue() {
        if (inputName != null)
            editName.setText(inputName);
        if (inputLoop != -1)
            editLoop.setText(String.valueOf(inputLoop));
    }

    private PlayList getPlayItem(String pListName) {
        return DBManager.getPlayItem(pListName);
    }

    private boolean insertPlayList(PlayList pList) {
        return DBManager.insertPlayList(pList);
    }
}
