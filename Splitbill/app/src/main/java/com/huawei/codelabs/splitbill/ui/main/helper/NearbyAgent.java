/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.codelabs.splitbill.ui.main.helper;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;


import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentAccountBinding;
import com.huawei.codelabs.splitbill.databinding.FragmentFileDetailsBinding;
import com.huawei.codelabs.splitbill.databinding.FragmentSendExpenseDetailsBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.DeviceAdapter;
import com.huawei.codelabs.splitbill.ui.main.adapter.FriendsListAdapter;
import com.huawei.codelabs.splitbill.ui.main.models.Device;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.nearby.Nearby;
import com.huawei.hms.nearby.StatusCode;
import com.huawei.hms.nearby.discovery.BroadcastOption;
import com.huawei.hms.nearby.discovery.ConnectCallback;
import com.huawei.hms.nearby.discovery.ConnectInfo;
import com.huawei.hms.nearby.discovery.ConnectResult;
import com.huawei.hms.nearby.discovery.DiscoveryEngine;
import com.huawei.hms.nearby.discovery.Policy;
import com.huawei.hms.nearby.discovery.ScanEndpointCallback;
import com.huawei.hms.nearby.discovery.ScanEndpointInfo;
import com.huawei.hms.nearby.discovery.ScanOption;
import com.huawei.hms.nearby.transfer.Data;
import com.huawei.hms.nearby.transfer.DataCallback;
import com.huawei.hms.nearby.transfer.TransferEngine;
import com.huawei.hms.nearby.transfer.TransferStateUpdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NearbyAgent {
    public static final int REQUEST_CODE_SCAN_ONE = 0X01;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private final String TAG = "Nearby_Agent";
    private final String mFileServiceId = "NearbyAgentFileService";
    private final String mEndpointName = android.os.Build.DEVICE;
    int lineYAxis = 350;
    FragmentAccountBinding fragmentAccountBinding;
    FragmentSendExpenseDetailsBinding fragmentSendExpenseDetailsBinding;
    FragmentFileDetailsBinding fragmentFileDetailsBinding;
    private MainActivity mContext = null;
    private TransferEngine mTransferEngine = null;
    private DiscoveryEngine mDiscoveryEngine = null;
    private List<File> mFiles = new ArrayList<>();
    private String mRemoteEndpointId;
    private String mRemoteEndpointName;
    private String mScanInfo;
    private String mRcvedFilename = null;
    private Bitmap mResultImage;
    private String mFileName;
    private long mStartTime = 0;
    private float mSpeed = 60;
    private String mSpeedStr = "60";
    private boolean isTransfer = false;
    private final DataCallback mDataCbSender =
            new DataCallback() {
                @Override
                public void onReceived(String endpointId, Data data) {
                    if (data.getType() == Data.Type.BYTES) {
                        String msg = new String(data.asBytes(), UTF_8);
                        if (msg.equals("Receive Success")) {
                            Log.d(TAG, "Received ACK. Send next.");
                            sendOneFile();
                        }
                    }
                }

                @Override
                public void onTransferUpdate(String string, TransferStateUpdate update) {
                    if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_SUCCESS) {
                    } else if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_IN_PROGRESS) {
                        showProgressSpeedSender(update);
                    } else if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_FAILURE) {
                        Log.d(TAG, "Transfer failed.");
                    } else {
                        Log.d(TAG, "Transfer cancelled.");
                    }
                }
            };
    private Data incomingFile = null;
    private final DataCallback mDataCbRcver =
            new DataCallback() {
                @Override
                public void onReceived(String endpointId, Data data) {
                    if (data.getType() == Data.Type.BYTES) {
                        String msg = new String(data.asBytes(), UTF_8);
                        mRcvedFilename = msg;
                        Log.d(TAG, "received filename: " + mRcvedFilename);
                        isTransfer = true;
                        fragmentFileDetailsBinding.tvMainDesc.setText(new StringBuilder("Receiving file ").append(mRcvedFilename).append(" from ").append(mRemoteEndpointName + ".").toString());
                        fragmentFileDetailsBinding.pbMainDownload.setVisibility(View.VISIBLE);
                    } else if (data.getType() == Data.Type.FILE) {
                        incomingFile = data;
                    } else {
                        Log.d(TAG, "received stream. ");
                    }
                }

                @Override
                public void onTransferUpdate(String string, TransferStateUpdate update) {
                    if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_SUCCESS) {
                    } else if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_IN_PROGRESS) {
                        showProgressSpeedReceiver(update);
                        if (update.getBytesTransferred() == update.getTotalBytes()) {
                            Log.d(TAG, "File transfer done. Rename File.");
                            renameFile();
                            Log.d(TAG, "Send Ack.");
                            fragmentFileDetailsBinding.tvMainDesc.setText(new StringBuilder("Transfer success. Speed: ").append(mSpeedStr).append("MB/s. \nView the File at /Sdcard/Download/Nearby"));
                            mTransferEngine.sendData(mRemoteEndpointId, Data.fromBytes("Receive Success".getBytes(StandardCharsets.UTF_8)));
                            isTransfer = false;
                        }
                    } else if (update.getStatus() == TransferStateUpdate.Status.TRANSFER_STATE_FAILURE) {
                        Log.d(TAG, "Transfer failed.");
                    } else {
                        Log.d(TAG, "Transfer cancelled.");
                    }
                }
            };
    private final ConnectCallback mConnCbRcver =
            new ConnectCallback() {
                @Override
                public void onEstablish(String endpointId, ConnectInfo connectionInfo) {
                    Log.d(TAG, "Accept connection.");
                    mRemoteEndpointName = connectionInfo.getEndpointName();
                    mRemoteEndpointId = endpointId;
                    mDiscoveryEngine.acceptConnect(endpointId, mDataCbRcver);
                }

                @Override
                public void onResult(String endpointId, ConnectResult result) {
                    if (result.getStatus().getStatusCode() == StatusCode.STATUS_SUCCESS) {
                        Log.d(TAG, "Connection Established. Stop Discovery.");
                        mDiscoveryEngine.stopBroadcasting();
                        mDiscoveryEngine.stopScan();
                        fragmentFileDetailsBinding.tvMainDesc.setText("Connected.");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.d(TAG, "Disconnected.");
                    if (isTransfer == true) {
                        fragmentSendExpenseDetailsBinding.tvMainDesc.setVisibility(View.GONE);
                        fragmentSendExpenseDetailsBinding.tvMainDesc.setText("Connection lost.");
                    }
                }
            };
    private final ScanEndpointCallback mDiscCb =
            new ScanEndpointCallback() {
                @Override
                public void onFound(String endpointId, ScanEndpointInfo discoveryEndpointInfo) {
                    if (discoveryEndpointInfo.getName().equals(mScanInfo)) {

                        Log.d(TAG, "Found endpoint:" + discoveryEndpointInfo.getName() + ". Connecting.");
                        mDiscoveryEngine.requestConnect(mEndpointName, endpointId, mConnCbRcver);
                    }
                }

                @Override
                public void onLost(String endpointId) {
                    Log.d(TAG, "Lost endpoint.");
                }
            };
    private ArrayList<Device> deviceList;
    private final ConnectCallback mConnCbSender =
            new ConnectCallback() {
                @Override
                public void onEstablish(String endpointId, ConnectInfo connectionInfo) {
                    Log.d(TAG, "Accept connection.");
                    mDiscoveryEngine.acceptConnect(endpointId, mDataCbSender);
                    fragmentSendExpenseDetailsBinding.rcDevice.setHasFixedSize(true);
                    fragmentSendExpenseDetailsBinding.rcDevice.setLayoutManager(new LinearLayoutManager(mContext));
                    Device device = new Device();
                    device.setDeviceName(connectionInfo.getEndpointName());
                    deviceList.add(device);
                    DeviceAdapter deviceAdapter = new DeviceAdapter(deviceList);
                    fragmentSendExpenseDetailsBinding.rcDevice.setAdapter(deviceAdapter);
                    deviceAdapter.notifyDataSetChanged();
                    mRemoteEndpointName = connectionInfo.getEndpointName();
                    mRemoteEndpointId = endpointId;
                }

                @Override
                public void onResult(String endpointId, ConnectResult result) {
                    if (result.getStatus().getStatusCode() == StatusCode.STATUS_SUCCESS) {
                        Log.d(TAG, "Connection Established. Stop discovery. Start to send file.");
                        mDiscoveryEngine.stopScan();
                        mDiscoveryEngine.stopBroadcasting();
                        sendOneFile();
                        fragmentSendExpenseDetailsBinding.barcodeImage.setVisibility(View.GONE);
                        fragmentSendExpenseDetailsBinding.tvMainDesc.setText(new StringBuilder("MB/s. \nView the File at /Sdcard/Download/Nearby").append(mFileName).append(" to ").append(mRemoteEndpointName).append("."));
                        fragmentSendExpenseDetailsBinding.pbMainDownload.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.d(TAG, "Disconnected.");
                    if (isTransfer == true) {
                        fragmentSendExpenseDetailsBinding.pbMainDownload.setVisibility(View.GONE);
                        fragmentSendExpenseDetailsBinding.tvMainDesc.setText("Connection lost.");
                    }
                }
            };

    public NearbyAgent(MainActivity context) {
        mContext = context;
        mDiscoveryEngine = Nearby.getDiscoveryEngine(context);
        deviceList = new ArrayList<>();
        mTransferEngine = Nearby.getTransferEngine(context);
        if (context instanceof MainActivity) {
            ActivityCompat.requestPermissions(context, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    public NearbyAgent(MainActivity context, FragmentAccountBinding fragmentAccountBinding) {
        this.fragmentAccountBinding = fragmentAccountBinding;
        this.mContext = context;
        mDiscoveryEngine = Nearby.getDiscoveryEngine(context);
        mTransferEngine = Nearby.getTransferEngine(context);
        if (context instanceof MainActivity) {
            ActivityCompat.requestPermissions(context, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }


    }

    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) {
            return Constants.UnknownFile;
        }
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) {
            return Constants.UnknownFile;
        }
        return documentFile.getName();
    }

    private void showProgressSpeedSender(TransferStateUpdate update) {
        long transferredBytes = update.getBytesTransferred();
        long totalBytes = update.getTotalBytes();
        long curTime = System.currentTimeMillis();
        Log.d(TAG, "Transfer in progress. Transferred Bytes: "
                + transferredBytes + " Total Bytes: " + totalBytes);
        fragmentSendExpenseDetailsBinding.pbMainDownload.setProgress((int) (transferredBytes * 100 / totalBytes));
        if (mStartTime == 0) {
            mStartTime = curTime;
        }
        if (curTime != mStartTime) {
            mSpeed = ((float) transferredBytes) / ((float) (curTime - mStartTime)) / 1000;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            mSpeedStr = myformat.format(mSpeed);
            fragmentSendExpenseDetailsBinding.tvMainDesc.setText(new StringBuilder("Transfer in Progress. Speed: ").append(mSpeedStr).append("MB/s."));
        }
        if (transferredBytes == totalBytes) {
            mStartTime = 0;
        }
    }

    public void loadScanCode(Bitmap mResultImage) {
        // Inflate dialog main
        fragmentSendExpenseDetailsBinding.barcodeImage.setVisibility(View.VISIBLE);
        fragmentSendExpenseDetailsBinding.barcodeImage.setImageBitmap(mResultImage);
    }

    public File createPdf(List<FriendsListAdapter.FriendsUI> friendsUIList, Bitmap scaledImageBitmap, FragmentActivity activity) {
        // create a new document
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(Constants.PAGEWIDTH, Constants.PAGEHEIGHT, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        canvas.drawBitmap(scaledImageBitmap, 0, 0, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50);
        paint.setColor(activity.getResources().getColor(android.R.color.black));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Invoice" + System.currentTimeMillis(), Constants.PAGEWIDTH / 2, 260, paint);
        paint.setStrokeWidth(2f);
        // horizontal lines
        canvas.drawLine(Constants.PAGEWIDTH / 2, 300, 550, 300, paint);
        canvas.drawText("Amount" + "     " + "Participants", Constants.PAGEWIDTH / 2, 400, paint);
        lineYAxis = 450;
        for (FriendsListAdapter.FriendsUI friendsUI :friendsUIList) {
            canvas.drawText(friendsUI.getFriendsName() + "     " + friendsUI.getAmount() +"", Constants.PAGEWIDTH / 2, Constants.LINEYAXIS + 50, paint);
            lineYAxis = lineYAxis + 50;
        }
        // finish the page
        document.finishPage(page);
        // draw text on the graphics object of the page
        // Create Page 2
        pageInfo = new PdfDocument.PageInfo.Builder(Constants.PAGEWIDTH, Constants.PAGEHEIGHT, 2).create();
        page = document.startPage(pageInfo);
        document.finishPage(page);
        // write the document content
        String directory_path = Environment.getExternalStorageDirectory().getPath() + Constants.CREATEINVOICE;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = new StringBuilder().append(directory_path).append("invoice").append(System.currentTimeMillis()).append(".pdf").toString();
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            Log.e("main", "error " + e.toString());

        }
        // close the document
        document.close();
        return filePath;
    }

    public void sendFile(File file, FragmentSendExpenseDetailsBinding fragmentSendExpenseDetailsBinding) {
        this.fragmentSendExpenseDetailsBinding = fragmentSendExpenseDetailsBinding;

        init();
        mFiles.add(file);
        sendFilesInner();
    }

    public void sendFiles(List<File> files) {
        init();
        mFiles = files;
        sendFilesInner();
    }

    public void sendFolder(File folder) {
        init();
        File[] subFile = folder.listFiles();
        for (int i = 0; i < subFile.length; i++) {
            if (!subFile[i].isDirectory()) {
                mFiles.add(subFile[i]);
                Log.d(TAG, "Travel folder: " + subFile[i].getName());
            }
        }
        sendFilesInner();
    }

    private void sendFilesInner() {

        /* generate bitmap */
        try {
            //Generate the barcode.
            HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapMargin(1).setBitmapColor(Color.BLACK).setBitmapBackgroundColor(Color.WHITE).create();
            mResultImage = ScanUtil.buildBitmap(mEndpointName, HmsScan.QRCODE_SCAN_TYPE, Constants.BARCODE_SIZE, Constants.BARCODE_SIZE, options);
            loadScanCode(mResultImage);
        } catch (WriterException e) {
            Log.e(TAG, e.toString());
        }
        /* start broadcast */
        BroadcastOption.Builder advBuilder = new BroadcastOption.Builder();
        advBuilder.setPolicy(Policy.POLICY_P2P);
        mDiscoveryEngine.startBroadcasting(mEndpointName, mFileServiceId, mConnCbSender, advBuilder.build());
        Log.d(TAG, "Start Broadcasting.");

    }

    public void receiveFile(FragmentFileDetailsBinding fragmentFileDetailsBinding) {
        /* scan bitmap */
        this.fragmentFileDetailsBinding = fragmentFileDetailsBinding;
        Log.d("TAG", "start");
        init();
        HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE).create();
        ScanUtil.startScan(mContext, REQUEST_CODE_SCAN_ONE, options);
        Log.d("TAG", "Sent");
    }

    public void onScanResult(Intent data) {
        if (data == null) {
            Log.d("TAG", "fail");
            fragmentFileDetailsBinding.tvMainDesc.setText("Scan Failed.");
            return;
        }
        /* save endpoint name */
        HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
        mScanInfo = obj.getOriginalValue();
        /* start scan*/
        ScanOption.Builder scanBuilder = new ScanOption.Builder();
        scanBuilder.setPolicy(Policy.POLICY_P2P);
        mDiscoveryEngine.startScan(mFileServiceId, mDiscCb, scanBuilder.build());
        Log.d(TAG, "Start Scan.");
        fragmentFileDetailsBinding.tvMainDesc.setText(new StringBuilder().append("Connecting to ").append(mScanInfo).append("..."));
    }

    private void sendOneFile() {
        Data filenameMsg = null;
        Data filePayload = null;
        isTransfer = true;
        Log.d(TAG, "Left " + mFiles.size() + " Files to send.");
        if (mFiles.isEmpty()) {
            Log.d(TAG, "All Files Done. Disconnect");
            fragmentSendExpenseDetailsBinding.tvMainDesc.setText(R.string.all_files_sent);
            fragmentSendExpenseDetailsBinding.pbMainDownload.setVisibility(View.GONE);
            fragmentSendExpenseDetailsBinding.tvHeading.setVisibility(View.GONE);
            mDiscoveryEngine.disconnectAll();
            isTransfer = false;
            return;
        }
        try {
            mFileName = mFiles.get(0).getName();
            filePayload = Data.fromFile(mFiles.get(0));
            mFiles.remove(0);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
            return;
        }
        filenameMsg = Data.fromBytes(mFileName.getBytes(StandardCharsets.UTF_8));
        Log.d(TAG, "Send filename: " + mFileName);
        mTransferEngine.sendData(mRemoteEndpointId, filenameMsg);
        Log.d(TAG, "Send Payload.");
        mTransferEngine.sendData(mRemoteEndpointId, filePayload);
    }

    private void renameFile() {
        if (incomingFile == null) {
            Log.d(TAG, "incomingFile is null");
            return;
        }
        File rawFile = incomingFile.asFile().asJavaFile();
        Log.d(TAG, "raw file: " + rawFile.getAbsolutePath());
        File targetFileName = new File(rawFile.getParentFile(), mRcvedFilename);
        Log.d(TAG, "rename to : " + targetFileName.getAbsolutePath());
        Uri uri = incomingFile.asFile().asUri();
        if (uri == null) {
            boolean result = rawFile.renameTo(targetFileName);
            if (!result) {
                Log.e(TAG, "rename failed");
            } else {
                Log.e(TAG, "rename Succeeded ");
            }
        } else {
            try {
                openStream(uri, targetFileName);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } finally {
                delFile(uri, rawFile);
            }
        }
    }

    private void openStream(Uri uri, File targetFileName) throws IOException {
        InputStream in = mContext.getContentResolver().openInputStream(uri);
        Log.e(TAG, "open input stream successfuly");
        try {
            copyStream(in, new FileOutputStream(targetFileName));
            Log.e(TAG, "copyStream successfuly");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            in.close();
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } finally {
            out.close();
        }
    }

    private void delFile(Uri uri, File payloadfile) {
        // Delete the original file.
        mContext.getContentResolver().delete(uri, null, null);
        if (!payloadfile.exists()) {
            Log.e(TAG, "delete original file by uri successfully");
        } else {
            Log.e(TAG, "delete  original file by uri failed and try to delete it by File delete");
            payloadfile.delete();
            if (payloadfile.exists()) {
                Log.e(TAG, "fail to delete original file");
            } else {
                Log.e(TAG, "delete original file successfully");
            }
        }
    }

    private void showProgressSpeedReceiver(TransferStateUpdate update) {
        long transferredBytes = update.getBytesTransferred();
        long totalBytes = update.getTotalBytes();
        long curTime = System.currentTimeMillis();
        Log.d(TAG, "Transfer in progress. Transferred Bytes: "
                + transferredBytes + " Total Bytes: " + totalBytes);
        fragmentFileDetailsBinding.pbMainDownload.setProgress((int) (transferredBytes * 100 / totalBytes));
        if (mStartTime == 0) {
            mStartTime = curTime;
        }
        if (curTime != mStartTime) {
            mSpeed = ((float) transferredBytes) / ((float) (curTime - mStartTime)) / 1000;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            mSpeedStr = myformat.format(mSpeed);
            fragmentFileDetailsBinding.tvMainDesc.setText(new StringBuilder().append("Transfer in Progress. Speed: ").append(mSpeedStr).append("MB/s."));
        }
        if (transferredBytes == totalBytes) {
            mStartTime = 0;
        }
    }

    private void init() {
        if (fragmentSendExpenseDetailsBinding != null) {
            fragmentSendExpenseDetailsBinding.pbMainDownload.setProgress(0);
            fragmentSendExpenseDetailsBinding.pbMainDownload.setVisibility(View.GONE);
            fragmentSendExpenseDetailsBinding.tvMainDesc.setText("");
            fragmentSendExpenseDetailsBinding.barcodeImage.setVisibility(View.GONE);
        }
        mDiscoveryEngine.disconnectAll();
        mDiscoveryEngine.stopScan();
        mDiscoveryEngine.stopBroadcasting();
        mFiles.clear();
    }
}
