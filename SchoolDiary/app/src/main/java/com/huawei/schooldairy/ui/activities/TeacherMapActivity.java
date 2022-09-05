package com.huawei.schooldairy.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.schooldairy.R;
import com.huawei.schooldairy.model.Loginmapping;
import com.huawei.schooldairy.data.ObjectTypeInfoHelper;
import com.huawei.schooldairy.model.UserData;

public class TeacherMapActivity extends AppCompatActivity {

    RelativeLayout lay_scan;
    private Dialog dialog = null;

    private CloudDBZone mCloudDBZone;
    private SharedPreferences preferences;
    private ListenerHandler mRegister;

    private ProgressDialog progressDialog;

    public static final String TAG = "TeacherMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_map);

        lay_scan = (RelativeLayout) findViewById(R.id.lay_scan);

        AGConnectCloudDB.initialize(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(TeacherMapActivity.this);
        establishconnection();
        addMappingSubscription();

        TextView  txt_heading1 = (TextView) findViewById(R.id.txt_heading1);
        txt_heading1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(TeacherMapActivity.this, MainActivity.class);
                startActivity(i);

            }
        });





        lay_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

                String content = "{\"ChildID\":\"" + user.getUid() + "\"," +
                        "\"ChildName\":\"" + user.getDisplayName() + "\"," +
                        "\"EmailID\":\"" + user.getEmail() + "\"}";

                int type = HmsScan.QRCODE_SCAN_TYPE;
                int width = 400;
                int height = 400;

                HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapMargin(3).create();

                try {
                    // If the HmsBuildBitmapOption object is not constructed, set options to null.
                    Bitmap qrBitmap = ScanUtil.buildBitmap(content, type, width, height, options);



                    dialog = new Dialog(TeacherMapActivity.this);
                    dialog.setContentView(R.layout.qr_dialog);
                    dialog.setCanceledOnTouchOutside(true);

                    ImageView image = dialog.findViewById(R.id.iv_qr_code);
                    image.setImageBitmap(qrBitmap);
                    dialog.show();

                } catch (WriterException e) {
                    Log.w("buildBitmap", e);
                }
            }
        });
    }


    public void establishconnection()
    {

        AGConnectCloudDB.initialize(this);
        AGConnectCloudDB mCloudDB = AGConnectCloudDB.getInstance();
        if(mCloudDB != null) {
            try {
                mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());


                CloudDBZoneConfig mConfig = new CloudDBZoneConfig("SchoolDB",
                        CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                        CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
                mConfig.setPersistenceEnabled(true);
                Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
                openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
                    @Override
                    public void onSuccess(CloudDBZone cloudDBZone) {
                        Log.d("TAG", "open clouddbzone success");
                        mCloudDBZone = cloudDBZone;
                        // Add subscription after opening cloudDBZone success
                        // addNotificationSubscription();
                        Toast.makeText(TeacherMapActivity.this, "openclouddbzonesuccess", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("TAG", "open clouddbzone failed for " + e);

                        Toast.makeText(TeacherMapActivity.this, "open clouddbzone failed for " + e, Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (AGConnectCloudDBException e) {
                Log.e("HomeFragment ", e.getMessage());
            } catch (Exception e) {
                Log.e("HomeFragmentJava__ ", e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void addMappingSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        try {
            AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

            CloudDBZoneQuery<Loginmapping> snapshotQuery = CloudDBZoneQuery.where(Loginmapping.class)
                    .equalTo("TeacherID", user.getUid());
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.d(TAG, "subscribeSnapshot: " + e);
        }
    }


    private OnSnapshotListener<Loginmapping> mSnapshotListener = new OnSnapshotListener<Loginmapping>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Loginmapping> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.d(TAG, "onSnapshot: " + e);
                return;
            }
            CloudDBZoneObjectList<Loginmapping> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();

            insertUserType();


           /* List<NotificationDetails> notificationlist = new ArrayList<NotificationDetails>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        NotificationDetails notificationDetails = snapshotObjects.next();
                        notificationlist.add(notificationDetails);
                    }

                    if(notificationlist.size()>0)
                    {
                        Log.d("onsnapsjotlistener_____",notificationlist.get(0).getMessage());
                        shownotification(notificationlist.get(0).getMessage());
                        updateNotificationFlag(notificationlist.get(0));
                    }

                }
            } catch (Exception snapshotException) {
                Log.d(TAG, "onSnapshot:(getObject) " + snapshotException);
            } finally {
                cloudDBZoneSnapshot.release();
            }*/
        }
    };


    public void insertUserType()
    {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it");
            return;
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        progressDialog.setCancelable(true);


        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        UserData userData = new UserData();
        userData.setUserID(user.getUid());
        userData.setUserName(user.getDisplayName());
        userData.setUserType("1");


        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(userData);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {

                progressDialog.dismiss();

                Log.d("TAG", "upsert_sucess_parent " + " records");

                Toast.makeText(TeacherMapActivity.this, "UserSelection_ " + cloudDBZoneResult + " records", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("USER_TYPE", 1);
                editor.apply();

                Intent i = new Intent(TeacherMapActivity.this, HomeActivity.class);
                startActivity(i);
                finish();

            }
        });
        upsertTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                progressDialog.dismiss();

                //  mUiCallBack.updateUiOnError("Insert book info failed");
                Log.d("TAG", "insert_failed " + e.getLocalizedMessage() + " records");

                Toast.makeText(TeacherMapActivity.this, "insert_failed " + e.getLocalizedMessage() + " records", Toast.LENGTH_SHORT).show();


            }
        });
    }


}