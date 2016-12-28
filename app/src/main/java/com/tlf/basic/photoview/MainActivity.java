package com.tlf.basic.photoview;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String one ="./gradlew clean build bintrayUpload -PbintrayUser=vic-tan -PbintrayKey=8970dbd317d720d9b1176877f90c9bd6a431e650 -PdryRun=false";

    }
}
