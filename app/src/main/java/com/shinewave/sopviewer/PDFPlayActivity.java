package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.ReaderView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PDFPlayActivity extends AppCompatActivity {

    private MuPDFCore core;
    private MuPDFReaderView mDocView;
    private View mButtonsView;
    private SeekBar mPageSlider;
    private int mPageSliderRes;
    private TextView mPageNumberView;
    private boolean mButtonsVisible;
    private Handler handler;
    private int playItemCount = 0;
    private int loopCount = 0;
    private PlayList plist;
    private Timer timer = null;
    private RelativeLayout layout = null;
    private static final int OUTER_TYPE_CONN = 1;
    private static final int OUTER_TYPE_PLAY = 2;
    private static final int OUTER_TYPE_CONN_AND_PLAY = 3;
    private String resultS;
    private StringBuilder resMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);
        //mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
        //mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);

        try {
            DataBaseHelper dbHelper = new DataBaseHelper(this.getApplicationContext());
            dbHelper.createDataBase();
            DBManager.initDBHelp(dbHelper);
        } catch (IOException e) {
            //
        }

        Intent it = getIntent();
        String playListName = "";
        if (it.getAction() != null && it.getAction().equals("com.shinewave.sopviewer.PDFPlayActivity")) {
            //外部傳入
            int type = it.getIntExtra("IntentType", 0);
            String jsonStr = getIntent().getStringExtra("Json");
            JSONObject obj = new JSONObject();
            try {
                obj = new JSONObject(jsonStr);
            } catch (Exception e) {
                resultS = getString(R.string.msg_json_error);
                //Toast.makeText(PDFPlayActivity.this, getString(R.string.msg_json_error), Toast.LENGTH_LONG).show();
            }
            if (type == OUTER_TYPE_CONN) {
                //Conn
                String msg = doOuterConn(obj);
                resultS = msg;
                //Toast.makeText(PDFPlayActivity.this, msg, Toast.LENGTH_LONG).show();
            } else if (type == OUTER_TYPE_PLAY) {
                //Play
                String msg = doOuterPlay(obj);
                if (msg.startsWith(getString(R.string.dialog_succeed))) {
                    String[] nameArr = msg.split(",");
                    playListName = nameArr[1];
                } else {
                    resultS = msg;
                    //Toast.makeText(PDFPlayActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } else if (type == OUTER_TYPE_CONN_AND_PLAY) {
                //Conn + Play
                String msgConn = doOuterConn(obj);
                if (msgConn.equals(getString(R.string.dialog_succeed))) {
                    String msgPlay = doOuterPlay(obj);
                    if (msgPlay.startsWith(getString(R.string.dialog_succeed))) {
                        String[] nameArr = msgPlay.split(",");
                        playListName = nameArr[1];
                    } else {
                        resultS = msgPlay;
                        //Toast.makeText(PDFPlayActivity.this, msgPlay, Toast.LENGTH_LONG).show();
                    }
                } else {
                    resultS = msgConn;
                    //Toast.makeText(PDFPlayActivity.this, msgConn, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            //內部
            playListName = getIntent().getStringExtra("PlayListName");
        }

        if (playListName == null || playListName.equals("")) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultS);
            PDFPlayActivity.this.setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            resMsg = new StringBuilder();
            resMsg.append(getString(R.string.cannot_open_file_Path)).append("\n");
            plist = DBManager.getPlayItem(playListName);
            if (plist != null && plist.playListItem.size() > 0) {
                mDocView = new MuPDFReaderView(this) {
                    @Override
                    protected void onMoveToChild(int i) {
                        mPageNumberView.setText(String.format("%d / %d", i + 1, core.countPages()));
                        mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                        mPageSlider.setProgress(i * mPageSliderRes);
                        super.onMoveToChild(i);
                    }

                    @Override
                    protected void onTapMainDocArea() {
                        if (!mButtonsVisible) {
                            showButtons();
                        } else {
                            hideButtons();
                        }
                    }

                    @Override
                    protected void onDocMotion() {
                        hideButtons();
                    }
                };

                layout = new RelativeLayout(this);
                layout.addView(mDocView);
                layout.addView(mButtonsView);
                setContentView(layout);

                setup("");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.title_error));
                builder.setMessage(getString(R.string.playlist_no_playitem));
                builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder.create().show();
            }

            Button btnFinish = (Button) findViewById(R.id.btn_finish);
            btnFinish.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", "FINISH");
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
        }
    }

    private void setup(String msg) {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }

        core = null;

        System.gc();

        if (playItemCount >= plist.playListItem.size()) {
            loopCount++;
            playItemCount = 0;
        }

        if (loopCount >= plist.loop) {
            Intent it = getIntent();
            if (it.getAction() != null && it.getAction().equals("com.shinewave.sopviewer.PDFPlayActivity")) {
                Intent returnIntent = new Intent();
                if (!msg.equals(""))
                    returnIntent.putExtra("result", msg.substring(0, msg.length() - 1));
                else
                    returnIntent.putExtra("result", "FINISH");
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                Toast.makeText(this, msg.substring(0, msg.length() - 1), Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }

        final PlayListItem pItem = plist.playListItem.get(playItemCount);
        if (pItem != null) {
            mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
            mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);

            playItemCount++;

            core = openFile(pItem.getlocalFullFilePath());
            if (core != null) {
                final List seqList = parseStringRange(pItem.strPages);
                handler = null;
                handler = new Handler() {
                    int idx = 0;

                    public void handleMessage(Message msg) {
                        if (msg.what == mDocView.getCurrent()) {
                            idx++;
                            if (timer != null) {
                                timer.cancel();
                                timer.purge();
                                timer = null;
                            }
                            timer = new Timer();
                            TimerTask timerTask = new TimerTask() {

                                public void run() {

                                    handler.post(new Runnable() {

                                        public void run() {
                                            try {
                                                mDocView.setDisplayedViewIndex((int) seqList.get(idx));
                                            } catch (Exception e) {
                                                setup("");
                                            }
                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask, pItem.sec * 1000);
                        }
                    }
                };

                int start = (int) seqList.get(0);
                mDocView.setAdapter(new MuPDFPageAdapter(this, null, core, handler));
                mDocView.setCurrent(start);

                int smax = Math.max(core.countPages() - 1, 1);
                mPageSliderRes = ((10 + smax - 1) / smax) * 2;

                // Activate the seekbar
                mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
                    }
                });

                mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(start);
                updatePageNumView(start);
                mDocView.refresh(false);
            } else {
                if (loopCount == 0)
                    resMsg.append(pItem.getlocalFullFilePath()).append("\n");
                setup(resMsg.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mButtonsVisible) {
            hideButtons();
        } else {
            showButtons();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private MuPDFCore openFile(String path) {
        try {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        } catch (java.lang.OutOfMemoryError e) {
            //  out of memory is not an Exception, so we catch it separately.
            System.out.println(e);
            return null;
        }
        return core;
    }

    private void showButtons() {
        if (core == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
            mPageSlider.setProgress(index * mPageSliderRes);

            Animation anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageSlider.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;

            Animation anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageSlider.setVisibility(View.INVISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    private void updatePageNumView(int index) {
        if (core == null)
            return;
        mPageNumberView.setText(String.format("%d / %d", index + 1, core.countPages()));
    }

    private List parseStringRange(String str) {
        ArrayList list = new ArrayList<>();
        if (core != null) {
            int length = core.countPages();
            int start = 0;
            int end = length;
            if ("".equals(str)) {
                for (int j = start; j < end; j++) {
                    list.add(j);
                }
            } else {
                String[] tmpArray = str.split(",");
                for (String tmp : tmpArray) {
                    try {
                        if (tmp.contains("-")) {
                            String[] tmpArray2 = tmp.split("-");
                            if (!"".equals(tmpArray2[0]))
                                start = Integer.parseInt(tmpArray2[0]);
                            if (!"".equals(tmpArray2[1]))
                                end = Integer.parseInt(tmpArray2[1]);
                            for (int j = start; j <= end; j++) {
                                if (j > 0 && j <= length) {
                                    list.add(j - 1);
                                }
                            }

                        } else {
                            int number = Integer.parseInt(tmp);
                            if (number > 0 && number <= length) {
                                list.add(number - 1);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        return list;
    }

    private String doOuterConn(JSONObject obj) {
        String res;
        List<ServerConnectionInfo> downloadList = parseConnJson(obj);
        if (downloadList.size() > 0) {
            //驗證Conn內容
            String resValid = ValidateConnJson(downloadList);
            if (resValid.trim().equals("")) {
                //建立Conn
                String failedConn = CreateConnJason(downloadList);
                if (failedConn.trim().equals("")) {
                    res = getString(R.string.dialog_succeed);
                } else {
                    res = getString(R.string.dialog_failed) + ": \n";
                }
            } else {
                res = getString(R.string.msg_conn_error) + ": \n" + resValid.substring(0, resValid.length() - 1);
            }
        } else {
            res = getString(R.string.msg_json_error);
        }
        return res;
    }

    private String doOuterPlay(JSONObject obj) {
        String res;
        PlayList playListCreate = parsePlayJson(obj);
        if (playListCreate.playListName != null && !playListCreate.playListName.trim().equals("")) {
            //驗證PlayList
            String resValidPlay = ValidatePlayJson(playListCreate);
            if (resValidPlay.trim().equals("")) {
                //建立PlayList
                boolean resPlay = CreatePlayJson(playListCreate);
                if (resPlay)
                    res = getString(R.string.dialog_succeed) + "," + playListCreate.playListName;
                else
                    res = getString(R.string.dialog_failed);
            } else {
                res = getString(R.string.msg_play_error) + ": \n" + resValidPlay.substring(0, resValidPlay.length() - 1);
            }
        } else {
            res = getString(R.string.msg_json_error);
        }
        return res;
    }

    private PlayList parsePlayJson(JSONObject obj) {
        PlayList pList = new PlayList();
        pList.playListItem = new ArrayList<>();
        try {
            pList.playListName = obj.getString("playListName").trim();
            pList.loop = obj.getInt("loop");
            JSONArray arr = obj.getJSONArray("PlayListItem");
            for (int i = 0; i < arr.length(); i++) {
                PlayListItem item = new PlayListItem(0, "", "", null, 0);
                item.seq = i;
                item.localFullFilePath = arr.getJSONObject(i).getString("localFullFilePath").trim();
                item.strPages = arr.getJSONObject(i).getString("strPages").trim();
                item.sec = arr.getJSONObject(i).getInt("sec");
                pList.playListItem.add(item);
            }
        } catch (Exception e) {
            pList = new PlayList();
        }
        return pList;
    }

    private List<ServerConnectionInfo> parseConnJson(JSONObject obj) {
        List<ServerConnectionInfo> connList = new ArrayList<>();
        try {
            JSONArray arr = obj.getJSONArray("Conn");
            for (int i = 0; i < arr.length(); i++) {
                ServerConnectionInfo info = new ServerConnectionInfo();
                info.connectionName = arr.getJSONObject(i).getString("connectionName").trim();
                String pType = arr.getJSONObject(i).getString("protocol").trim();
                if (pType.toLowerCase().equals("ftp"))
                    info.protocol = 0;
                else if (pType.toLowerCase().equals("smb"))
                    info.protocol = 1;
                else
                    info.protocol = 2;
                info.url = arr.getJSONObject(i).getString("url").trim();
                info.id = arr.getJSONObject(i).getString("id").trim();
                info.password = arr.getJSONObject(i).getString("password").trim();
                info.fullFilePath = arr.getJSONObject(i).getString("fullFilePath").trim();
                info.fileSavePath = arr.getJSONObject(i).getString("fileSavePath").trim();
                connList.add(info);
            }
        } catch (Exception e) {
            connList = new ArrayList<>();
        }
        return connList;
    }

    private String ValidateConnJson(List<ServerConnectionInfo> infoList) {
        StringBuilder failedName = new StringBuilder();
        try {
            for (ServerConnectionInfo info : infoList) {
                if (info.connectionName == null || info.connectionName.trim().equals("")) {
                    failedName.append(getString(R.string.msg_Conn_null)).append("\n");
                } else if (info.id == null || info.id.trim().equals("") || info.password == null || info.password.trim().equals("") ||
                        info.url == null || info.url.trim().equals("") || info.fullFilePath == null || info.fullFilePath.trim().equals("") ||
                        info.fileSavePath == null || info.fileSavePath.trim().equals("")) {
                    failedName.append(info.connectionName).append("\n");
                } else if (info.connectionName.length() > 25 || info.protocol == 2 || info.id.length() > 40 || info.password.length() > 40 ||
                        info.fullFilePath.length() > 500 || info.fileSavePath.length() > 500 || info.url.length() > 500) {
                    failedName.append(info.connectionName).append("\n");
                }
            }
        } catch (Exception e) {
            //
        }
        return failedName.toString();
    }

    private String ValidatePlayJson(PlayList pList) {
        StringBuilder failedName = new StringBuilder();
        try {
            if (pList.playListName == null || pList.playListName.trim().equals("")) {
                failedName.append(getString(R.string.msg_play_null)).append("\n");
            } else if (pList.playListName.length() > 30 || String.valueOf(pList.loop).length() > 30 || pList.playListItem.size() == 0) {
                failedName.append(pList.playListName).append("\n");
            } else if (pList.playListItem.size() > 0) {
                for (PlayListItem item : pList.playListItem) {
                    if (item.localFullFilePath == null || item.localFullFilePath.trim().equals("") || item.strPages == null || item.strPages.trim().equals("")) {
                        failedName.append(pList.playListName).append("\n");
                    } else if (!pageValidate(item)) {
                        failedName.append(pList.playListName).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            //
        }
        return failedName.toString();
    }

    private boolean pageValidate(PlayListItem item) {
        boolean isValidated = true;
        if (item.getStrPages().trim().startsWith(",") || item.getStrPages().trim().startsWith("-") || item.getStrPages().trim().endsWith(",") || item.getStrPages().trim().endsWith("-") ||
                item.getStrPages().trim().contains(",-") || item.getStrPages().trim().contains("-,") || item.getStrPages().trim().contains(",,") || item.getStrPages().trim().contains("--")) {
            isValidated = false;
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
            }
        }

        return isValidated;
    }

    private boolean CreatePlayJson(PlayList createPList) {
        Boolean res;
        try {
            PlayList pList = DBManager.getPlayItem(createPList.playListName);
            if (pList.playListItem.size() > 0) {
                res = DBManager.deletePlayList(createPList.playListName);
            }
            res = DBManager.insertPlayList(createPList);
        } catch (Exception e) {
            res = false;
        }
        return res;
    }

    private String CreateConnJason(List<ServerConnectionInfo> downloadList) {
        StringBuilder failedName = new StringBuilder();
        try {
            List<ServerConnectionInfo> resList = ConnectionManagerFragment.createConnection(downloadList);
            for (ServerConnectionInfo info : resList) {
                if (!info.downloadSuccessed) {
                    failedName.append(info.connectionName).append("\n");
                }
            }
        } catch (Exception e) {
            //
        }
        return failedName.toString();
    }

    public void onDestroy() {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
            mDocView = null;
        }
        if (core != null)
            core.onDestroy();
        core = null;

        layout = null;
        mButtonsView = null;
        mPageSlider = null;
        mPageNumberView = null;
        handler = null;

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        super.onDestroy();

        System.gc();
    }

}
