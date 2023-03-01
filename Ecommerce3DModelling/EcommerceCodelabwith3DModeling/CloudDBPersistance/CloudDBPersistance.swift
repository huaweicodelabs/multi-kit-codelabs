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

struct CloudDBPersistance {
    
    typealias CloudDBpersistanceCallback = (_ isSuccess: Bool,_ error: Error?) -> Void
    private static let userId: Int? = {
        let id = AGCAuth.instance().currentUser?.uid ?? ""
        return Int(id)
    }()
    
    static func upsertUser(username: String, fullname: String, email: String, completion: @escaping CloudDBpersistanceCallback) {
        let user = user3()
        user.username = username
        user.fullname = fullname
        user.email = email
        if let userId = userId {
            CloudDBManager.shareInsatnce().executeUpsert(with: user, userId: userId) { isSuccess, error in
                completion(isSuccess,error)
            }
        }
    }
    
    static func upsertProduct(productID: Int, productName: String, category: String, price: Float64, imageURL: String, completion: @escaping CloudDBpersistanceCallback) {
        let product = product3()
        product.category = category
        product.price = NSNumber(value: price)
        product.image = imageURL
        product.name = productName
        if let userID = userId {
            product.userid = NSNumber(value: userID)
        }
        CloudDBManager.shareInsatnce().executeUpsert(with: product, productId: productID) { isSuccess, error in
            completion(isSuccess,error)
        }
    }
    
    static func updateUser(username: String, fullname:String, email:String, completion: @escaping CloudDBpersistanceCallback) {
        let user = user3()
        user.username = username
        user.fullname = fullname
        user.email = email
        if let userID = userId {
            user.id = NSNumber(value: userID)
        }
        CloudDBManager.shareInsatnce().executeUpdate(with: user) { isSuccess, error in
            completion(isSuccess,error)
        }
    }
    
    static func updateProduct(productID: NSNumber, productName: String, category: String, price: Float64, imageURL: String, completion: @escaping CloudDBpersistanceCallback) {
        let product = product3()
        product.category = category
        product.price = NSNumber(value: price)
        product.image = imageURL
        product.name = productName
        product.id = productID
        if let userID = userId {
            product.userid = NSNumber(value: userID)
        }
        CloudDBManager.shareInsatnce().executeUpdate(with: product) { isSuccess, error in
            completion(isSuccess,error)
        }
    }
    
    static func deleteUser(completion: @escaping CloudDBpersistanceCallback) {
        if let userID = userId {
            CloudDBManager.shareInsatnce().deleteAGCData(withUserID: userID) { isSuccess, error in
                completion(isSuccess,error)
            }
        }
    }
    
    static func deleteProduct(productID: Int, completion: @escaping CloudDBpersistanceCallback) {
        let productId = productID
        CloudDBManager.shareInsatnce().deleteAGCData(withProductID: productId) { isSuccess, error in
            completion(isSuccess,error)
        }
    }
    
    static func queryAllProductsWithUserID(completion: @escaping (_ data: [product3], _ error: Error?) -> Void) {
        if let userID = userId {
            CloudDBManager.shareInsatnce().queryAGCProductData(withFieldName: GeneralConstants.CloudDB.productTableForeignKey, value: userID, sortType: CloudDBManagerSortType.asc) { data, error in
                completion(data as? [product3] ?? [], error)
            }
        }
    }
}
