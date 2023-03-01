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


import UIKit
import AGConnectCore
import AGConnectDatabase
import AGConnectAuth
import Modeling3dKit


@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    var dbZone: AGCCloudDBZone?
    var agcConnectCloudDB = AGConnectCloudDB.shareInstance()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        AGCInstance.startUp()
        Modeling3dReconstructTask.sharedManager().initSDK()
        
        self.window = UIWindow(frame: UIScreen.main.bounds)
        var vc: UIViewController!
        
        CloudDBManager.shareInsatnce().prepare { isSuccess, error in
            print(isSuccess);
        }

        let isLogin = UserDefaults.standard.bool(forKey: GeneralConstants.UserDefault.isLoginState)
        if isLogin {
            vc = TabBarController()
        } else {
            let login =  LoginViewController()
            let viewModel = AuthenticationViewModel()
            login.viewModel = viewModel
            vc = login
        }
        
        self.window?.rootViewController = vc
        self.window?.makeKeyAndVisible()
        
        return true
    }
}

extension AppDelegate {
    func executeUpsert(withUsers users: [user3], complete: @escaping (_ success: Bool, _ error: Error?) -> Void) {
        if users.isEmpty {
            return
        }
        
        self.dbZone?.executeUpsert(users, onCompleted: { count, error in
            complete(error == nil, error)
        })
    }
        
    func loginAGCWithComplete(completion: @escaping(_ success: Bool, _ error: String?) -> ()) {
        var error: NSError?
        AGConnectCloudDB.initEnvironment(&error)
        print(GeneralConstants.CloudDB.cloudDBZoneName)
        guard error == nil else {
            return
        }
        
        self.agcConnectCloudDB = AGConnectCloudDB.shareInstance()
        var createError: NSError?
        self.agcConnectCloudDB.createObjectType(AGCCloudDBObjectTypeInfoHelper.obtainObjectTypeInfo(),
                                                error: &createError)
        
        if createError != nil {
            print("\(GeneralConstants.CloudDBError.cloudDBObjectCreationError): \(createError?.localizedDescription)");
        }
        
        
        let zoneName = GeneralConstants.CloudDB.cloudDBZoneName
        let zoneConfig = AGCCloudDBZoneConfig(zoneName: zoneName,
                                              syncMode: AGCCloudDBZoneSyncMode.cloudCache,
                                              accessMode: AGCCloudDBZoneAccessMode.public)
        zoneConfig.persistence = true
        self.agcConnectCloudDB.openZone2(zoneConfig,
                                         allowCreate: true)
        { [weak self] zone, error in
            if error != nil {
                print("\(GeneralConstants.CloudDBError.cloudDBObjectCreationError): \(error?.localizedDescription ?? "")")
                
                completion(false, error?.localizedDescription)
                return
            }
            
            self?.dbZone = zone
            self?.agcConnectCloudDB.enableNetwork(zoneName)
            
            completion(true, nil)
        }
    }
    
   
}

