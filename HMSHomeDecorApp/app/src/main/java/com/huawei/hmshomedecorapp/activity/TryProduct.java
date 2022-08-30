/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hmshomedecorapp.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.huawei.hms.scene.math.Vector3;
import com.huawei.hms.scene.sdk.render.Animator;
import com.huawei.hms.scene.sdk.render.Light;
import com.huawei.hms.scene.sdk.render.Model;
import com.huawei.hms.scene.sdk.render.Node;
import com.huawei.hms.scene.sdk.render.Renderable;
import com.huawei.hms.scene.sdk.render.Resource;
import com.huawei.hms.scene.sdk.render.Texture;
import com.huawei.hms.scene.sdk.render.Transform;
import com.huawei.hms.scene.sdk.ux.ar.ARNode;
import com.huawei.hms.scene.sdk.ux.ar.ARView;
import com.huawei.hmshomedecorapp.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TryProduct extends AppCompatActivity {
    private ARView arView;
    private Model testModel;
    private Node node;
    String paths;
    private boolean destroyed = false;
    private Texture skyBoxTexture;
    private Texture specularEnvTexture;
    private Texture diffuseEnvTexture;
    String threeDFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.try_product_home_decor);
        arView = findViewById(R.id.arview);
        threeDFileName = getIntent().getStringExtra("ThreeDFileName");
        arView.enablePlaneDisplay(true);
        loadAsset();
        addCameraAndLight();
        arView.addOnTapPlaneEventListener(tapPlaneResult -> {
            node = arView.getScene().createNodeFromModel(testModel);
            node.getComponent(Transform.descriptor())
                    .scale(new Vector3(0.01f, 0.01f, 0.01f));
            node.traverseDescendants(des -> {
                Renderable renderableComponent = des.getComponent(Renderable.descriptor());
                if (renderableComponent != null) {
                    renderableComponent.setCastShadow(true).setReceiveShadow(true);
                }
            });
            Animator animator = node.getComponent(Animator.descriptor());
            if (animator != null) {
                animator.play(animator.getAnimations().get(0));
            }
            ARNode arNode = tapPlaneResult.createARNode(node);
            arView.recordARNode(arNode);
        });
    }
    private void loadAsset() {
        if (testModel != null) {
            return;
        }
        Model.builder()
                .setUri(Uri.parse("Furniture/"+threeDFileName))
                .load(getApplicationContext(), new Resource.OnLoadEventListener<Model>() {
                    @Override
                    public void onLoaded(Model model) {
                        Toast.makeText(TryProduct.this, "load success.", Toast.LENGTH_SHORT).show();
                        testModel = model;
                    }

                    @Override
                    public void onException(Exception exception) {
                        Toast.makeText(TryProduct.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addCameraAndLight() {
        Node lightNode = arView.getScene().createNode();
        lightNode.addComponent(Light.descriptor())
                .setType(Light.Type.DIRECTIONAL)
                .setIntensity(30.f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        arView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        arView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arView.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String filepath = data.getDataString();
        super.onActivityResult(requestCode, resultCode, data);
    }


}
