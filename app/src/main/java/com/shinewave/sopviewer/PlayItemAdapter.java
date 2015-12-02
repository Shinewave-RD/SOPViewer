package com.shinewave.sopviewer;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

/**
 * Created by user on 2015/11/24.
 */
public class PlayItemAdapter extends ArrayAdapter<PlayListItem> {
    protected static final String TAG = PlayItemAdapter.class.getSimpleName();

    private List<PlayListItem> items;
    private int layoutResourceId;
    private Context context;

    public PlayItemAdapter(Context context, int layoutResourceId, List<PlayListItem> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlayItemHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new PlayItemHolder();
        holder.playItem = items.get(position);
        holder.deleteButton = (ImageButton) row.findViewById(R.id.btn_Delete);
        holder.deleteButton.setTag(holder.playItem);
        holder.deleteButton.setOnClickListener(new ItemButtonDelete_Click(position));

        holder.fileButton = (Button) row.findViewById(R.id.ItemButton_Flie);
        holder.fileButton.setTag(holder.playItem);
        holder.fileButton.setOnClickListener(new ItemButtonFile_Click(position));

        holder.lblFullPath = (TextView) row.findViewById(R.id.lbl_fullPath);

        holder.txtPage = (TextView) row.findViewById(R.id.txt_page);
        setPageTextChangeListener(holder);
        holder.txtSec = (TextView) row.findViewById(R.id.txt_Sec);
        setSecTextListeners(holder);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    public List<PlayListItem> getItems() {
        return items;
    }

    private void setupItem(PlayItemHolder holder) {
        holder.txtPage.setText(holder.playItem.getStrPages());
        holder.txtSec.setText(String.valueOf(holder.playItem.getSec()));

        int iSlash = holder.playItem.getlocalFullFilePath().lastIndexOf('/');
        if (iSlash > 0) {
            String sFileName = holder.playItem.getlocalFullFilePath().substring(iSlash + 1);
            String sPreFileName = holder.playItem.getlocalFullFilePath().substring(0, iSlash + 1);
            holder.fileButton.setText(sFileName);
            holder.lblFullPath.setText(sPreFileName);
        } else {
            holder.fileButton.setText(holder.playItem.getlocalFullFilePath());
        }
    }

    public static class PlayItemHolder {
        PlayListItem playItem;
        TextView txtPage;
        TextView txtSec;
        ImageButton deleteButton;
        Button fileButton;
        TextView lblFullPath;
    }

    private void setPageTextChangeListener(final PlayItemHolder holder) {
        holder.txtPage.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.playItem.setStrPages(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setSecTextListeners(final PlayItemHolder holder) {
        holder.txtSec.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    holder.playItem.setSec(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    //Log.e(LOG_TAG, "error reading double value: " + s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    class ItemButtonFile_Click implements View.OnClickListener {
        private int position;

        ItemButtonFile_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            PlayItemFragment.nowSeq = position;
            PlayItemFragment.nowPlayItem = getItems();
            PlayItemFragment.inputName = PlayItemFragment.editName.getText().toString();
            if (!PlayItemFragment.editLoop.getText().toString().equals(""))
                PlayItemFragment.inputLoop = Integer.parseInt(PlayItemFragment.editLoop.getText().toString());
            MainActivity ma = (MainActivity) context;
            ma.onFragmentInteraction("FileBrowser");
            ma.onNavigationDrawerItemSelected(0);
        }
    }

    class ItemButtonDelete_Click implements View.OnClickListener {
        private int position;

        ItemButtonDelete_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            PlayListItem itemToRemove = getItem(position);
            remove(itemToRemove);
        }
    }
}

