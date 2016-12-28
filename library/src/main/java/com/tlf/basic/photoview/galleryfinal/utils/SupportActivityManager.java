package com.tlf.basic.photoview.galleryfinal.utils;

import android.app.Activity;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.Stack;

/**
 * Desction:应用程序Activity管理类：用于Activity管理和应用程序退出
 * Author:pengjianbo
 * Date:15/9/17 下午4:48
 */
public class SupportActivityManager {

    private static Stack<Activity> activityStack;
    private static SupportActivityManager instance;

    private SupportActivityManager(){}

    /**
     * 单一实例
     */
    public static SupportActivityManager getActivityManager(){
        if(instance==null){
            instance=new SupportActivityManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity){
        if(activityStack==null){
            activityStack=new Stack<>();
        }
        activityStack.add(activity);
    }



    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity){
        if( activityStack == null ) {
            return;
        }
        if(activity!=null){
            activityStack.remove(activity);
            activity.finish();
            activity=null;
        }
    }



    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls){
        if( activityStack == null ) {
            return;
        }
        Iterator<Activity> iterator = activityStack.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if(activity != null && activity.getClass().equals(cls) ){
                activity.finish();
                iterator.remove();
            }
        }
    }



    /**
     * 根据ActivityName获取堆中Activity实例
     * @param activityName
     * @return
     */
    public Activity getActivity(String activityName) {
        Iterator<Activity> iterator = activityStack.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if(activity != null && TextUtils.equals(activity.getClass().getName(), activityName)){
                return activity;
            }
        }
        return null;
    }


}
