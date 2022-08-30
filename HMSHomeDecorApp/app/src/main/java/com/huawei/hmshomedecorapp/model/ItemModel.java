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

package com.huawei.hmshomedecorapp.model;

import com.huawei.hmshomedecorapp.R;

import java.util.List;

public class ItemModel {
    int imageId;
    String applianceName;
    String threeDimensionalFileName;

    public ItemModel(int imgId,String itemName) {
        this.imageId = imgId;
        this.applianceName = itemName;
        switch(itemName){
            // Case statements

            case "Smart TV":
                this.threeDimensionalFileName = "air_conditioner/scene.gltf";
                break;
            case "Washing Machine":
                this.threeDimensionalFileName = "air_conditioner/scene.gltf";
                break;
            case "Ceiling Light":
                this.threeDimensionalFileName = "air_conditioner/scene.gltf";
                break;
            case "Cooler":
                this.threeDimensionalFileName = "air_conditioner/scene.gltf";
                break;
            case "Chair":
                this.threeDimensionalFileName = "worn_chair_3d_scan/scene.gltf";
                break;
            case "Garden Chair":
                this.threeDimensionalFileName = "wooden_table/scene.gltf";
                break;
            case "Room Chair":
                this.threeDimensionalFileName = "bar_stool/scene.gltf";
                break;
            case "Office Chair":
                this.threeDimensionalFileName = "oslo_accent_chair_marl_grey/scene.gltf";
                break;
            case "Office Table":
                this.threeDimensionalFileName = "oslo_3_seater_sofa_marl_grey_with_dark_oak_legs/scene.gltf";
                break;
            default:
                this.threeDimensionalFileName = "air_conditioner/scene.gltf";
        }
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getApplianceName() {
        return applianceName;
    }

    public void setApplianceName(String applianceName) {
        this.applianceName = applianceName;
    }

    public String getThreeDimensionalFileName() {
        return threeDimensionalFileName;
    }

    public void setThreeDimensionalFileName(String threeDimensionalFileName) {
        this.threeDimensionalFileName = threeDimensionalFileName;
    }
}
