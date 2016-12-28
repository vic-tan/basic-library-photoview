/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tlf.basic.photoview.galleryfinal;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.tlf.basic.base.autolayout.AutoLayoutActivity;
import com.tlf.basic.base.systembartint.SystemBarTintManager;
import com.tlf.basic.photoview.R;
import com.tlf.basic.photoview.galleryfinal.model.PhotoInfo;
import com.tlf.basic.photoview.galleryfinal.permission.EasyPermissions;
import com.tlf.basic.photoview.galleryfinal.utils.CurrentUtils;
import com.tlf.basic.photoview.galleryfinal.utils.DeviceUtils;
import com.tlf.basic.photoview.galleryfinal.utils.MediaScanner;
import com.tlf.basic.photoview.galleryfinal.utils.SupportActivityManager;
import com.tlf.basic.photoview.galleryfinal.utils.SupportToastUtils;
import com.tlf.basic.photoview.galleryfinal.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tlf.basic.photoview.galleryfinal.ThemeConfig.THEME_COLOR;


/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/10 下午5:46
 */
public abstract class PhotoBaseActivity extends AutoLayoutActivity implements EasyPermissions.PermissionCallbacks {

    protected static String mPhotoTargetFolder;

    private Uri mTakePhotoUri;
    private MediaScanner mMediaScanner;

    protected int mScreenWidth = 720;
    protected int mScreenHeight = 1280;

    protected boolean mTakePhotoAction;//打开相机动作

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("takePhotoUri", mTakePhotoUri);
        outState.putString("photoTargetFolder", mPhotoTargetFolder);
    }




    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTakePhotoUri = savedInstanceState.getParcelable("takePhotoUri");
        mPhotoTargetFolder = savedInstanceState.getString("photoTargetFolder");
    }

    protected Handler mFinishHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finishGalleryFinalPage();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        SupportActivityManager.getActivityManager().addActivity(this);
        mMediaScanner = new MediaScanner(this);
        DisplayMetrics dm = DeviceUtils.getScreenPix(this);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        setSystemBarTint(Color.parseColor(THEME_COLOR));

    }

    /**
     * 如果不想让标题栏变色，或者更改其它变色，重写此方法那可
     */
    protected void setSystemBarTint(int statusBarTintResource) {
        applyKitKatTranslucency(statusBarTintResource);
    }


    /**
     * 标题栏变色
     *
     * @param statusBarTintResource
     */
    protected void applyKitKatTranslucency(int statusBarTintResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setStatusBarTintColor(statusBarTintResource);//通知栏所需颜色
        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaScanner != null) {
            mMediaScanner.unScanFile();
        }
        SupportActivityManager.getActivityManager().finishActivity(this);
    }

    public void toast(String msg) {
        SupportToastUtils.show(this,msg);
    }

    /**
     * 拍照
     */
    protected void takePhotoAction() {
        if (!DeviceUtils.existSDCard()) {
            String errormsg = getString(R.string.empty_sdcard);
            toast(errormsg);
            if (mTakePhotoAction) {
                resultFailure(errormsg, true);
            }
            return;
        }

        File takePhotoFolder = null;
        if (CurrentUtils.isEmpty(mPhotoTargetFolder)) {
            takePhotoFolder = GalleryFinal.getCoreConfig().getTakePhotoFolder();
        } else {
            takePhotoFolder = new File(mPhotoTargetFolder);
        }
        boolean suc = CurrentUtils.mkdirs(takePhotoFolder);
        File toFile = new File(takePhotoFolder, "IMG" + CurrentUtils.format(new Date(), "yyyyMMddHHmmss") + ".jpg");

        Log.d("PhotoBaseActivity","create folder=" + toFile.getAbsolutePath());
        if (suc) {
            mTakePhotoUri = Uri.fromFile(toFile);
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
            startActivityForResult(captureIntent, GalleryFinal.TAKE_REQUEST_CODE);
        } else {
            takePhotoFailure();
            Log.e("PhotoBaseActivity","create file failure");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GalleryFinal.TAKE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && mTakePhotoUri != null) {
                final String path = mTakePhotoUri.getPath();
                if (new File(path).exists()) {
                    final PhotoInfo info = new PhotoInfo();
                    info.setPhotoId(Utils.getRandom(10000, 99999));
                    info.setPhotoPath(path);
                    updateGallery(path);
                    takeResult(info);
                } else {
                    takePhotoFailure();
                }
            } else {
                takePhotoFailure();
            }
        }
    }

    private void takePhotoFailure() {
        String errormsg = getString(R.string.take_photo_fail);
        if (mTakePhotoAction) {
            resultFailure(errormsg, true);
        } else {
            toast(errormsg);
        }
    }

    /**
     * 更新相册
     */
    private void updateGallery(String filePath) {
        if (mMediaScanner != null) {
            mMediaScanner.scanFile(filePath, "image/jpeg");
        }
    }

    protected void resultData(ArrayList<PhotoInfo> photoList) {
        GalleryFinal.OnHanlderResultCallback callback = GalleryFinal.getCallback();
        int requestCode = GalleryFinal.getRequestCode();
        if (callback != null) {
            if (photoList != null && photoList.size() > 0) {
                callback.onHanlderSuccess(requestCode, photoList);
            } else {
                callback.onHanlderFailure(requestCode, getString(R.string.photo_list_empty));
            }
        }
        finishGalleryFinalPage();
    }

    protected void resultFailureDelayed(String errormsg, boolean delayFinish) {
        GalleryFinal.OnHanlderResultCallback callback = GalleryFinal.getCallback();
        int requestCode = GalleryFinal.getRequestCode();
        if (callback != null) {
            callback.onHanlderFailure(requestCode, errormsg);
        }
        if (delayFinish) {
            mFinishHanlder.sendEmptyMessageDelayed(0, 500);
        } else {
            finishGalleryFinalPage();
        }
    }

    protected void resultFailure(String errormsg, boolean delayFinish) {
        GalleryFinal.OnHanlderResultCallback callback = GalleryFinal.getCallback();
        int requestCode = GalleryFinal.getRequestCode();
        if (callback != null) {
            callback.onHanlderFailure(requestCode, errormsg);
        }
        if (delayFinish) {
            finishGalleryFinalPage();
        } else {
            finishGalleryFinalPage();
        }
    }

    private void finishGalleryFinalPage() {
        SupportActivityManager.getActivityManager().finishActivity(PhotoEditActivity.class);
        SupportActivityManager.getActivityManager().finishActivity(PhotoSelectActivity.class);
        Global.mPhotoSelectActivity = null;
        System.gc();
    }

    protected abstract void takeResult(PhotoInfo info);

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(List<String> list) {
    }

    @Override
    public void onPermissionsDenied(List<String> list) {
    }
}
