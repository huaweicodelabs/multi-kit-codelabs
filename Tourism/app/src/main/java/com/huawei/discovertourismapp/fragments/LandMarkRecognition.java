package com.huawei.discovertourismapp.fragments;

import static com.huawei.discovertourismapp.utils.Constants.CAMERA_PERMISSION;
import static com.huawei.discovertourismapp.utils.Constants.WRITE_EXTERNAL_PERMISSION;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.discovertourismapp.MainActivity;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.activity.MapActivity;
import com.huawei.discovertourismapp.activity.SearchPlaces;
import com.huawei.discovertourismapp.utils.AppLog;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.FileUtils;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.discovertourismapp.viewmodel.PageViewModel;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLCoordinate;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzer;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzerSetting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class LandMarkRecognition extends Fragment {

    private static final String TAG = "Recents";
    Bitmap bitmap;

    private PageViewModel pageViewModel;
    private MLRemoteLandmarkAnalyzer analyzer;

    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{CAMERA_PERMISSION, WRITE_EXTERNAL_PERMISSION};
    ImageView landmark_image;
    TextView landMark_text;
    ImageView map_nav;
    LinearLayout capture_layout;
    LinearLayout landmark_layout;
    double lat,lng;
    String location_name;
    Util util;
    public LandMarkRecognition() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment RecentsFragment.
     */
    public static LandMarkRecognition newInstance() {
        return new LandMarkRecognition();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //pageViewModel.setIndex(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        //final TextView textView = root.findViewById(R.id.section_label);
       /* pageViewModel.getText().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        util = new Util();
        capture_layout= root.findViewById(R.id.capture_layout);
        landmark_layout= root.findViewById(R.id.placeDetail_layout);
        final ImageView camera_image = root.findViewById(R.id.camera_img);
        final ImageView gallery_image = root.findViewById(R.id.gallery_img);
        landmark_image = root.findViewById(R.id.landmark_image);
        landMark_text = root.findViewById(R.id.landMark_text);
        map_nav = root.findViewById(R.id.map_nav);
        camera_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkingAllPermissionsGranted()) {
                    //startingCamera(); // start camera if permission has been granted by user
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    launcherForCamera.launch(takePicture);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }
            }
        });
        gallery_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkingAllPermissionsGranted()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    launcherForGallery.launch(intent);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                            REQUEST_CODE_PERMISSIONS);
                }
            }
        });
        map_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), MapActivity.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("location_name",location_name);
                startActivity(intent);
            }
        });
        landmark_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), SearchPlaces.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("location_name",location_name);
                if(bitmap!=null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("bitmap", byteArray);
                }
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return root;
    }
    ActivityResultLauncher<Intent> launcherForCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Log.d(TAG, "image_data--->" + data.getData());
                    File file = FileUtils.bitmapToFile(getActivity(),
                            (Bitmap) result.getData().getExtras().get("data"), "phoneNumber" + "userProfile.jpg");
                bitmap=(Bitmap) result.getData().getExtras().get("data");
                    capture_layout.setVisibility(View.GONE);
                    landmark_layout.setVisibility(View.VISIBLE);
                    landmark_image.setImageBitmap(bitmap);
                    analyzer(bitmap);
                }
            });
    ActivityResultLauncher<Intent> launcherForGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            capture_layout.setVisibility(View.GONE);
            landmark_layout.setVisibility(View.VISIBLE);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            landmark_image.setImageBitmap(bitmap);
            analyzer(bitmap);

        }
    });
    private void analyzer(Bitmap bitmap) {
        util.showProgressBar(getActivity());

        analyzer = MLAnalyzerFactory.getInstance().getRemoteLandmarkAnalyzer();
        MLRemoteLandmarkAnalyzerSetting settings = new MLRemoteLandmarkAnalyzerSetting.Factory()
                .setLargestNumOfReturns(1)
                .setPatternType(MLRemoteLandmarkAnalyzerSetting.STEADY_PATTERN)
                .create();
        this.analyzer = MLAnalyzerFactory.getInstance()
                .getRemoteLandmarkAnalyzer(settings);
        // Create an MLFrame by using android.graphics.Bitmap. Recommended image size: large than 640*640.
       // Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.landmark_image);
        MLFrame mlFrame = new MLFrame.Creator().setBitmap(bitmap).create();
        // Set ApiKey.
        MLApplication.getInstance().setApiKey(getResources().getString(R.string.api_key));
        // Set access token.
        // MLApplication.getInstance().setAccessToken(MainActivity.accessToken);
        Task<List<MLRemoteLandmark>> task = this.analyzer.asyncAnalyseFrame(mlFrame);
        task.addOnSuccessListener(new OnSuccessListener<List<MLRemoteLandmark>>() {
            @Override
            public void onSuccess(List<MLRemoteLandmark> landmarkResults) {
                // Processing logic for recognition success.
                displaySuccess(landmarkResults.get(0));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Processing logic for recognition failur
               displayFailure(e);
            }
        });
    }

    private boolean checkingAllPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkingAllPermissionsGranted()) {
                //startingCamera();
            } else {
                Toast.makeText(getActivity(), getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
    private void displayFailure(Exception exception) {
        String error = "Failure. ";
        try {
            MLException mlException = (MLException) exception;
            error += "error code: " + mlException.getErrCode() + "\n" + "error message: " + mlException.getMessage();
        } catch (Exception e) {
            error += e.getMessage();
        }
     //   this.mTextView.setText(error);
    }

    private void displaySuccess(MLRemoteLandmark landmark) {
        String result = "";
        if (landmark.getLandmark() != null) {
            result = "Landmark: " + landmark.getLandmark();
        }
        location_name=landmark.getLandmark();
        result += "\nPositions: ";
        if (landmark.getPositionInfos() != null) {
            for (MLCoordinate coordinate : landmark.getPositionInfos()) {
                result += "\nLatitude:" + coordinate.getLat();
                result += "\nLongitude:" + coordinate.getLng();
                lat=coordinate.getLat();
                lng=coordinate.getLng();
            }
        }
        this.landMark_text.setText(landmark.getLandmark()+"\n\n"+landmark.getLandmarkIdentity());
        util.stopProgressBar();
        // this.mTextView.setText(result);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.analyzer == null) {
            return;
        }
        try {
            this.analyzer.stop();
        } catch (IOException e) {
            Log.e(TAG, "Stop failed: " + e.getMessage());
        }


    }
}
