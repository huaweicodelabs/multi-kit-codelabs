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
import AGConnectAuth

class NetworkHelper {
    static func login(email: String, password: String, completion: @escaping (_ success: Bool, _ errorMessage: String?) -> ()) {
        let credential = AGCEmailAuthProvider.credential(withEmail: email, password: password)
        AGCAuth.instance().signIn(credential: credential).onSuccess{ (result) in
           completion(true, nil)
        }.onFailure{ (error) in
            completion(false, error.localizedDescription)
        }
    }
    
    static func signup(email: String, password: String, verifyCode: String, completion: @escaping (_ success: Bool, _ errorMessage: String?) -> ()) {
        AGCAuth.instance().createUser(withEmail: email, password: password, verifyCode: verifyCode).onSuccess{ (result) in
            completion(true, nil)
        }.onFailure{ (error) in
            completion(false, error.localizedDescription)
        }
    }
    
    static func updatePassword(email: String, newPassword: String, verifyCode: String, completion: @escaping (_ success: Bool, _ errorMessage: String?) -> ()) {
        AGCAuth.instance().resetPassword(withEmail:email, newPassword:newPassword, verifyCode: verifyCode)
        .onSuccess{ (result) in
            completion(true, nil)
        }.onFailure{ (error) in
            completion(false, error.localizedDescription)
        }
    }
    
    static func verifyCodeForSignup(email: String, completion: @escaping (_ success: Bool, _ errorMessage: String?) -> ()) {
        let setting = AGCVerifyCodeSettings.init(action:AGCVerifyCodeAction.registerLogin, locale:nil, sendInterval:30)
        AGCEmailAuthProvider.requestVerifyCode(withEmail: email, settings: setting).onSuccess { (result) in
            completion(true, nil)
        }.onFailure{ (error) in
            completion(false, error.localizedDescription)
        }
    }
    
    static func verifyCodeForResetPassword (email: String, completion: @escaping (_ success: Bool, _ errorMessage: String?) -> ()) {
        let setting = AGCVerifyCodeSettings.init(action:AGCVerifyCodeAction.resetPassword, locale:nil, sendInterval:30)
        AGCEmailAuthProvider.requestVerifyCode(withEmail:email, settings:setting).onSuccess{ (result) in
            completion(true, nil)
        }.onFailure{ (error) in
            completion(false, error.localizedDescription)
        }
    }
}
