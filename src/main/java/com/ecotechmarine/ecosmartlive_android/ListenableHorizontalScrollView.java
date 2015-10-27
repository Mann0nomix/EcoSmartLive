package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by emann on 3/13/14.
 */
public class ListenableHorizontalScrollView extends HorizontalScrollView{

    RelativeLayout scheduleLayout;
    ArrayList<View> viewArray;
    ArrayList<Integer> centeredPointLocation;
    int screenWidth;
    int centerOfScreen;
    int halfPointWidth;
    int nowIndex;
    int nightIndex;
    int dayIndex;
    Context context;
    Schedule schedule;
    boolean onLock;
    boolean onStart;
    int snapCount = 0;
    int currentIndex = 0;

    public ListenableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setSmoothScrollingEnabled(true);
        centeredPointLocation = new ArrayList<Integer>();
        viewArray = new ArrayList<View>();
        schedule = new Schedule();
        onLock = false;
        onStart = false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (scheduleLayout != null && !this.viewArray.isEmpty() && this.viewArray.size() > 0){
            screenWidth = scheduleLayout.getWidth();
            centerOfScreen = screenWidth / 2;
            //all views are the same size so just get the first views size for half point width
            halfPointWidth = this.viewArray.get(0).getWidth() / 2;

            //Condition to snap to the forward
            if (!centeredPointLocation.isEmpty() && currentIndex < centeredPointLocation.size()){
                if (snapCount == 0){
                    if(l > centeredPointLocation.get(currentIndex) + halfPointWidth / 2 && l > oldl) {
                        if (currentIndex < viewArray.size()){
                            currentIndex++;
                            if (onStart && nowIndex != currentIndex && !NavigationWheel.wheelActivity.scheduleSeekChange){
                                currentIndex = nowIndex;
                                onStart = false;
                            }
                            //System.out.println("Center Point Array Size = " + centeredPointLocation.size());
                            //System.out.println("Current Scroll Index = " + currentIndex);
                            smoothScrollTo(centeredPointLocation.get(currentIndex), 0);
                            onLock = true;
                            snapCount = 1;
                            return;
                        }
                    }

                    //Condition to snap to the backward
                    if(l < centeredPointLocation.get(currentIndex) - halfPointWidth / 2 && oldl >= l) {
                        if (currentIndex > 0){
                            currentIndex--;
                            if (onStart && nowIndex != currentIndex && !NavigationWheel.wheelActivity.scheduleSeekChange){
                                currentIndex = nowIndex;
                                onStart = false;
                            }
                            //System.out.println("Center Point Array Size = " + centeredPointLocation.size());
                            //System.out.println("Current Scroll Index = " + currentIndex);
                            smoothScrollTo(centeredPointLocation.get(currentIndex), 0);
                            onLock = true;
                            snapCount = 1;
                            return;
                        }
                    }
                }

                //Condition to lock the scroll view in place when a view is scrolled forward
                if (onLock && l > centeredPointLocation.get(currentIndex) + halfPointWidth / 1.5 && l > oldl){
                    snapCount = 0;
                    return;
                }

                //Condition to lock the scroll view in place when a view is scrolled backward
                if (onLock && l < centeredPointLocation.get(currentIndex) - halfPointWidth / 1.5 && oldl >= l){
                    snapCount = 0;
                    return;
                }

                //Delete Final if things start buggin out
                for (final View item: viewArray){
                    if (item != null){
                        if (l <= centeredPointLocation.get(viewArray.indexOf(item)) - halfPointWidth / 1.2 ||l >= centeredPointLocation.get(viewArray.indexOf(item)) + halfPointWidth / 1.2){
                            //currentIndex = viewArray.indexOf(item);
                            if (Build.VERSION.SDK_INT < 11){
                                AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                                alpha.setDuration(0); // Make animation instant
                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                item.startAnimation(alpha);

                            }else{
                                item.setAlpha(0.5f);
                            }
                            //get text that needs to be bolded and highlighted and adjust the text to bold
                            TextView dataPointText = (TextView)viewArray.get(viewArray.indexOf(item)).findViewById(R.id.datapoint_display_time);
                            dataPointText.setTypeface(null,Typeface.NORMAL);
                            TextView dataPointIntensity = (TextView)viewArray.get(viewArray.indexOf(item)).findViewById(R.id.datapoint_display_intensity);
                            dataPointIntensity.setTypeface(null,Typeface.NORMAL);

                            //make unselected data points unselectable
                            item.setOnClickListener(null);
                            item.setOnLongClickListener(null);

                            // ============== CENTER POINT LOGIC ================= //
                        }else{
                            if (Build.VERSION.SDK_INT < 11){
                                AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                                alpha.setDuration(0); // Make animation instant
                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                item.startAnimation(alpha);
                            }else{
                                item.setAlpha(1f);
                            }

                            //get text that needs to be bolded and highlighted and adjust the text to bold
                            TextView dataPointText = (TextView)viewArray.get(viewArray.indexOf(item)).findViewById(R.id.datapoint_display_time);
                            dataPointText.setTypeface(null,Typeface.BOLD);
                            TextView dataPointIntensity = (TextView)viewArray.get(viewArray.indexOf(item)).findViewById(R.id.datapoint_display_intensity);
                            dataPointIntensity.setTypeface(null,Typeface.BOLD);

                            //if the now point has been passed, change things on scroll based off of an index of -1
                            if (nowIndex == 0){
                                //handle dynamic change of background
                                if (viewArray.indexOf(item) > 0){
                                    if (schedule.scheduleType.equalsIgnoreCase("N")) {

                                        if(schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).cloudFrequency > 0 || schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).stormFrequency > 0){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            if (nightIndex < dayIndex){
                                                if (viewArray.indexOf(item)-1 < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (nightIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item)-1 > dayIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            } else if (dayIndex < nightIndex) {
                                                if (viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (dayIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item)-1 > nightIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            }
                                        } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartDay){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item)-1 < dayIndex || viewArray.indexOf(item)-1 == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (nightIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > dayIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                        } else if (nightIndex < dayIndex) {
                                            if (viewArray.indexOf(item) < nightIndex || viewArray.indexOf(item) == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        //safety measure to ensure the now point never borrows from night point
                                        if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        if(schedule.dataPoints.get(viewArray.indexOf(item)-1).cloudFrequency > 0 || schedule.dataPoints.get(viewArray.indexOf(item)-1).stormFrequency > 0){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            if (nightIndex < dayIndex){
                                                if (viewArray.indexOf(item)-1 < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (nightIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item)-1 > dayIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            } else if (dayIndex < nightIndex) {
                                                if (viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (dayIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item)-1 > nightIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            }
                                        } else if(schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartDay){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        } else if (schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item)-1 < dayIndex || viewArray.indexOf(item)-1 == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (nightIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > dayIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                        } else if (nightIndex < dayIndex) {
                                            if (viewArray.indexOf(item)-1 < nightIndex || viewArray.indexOf(item)-1 == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        //safety measure to ensure the now point never borrows from night point
                                        if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    }
                                }else{
                                    if (schedule.scheduleType.equalsIgnoreCase("N")) {
                                        if(schedule.filteredDataPoints.get(viewArray.indexOf(item)).cloudFrequency > 0 || schedule.filteredDataPoints.get(viewArray.indexOf(item)).stormFrequency > 0){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            if (nightIndex < dayIndex){
                                                if (viewArray.indexOf(item) < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (nightIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item) > dayIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            } else if (dayIndex < nightIndex) {
                                                if (viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (dayIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item) > nightIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            }
                                        } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartDay){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item) < dayIndex || viewArray.indexOf(item) == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (nightIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > dayIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                        } else if (nightIndex < dayIndex) {
                                            if (viewArray.indexOf(item) < nightIndex || viewArray.indexOf(item) == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        //safety measure to ensure the now point never borrows from night point
                                        if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        if(schedule.dataPoints.get(viewArray.indexOf(item)).cloudFrequency > 0 || schedule.dataPoints.get(viewArray.indexOf(item)).stormFrequency > 0){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            if (nightIndex < dayIndex){
                                                if (viewArray.indexOf(item) < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (nightIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item) > dayIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            } else if (dayIndex < nightIndex) {
                                                if (viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (dayIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < nightIndex) {
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                                if (viewArray.indexOf(item) > nightIndex){
                                                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                    NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                                }
                                            }
                                        } else if(schedule.dataPoints.get(viewArray.indexOf(item)).isStartDay){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        } else if (schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item) < dayIndex || viewArray.indexOf(item) == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (nightIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > dayIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                        } else if (nightIndex < dayIndex) {
                                            if (viewArray.indexOf(item) < nightIndex || viewArray.indexOf(item) == 0) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        //safety measure to ensure the now point never borrows from night point
                                        if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    }
                                }
                            }else if (viewArray.indexOf(item) < nowIndex) {
                                //handle dynamic change of background
                                if (schedule.scheduleType.equalsIgnoreCase("N")) {
                                    if(schedule.filteredDataPoints.get(viewArray.indexOf(item)).cloudFrequency > 0 || schedule.filteredDataPoints.get(viewArray.indexOf(item)).stormFrequency > 0){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        if (nightIndex < dayIndex){
                                            if (viewArray.indexOf(item) < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (nightIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        }
                                    } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartDay){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                    } else if (dayIndex < nightIndex) {
                                        if (viewArray.indexOf(item) < dayIndex || viewArray.indexOf(item) == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (nightIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > dayIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (viewArray.indexOf(item) > nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                    } else if (nightIndex < dayIndex) {
                                        if (viewArray.indexOf(item) < nightIndex || viewArray.indexOf(item) == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (dayIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > nightIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (viewArray.indexOf(item) > dayIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                    //safety measure to ensure the now point never borrows from night point
                                    if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                } else {
                                    if(schedule.dataPoints.get(viewArray.indexOf(item)).cloudFrequency > 0 || schedule.dataPoints.get(viewArray.indexOf(item)).stormFrequency > 0){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        if (nightIndex < dayIndex){
                                            if (viewArray.indexOf(item) < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (nightIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item) < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex < viewArray.indexOf(item) && viewArray.indexOf(item) < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item) > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        }
                                    } else if(schedule.dataPoints.get(viewArray.indexOf(item)).isStartDay){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    } else if (schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode) {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                    } else if (dayIndex < nightIndex) {
                                        if (viewArray.indexOf(item) < dayIndex || viewArray.indexOf(item) == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (nightIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > dayIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (viewArray.indexOf(item) > nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                    } else if (nightIndex < dayIndex) {
                                        if (viewArray.indexOf(item) < nightIndex || viewArray.indexOf(item) == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (dayIndex > viewArray.indexOf(item) && viewArray.indexOf(item) > nightIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (viewArray.indexOf(item) > dayIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                    //safety measure to ensure the now point never borrows from night point
                                    if (viewArray.indexOf(item) == nowIndex && nowIndex > dayIndex && nowIndex <= nightIndex){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                }
                            }else{
                                //handle dynamic change of background
                                if (schedule.scheduleType.equalsIgnoreCase("N")) {
                                    if(schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).cloudFrequency > 0 || schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).stormFrequency > 0){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        if (nightIndex < dayIndex){
                                            if (viewArray.indexOf(item)-1 < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (nightIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        }
                                    } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartDay){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    } else if (schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                    } else if (dayIndex < nightIndex) {
                                        if (viewArray.indexOf(item)-1 < dayIndex || viewArray.indexOf(item)-1 == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (nightIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > dayIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (viewArray.indexOf(item)-1 > nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                    } else if (nightIndex < dayIndex) {
                                        if (viewArray.indexOf(item)-1 < nightIndex || viewArray.indexOf(item)-1 == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (dayIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > nightIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (viewArray.indexOf(item)-1 > dayIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                } else {
                                    if(schedule.dataPoints.get(viewArray.indexOf(item)-1).cloudFrequency > 0 || schedule.dataPoints.get(viewArray.indexOf(item)-1).stormFrequency > 0){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        if (nightIndex < dayIndex){
                                            if (viewArray.indexOf(item)-1 < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (nightIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > dayIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        } else if (dayIndex < nightIndex) {
                                            if (viewArray.indexOf(item)-1 < dayIndex || schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (dayIndex < viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 < nightIndex) {
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                            if (viewArray.indexOf(item)-1 > nightIndex){
                                                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                                                NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                            }
                                        }
                                    } else if(schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartDay){
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    } else if (schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode) {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                    } else if (dayIndex < nightIndex) {
                                        if (viewArray.indexOf(item)-1 < dayIndex || viewArray.indexOf(item)-1 == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (nightIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > dayIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (viewArray.indexOf(item)-1 > nightIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                    } else if (nightIndex < dayIndex) {
                                        if (viewArray.indexOf(item)-1 < nightIndex || viewArray.indexOf(item)-1 == 0) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                        if (dayIndex > viewArray.indexOf(item)-1 && viewArray.indexOf(item)-1 > nightIndex) {
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(VISIBLE);
                                        }
                                        if (viewArray.indexOf(item)-1 > dayIndex){
                                            NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                            NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                        }
                                    } else {
                                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                                        NavigationWheel.wheelActivity.nightMoon.setVisibility(INVISIBLE);
                                    }
                                }
                            }

                            if (viewArray.indexOf(item) != nowIndex){
                                item.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (viewArray.indexOf(item) > nowIndex){
                                            //handle transfer of data with an index of -1 since the now point has been added
                                            Calendar timeConvert = Calendar.getInstance();
                                            Intent intent = new Intent(getContext(),SchedulePointView.class);
                                            if (schedule.scheduleType.equalsIgnoreCase("N")){
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                                //schedule related properties that must be passed
                                                intent.putExtra("editUV",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).uv);
                                                intent.putExtra("editRB",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).royalBlue1);
                                                intent.putExtra("editRB2",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).royalBlue2);
                                                intent.putExtra("editBlue",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).blue);
                                                intent.putExtra("editWhite",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).white);
                                                intent.putExtra("editGreen",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).green);
                                                intent.putExtra("editRed",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).red);
                                                intent.putExtra("editBright",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).brightness);
                                                intent.putExtra("editCloud",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).cloudFrequency);
                                                intent.putExtra("editStorm",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).stormFrequency);
                                                intent.putExtra("editTimeHours",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                intent.putExtra("editTimeMins",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                                intent.putExtra("editTimeSeconds",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeSeconds);
                                                intent.putExtra("editPresetId",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).presetId);
                                                intent.putExtra("editPresetName",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).presetName);
                                                intent.putExtra("editIsNightMode",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isNightMode);
                                                intent.putExtra("editIsStartDay",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartDay);
                                                intent.putExtra("editIsStartNight",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartNight);
                                                intent.putExtra("editDataPointId",schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).scheduleDataPointId);
                                            }else{
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                                //schedule related properties that must be passed
                                                intent.putExtra("editUV",schedule.dataPoints.get(viewArray.indexOf(item)-1).uv);
                                                intent.putExtra("editRB",schedule.dataPoints.get(viewArray.indexOf(item)-1).royalBlue1);
                                                intent.putExtra("editRB2",schedule.dataPoints.get(viewArray.indexOf(item)-1).royalBlue2);
                                                intent.putExtra("editBlue",schedule.dataPoints.get(viewArray.indexOf(item)-1).blue);
                                                intent.putExtra("editWhite",schedule.dataPoints.get(viewArray.indexOf(item)-1).white);
                                                intent.putExtra("editGreen",schedule.dataPoints.get(viewArray.indexOf(item)-1).green);
                                                intent.putExtra("editRed",schedule.dataPoints.get(viewArray.indexOf(item)-1).red);
                                                intent.putExtra("editBright",schedule.dataPoints.get(viewArray.indexOf(item)-1).brightness);
                                                intent.putExtra("editCloud",schedule.dataPoints.get(viewArray.indexOf(item)-1).cloudFrequency);
                                                intent.putExtra("editStorm",schedule.dataPoints.get(viewArray.indexOf(item)-1).stormFrequency);
                                                intent.putExtra("editTimeHours",schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                intent.putExtra("editTimeMins",schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                                intent.putExtra("editTimeSeconds",schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeSeconds);
                                                intent.putExtra("editPresetId",schedule.dataPoints.get(viewArray.indexOf(item)-1).presetId);
                                                intent.putExtra("editPresetName",schedule.dataPoints.get(viewArray.indexOf(item)-1).presetName);
                                                intent.putExtra("editIsNightMode",schedule.dataPoints.get(viewArray.indexOf(item)-1).isNightMode);
                                                intent.putExtra("editIsStartDay",schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartDay);
                                                intent.putExtra("editIsStartNight",schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartNight);
                                                intent.putExtra("editDataPointId",schedule.dataPoints.get(viewArray.indexOf(item)-1).scheduleDataPointId);
                                            }
                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            ///MIGHT NOT WORK / TEST OUT
                                            String editPointTime = formatter.format(timeConvert.getTime());
                                            intent.putExtra("isEditPoint",true);
                                            intent.putExtra("scheduleId",schedule.scheduleId);
                                            intent.putExtra("scheduleType",schedule.scheduleType);
                                            intent.putExtra("editIndex",viewArray.indexOf(item)-1); //used to grab specific point
                                            intent.putExtra("editPointTime",editPointTime);
                                            intent.putExtra("editPointHours",timeConvert.get(Calendar.HOUR_OF_DAY));
                                            intent.putExtra("editPointMins",timeConvert.get(Calendar.MINUTE));
                                            intent.putExtra("schedulePointTimes",NavigationWheel.wheelActivity.schedulePointTimes);
                                            intent.putExtra("startDayTime",NavigationWheel.wheelActivity.startDayPoint.totalSeconds);
                                            intent.putExtra("startNightTime",NavigationWheel.wheelActivity.startNightPoint.totalSeconds);

                                            NavigationWheel.wheelActivity.startActivityForResult(intent,0);
                                        }else{
                                            Calendar timeConvert = Calendar.getInstance();
                                            Intent intent = new Intent(getContext(),SchedulePointView.class);
                                            if (schedule.scheduleType.equalsIgnoreCase("N")){
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                                //schedule related properties that must be passed
                                                intent.putExtra("editUV",schedule.filteredDataPoints.get(viewArray.indexOf(item)).uv);
                                                intent.putExtra("editRB",schedule.filteredDataPoints.get(viewArray.indexOf(item)).royalBlue1);
                                                intent.putExtra("editRB2",schedule.filteredDataPoints.get(viewArray.indexOf(item)).royalBlue2);
                                                intent.putExtra("editBlue",schedule.filteredDataPoints.get(viewArray.indexOf(item)).blue);
                                                intent.putExtra("editWhite",schedule.filteredDataPoints.get(viewArray.indexOf(item)).white);
                                                intent.putExtra("editGreen",schedule.filteredDataPoints.get(viewArray.indexOf(item)).green);
                                                intent.putExtra("editRed",schedule.filteredDataPoints.get(viewArray.indexOf(item)).red);
                                                intent.putExtra("editBright",schedule.filteredDataPoints.get(viewArray.indexOf(item)).brightness);
                                                intent.putExtra("editCloud",schedule.filteredDataPoints.get(viewArray.indexOf(item)).cloudFrequency);
                                                intent.putExtra("editStorm",schedule.filteredDataPoints.get(viewArray.indexOf(item)).stormFrequency);
                                                intent.putExtra("editTimeHours",schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                intent.putExtra("editTimeMins",schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                                intent.putExtra("editTimeSeconds",schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeSeconds);
                                                intent.putExtra("editPresetId",schedule.filteredDataPoints.get(viewArray.indexOf(item)).presetId);
                                                intent.putExtra("editPresetName",schedule.filteredDataPoints.get(viewArray.indexOf(item)).presetName);
                                                intent.putExtra("editIsNightMode",schedule.filteredDataPoints.get(viewArray.indexOf(item)).isNightMode);
                                                intent.putExtra("editIsStartDay",schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartDay);
                                                intent.putExtra("editIsStartNight",schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartNight);
                                                intent.putExtra("editDataPointId",schedule.filteredDataPoints.get(viewArray.indexOf(item)).scheduleDataPointId);
                                            }else{
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.dataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.dataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                                //schedule related properties that must be passed
                                                intent.putExtra("editUV",schedule.dataPoints.get(viewArray.indexOf(item)).uv);
                                                intent.putExtra("editRB",schedule.dataPoints.get(viewArray.indexOf(item)).royalBlue1);
                                                intent.putExtra("editRB2",schedule.dataPoints.get(viewArray.indexOf(item)).royalBlue2);
                                                intent.putExtra("editBlue",schedule.dataPoints.get(viewArray.indexOf(item)).blue);
                                                intent.putExtra("editWhite",schedule.dataPoints.get(viewArray.indexOf(item)).white);
                                                intent.putExtra("editGreen",schedule.dataPoints.get(viewArray.indexOf(item)).green);
                                                intent.putExtra("editRed",schedule.dataPoints.get(viewArray.indexOf(item)).red);
                                                intent.putExtra("editBright",schedule.dataPoints.get(viewArray.indexOf(item)).brightness);
                                                intent.putExtra("editCloud",schedule.dataPoints.get(viewArray.indexOf(item)).cloudFrequency);
                                                intent.putExtra("editStorm",schedule.dataPoints.get(viewArray.indexOf(item)).stormFrequency);
                                                intent.putExtra("editTimeHours",schedule.dataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                intent.putExtra("editTimeMins",schedule.dataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                                intent.putExtra("editTimeSeconds",schedule.dataPoints.get(viewArray.indexOf(item)).startTimeSeconds);
                                                intent.putExtra("editPresetId",schedule.dataPoints.get(viewArray.indexOf(item)).presetId);
                                                intent.putExtra("editPresetName",schedule.dataPoints.get(viewArray.indexOf(item)).presetName);
                                                intent.putExtra("editIsNightMode",schedule.dataPoints.get(viewArray.indexOf(item)).isNightMode);
                                                intent.putExtra("editIsStartDay",schedule.dataPoints.get(viewArray.indexOf(item)).isStartDay);
                                                intent.putExtra("editIsStartNight",schedule.dataPoints.get(viewArray.indexOf(item)).isStartNight);
                                                intent.putExtra("editDataPointId",schedule.dataPoints.get(viewArray.indexOf(item)).scheduleDataPointId);

                                            }
                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            ///MIGHT NOT WORK / TEST OUT
                                            String editPointTime = formatter.format(timeConvert.getTime());
                                            intent.putExtra("isEditPoint",true);
                                            intent.putExtra("scheduleId",schedule.scheduleId);
                                            intent.putExtra("scheduleType",schedule.scheduleType);
                                            intent.putExtra("editIndex",viewArray.indexOf(item)); //used to grab specific point
                                            intent.putExtra("editPointTime",editPointTime);
                                            intent.putExtra("editPointHours",timeConvert.get(Calendar.HOUR_OF_DAY));
                                            intent.putExtra("editPointMins",timeConvert.get(Calendar.MINUTE));
                                            intent.putExtra("schedulePointTimes",NavigationWheel.wheelActivity.schedulePointTimes);
                                            intent.putExtra("startDayTime",NavigationWheel.wheelActivity.startDayPoint.totalSeconds);
                                            intent.putExtra("startNightTime",NavigationWheel.wheelActivity.startNightPoint.totalSeconds);

                                            NavigationWheel.wheelActivity.startActivityForResult(intent,0);
                                        }
                                    }
                                });
                            }


                            if (viewArray.indexOf(item) != nowIndex){
                                item.setOnLongClickListener(new OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        if (viewArray.indexOf(item) > nowIndex){
                                            Calendar timeConvert = Calendar.getInstance();
                                            if (schedule.scheduleType.equalsIgnoreCase("N")) {
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                            }else{
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.dataPoints.get(viewArray.indexOf(item)-1).startTimeMinute);
                                            }
                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            String editPointTime = formatter.format(timeConvert.getTime());

                                            if (schedule.scheduleType.equalsIgnoreCase("N")){
                                                if (!schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartDay && !schedule.filteredDataPoints.get(viewArray.indexOf(item)-1).isStartNight){
                                                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context);
                                                    myNewAlert.setMessage(R.string.delete_data_point);
                                                    myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                                                    myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            /*new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ConnectManager connect = new ConnectManager();
                                                                    connect.deleteScheduleDataPoint(schedule.filteredDataPoints.get(viewArray.indexOf(item)-1));
                                                                }
                                                            }).start();*/
                                                            new DeletePointTask(schedule.filteredDataPoints.get(viewArray.indexOf(item)-1)).execute();
                                                            //NavigationWheel.wheelActivity.refreshSchedulePoints();
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.show();
                                                }
                                            }else{
                                                if (!schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartDay && !schedule.dataPoints.get(viewArray.indexOf(item)-1).isStartNight){
                                                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context);
                                                    myNewAlert.setMessage(R.string.delete_data_point);
                                                    myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                                                    myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            /*new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ConnectManager connect = new ConnectManager();
                                                                    connect.deleteScheduleDataPoint(schedule.dataPoints.get(viewArray.indexOf(item)-1));
                                                                }
                                                            }).start();*/
                                                            new DeletePointTask(schedule.dataPoints.get(viewArray.indexOf(item)-1)).execute();
                                                            //NavigationWheel.wheelActivity.refreshSchedulePoints();
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.show();
                                                }
                                            }
                                        }else{
                                            Calendar timeConvert = Calendar.getInstance();
                                            if (schedule.scheduleType.equalsIgnoreCase("N")) {
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.filteredDataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                            }else{
                                                timeConvert.set(Calendar.HOUR_OF_DAY, schedule.dataPoints.get(viewArray.indexOf(item)).startTimeHour);
                                                timeConvert.set(Calendar.MINUTE, schedule.dataPoints.get(viewArray.indexOf(item)).startTimeMinute);
                                            }
                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            String editPointTime = formatter.format(timeConvert.getTime());

                                            if (schedule.scheduleType.equalsIgnoreCase("N")){
                                                if (!schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartDay && !schedule.filteredDataPoints.get(viewArray.indexOf(item)).isStartNight){
                                                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context);
                                                    myNewAlert.setMessage(R.string.delete_data_point);
                                                    myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                                                    myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            /*new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ConnectManager connect = new ConnectManager();
                                                                    connect.deleteScheduleDataPoint(schedule.filteredDataPoints.get(viewArray.indexOf(item)));
                                                                }
                                                            }).start();*/
                                                            new DeletePointTask(schedule.filteredDataPoints.get(viewArray.indexOf(item))).execute();
                                                            //NavigationWheel.wheelActivity.refreshSchedulePoints();
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.show();
                                                }
                                            }else{
                                                if (!schedule.dataPoints.get(viewArray.indexOf(item)).isStartDay && !schedule.dataPoints.get(viewArray.indexOf(item)).isStartNight){
                                                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context);
                                                    myNewAlert.setMessage(R.string.delete_data_point);
                                                    myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                                                    myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            /*new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ConnectManager connect = new ConnectManager();
                                                                    connect.deleteScheduleDataPoint(schedule.dataPoints.get(viewArray.indexOf(item)));
                                                                }
                                                            }).start();*/
                                                            new DeletePointTask(schedule.dataPoints.get(viewArray.indexOf(item))).execute();
                                                            //NavigationWheel.wheelActivity.refreshSchedulePoints();
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    myNewAlert.show();
                                                }
                                            }
                                        }


                                        return true;
                                    }
                                });
                            }
                        }
                    }
                }
            }else{
                currentIndex = 0;
            }
        }
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i("Scrolling", "X from [" + oldl + "] to [" + l + "]");
    }

    public void setViewArray(ArrayList<View> array){
        if (!centeredPointLocation.isEmpty()){
            centeredPointLocation.clear();
            centeredPointLocation = new ArrayList<Integer>();
        }
        viewArray = new ArrayList<View>();
        viewArray = array;
        onStart = true;
    }

    public void setDefaults(int center, int dataPointWidth, Schedule schedule, int nowIndex, int nightIndex, int dayIndex){
        LinearLayout viewCollection = (LinearLayout)findViewById(R.id.schedule_point_list);
        viewCollection.setPadding(center - dataPointWidth, 0, center - dataPointWidth, 0);
        this.schedule = schedule;
        this.nowIndex = nowIndex;
        this.nightIndex = nightIndex;
        this.dayIndex = dayIndex;
        //get the center points for all items
        for (View item: this.viewArray){
            centeredPointLocation.add(item.getLeft()  + halfPointWidth - centerOfScreen);
        }
        //fullScroll(FOCUS_LEFT);

    }

    public void setScheduleLayout(RelativeLayout scheduleLayout){
        this.scheduleLayout = scheduleLayout;
    }

    public class DeletePointTask extends AsyncTask<Void,Void,Void> {
        ScheduleDataPoint dataPoint;

        public DeletePointTask(ScheduleDataPoint dataPoint){
            this.dataPoint = dataPoint;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.deleteScheduleDataPoint(dataPoint);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            NavigationWheel.wheelActivity.refreshSchedulePoints();
        }
    }

}
