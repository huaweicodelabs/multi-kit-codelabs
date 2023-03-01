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


protocol AddProductDelegate: AnyObject  {
    func routeToProfilePage()
}

class AddProductViewModel {
    weak var delegate: AddProductDelegate?
    
    private var count = 0
    
    func addProduct(nameTextField: String,
                    categoryTextField: String,
                    priceTextField: String,
                    completion: @escaping (_ success: Bool) -> Void) {
        CloudDBPersistance.queryAllProductsWithUserID { [weak self] data, error in
            self?.count =  data.count + 1
            
            if let cnt = self?.count {
                CloudDBPersistance.upsertProduct(productID:cnt,
                                                 productName: nameTextField,
                                                 category: categoryTextField,
                                                 price: Double(priceTextField)!,
                                                 imageURL: "")
                { isSuccess, error in
                    completion(isSuccess)
                }
            }
            
        }
    }
}
