package com.shinewave.sopviewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.OutlineActivityData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PDFPlayActivity extends AppCompatActivity {

    private MuPDFCore        core;
    private MuPDFReaderView  mDocView;
    private View             mButtonsView;
    private SeekBar          mPageSlider;
    private int              mPageSliderRes;
    private TextView         mPageNumberView;
    private boolean          mButtonsVisible;
    private Handler          handler;
    private int              playItemCount = 0;
    private int              loopCount = 0;
    private PlayList         plist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);
        //mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
        //mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);

        String playListName = getIntent().getStringExtra("PlayListName");
        plist = DBManager.getPlayItem(playListName);
        if(plist != null && plist.playListItem.size() > 0) {
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

            RelativeLayout layout = new RelativeLayout(this);
            layout.addView(mDocView);
            layout.addView(mButtonsView);
            setContentView(layout);

            setup();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error!!");
            builder.setMessage("Play list include no play item !!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            builder.create().show();
        }
    }

    private void setup() {
        if(playItemCount >= plist.playListItem.size()) {
            loopCount++;
            playItemCount = 0;
        }

        if(loopCount >= plist.loop)
            return;

        final PlayListItem pItem = plist.playListItem.get(playItemCount);
        if(pItem != null) {
            mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
            mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);

            playItemCount++;
            core = null;
            core = openFile(pItem.getlocalFullFilePath());
            if (core != null) {
                final List seqList = parseStringRange(pItem.strPages);
                handler = null;
                handler = new Handler() {
                    Timer timer;
                    int idx= 0;
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
                                            }
                                            catch(Exception e)
                                            {
                                                setup();
                                            }
                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask, pItem.sec * 1000);
                        }
                    }
                };

                int start = (int)seqList.get(0);
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

    private MuPDFCore openFile(String path)
    {
        try
        {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        catch (java.lang.OutOfMemoryError e)
        {
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
            mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
            mPageSlider.setProgress(index * mPageSliderRes);

            Animation anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageSlider.setVisibility(View.VISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
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

    private List parseStringRange(String str){
        ArrayList list = new ArrayList<>();
        if(core != null) {
            int length = core.countPages();
            int start = 0;
            int end = length;
            if("".equals(str))
            {
                for (int j = start; j < end; j++) {
                    list.add(j);
                }
            }
            else {
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

}
