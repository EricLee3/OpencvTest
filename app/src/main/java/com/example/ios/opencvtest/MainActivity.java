package com.example.ios.opencvtest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat img_input;
    private Mat img_result;
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;

    public native int convertNativeLib(long matAddrInput, long matAddrResult);

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.CAMERA"};

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private boolean hasPermissions(String[] permissions)  {
        int ret = 0;
        for (String perms:permissions)  {
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED))  {
                return false;
            }
        }
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions)  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)  {
        @Override
        public void onManagerConnected(int status)  {
            switch (status)  {
                case LoaderCallbackInterface.SUCCESS:  {
                    mOpenCvCameraView.enableView();
                } break;
                default:  {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults)  {
        switch(permsRequestCode)  {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0)  {
                    boolean camreaAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  {
                        if (!camreaAccepted)  {
                            showDialogforPermission("You should get permission for App.");
                            return;
                        } else  {
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg)  {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this);
        myDialog.setTitle("Alarm");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()  {
            public void onClick(DialogInterface arg0, int arg1)  {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
            }
        });
        myDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1)  {
                finish();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!hasPermissions(PERMISSIONS)) {
            requestNecessaryPermissions(PERMISSIONS);
        } else  {
        }

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onPause()  {
        super.onResume();
        if (!OpenCVLoader.initDebug())  {
            Log.d(TAG, "onResume::Internal OpenCVlibrary not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else  {
            Log.d(TAG, "onResume:: OpenCV library found inside package. Using it!");
        }
    }

    public void onDestroy()  {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height)  {

    }

    @Override
    public void onCameraViewStopped()  {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)  {
        img_input = inputFrame.rgba();
        img_result = new Mat();

        convertNativeLib(img_input.getNativeObjAddr(), img_result.getNativeObjAddr());
        return img_result;

    }
}
