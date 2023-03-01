/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import Foundation

struct GeneralConstants {
    struct UserDefault {
        static let isLoginState = "LoginState"
       
    }
    
    struct ViewModelConstants {
            static let modelZip = "model.zip"
            static let caches = "Caches"
            static let dataTask = "dataTask"
            static let downloadModel = "downloadModel"
            static let model = "model"
            static let mtl = "mesh_texture.mtl"
            static let obj = "mesh_texture.obj"
            static let stringOfMtl = "newmtl material\nmap_Kd mesh_texture_material_map_Kd.jpg\n"
    }
    
    struct CloudDB {
        static let productTableForeignKey = "userid"
        static let cloudDBZoneName = "ECOM3DCODL"
    }
    
    struct CloudDBError {
        static let cloudDBObjectCreationError = "created cloud database object failed with reason"
    }
    
    struct HomePageViewControllerConstans {
               static let headerView = "Category"
               static let identifier = "HomePageCollectionViewCell"
    }
}
