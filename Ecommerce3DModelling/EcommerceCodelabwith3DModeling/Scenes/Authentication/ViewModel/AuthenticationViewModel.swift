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

protocol AuthenticationDelegate: AnyObject {
    func routeToHome()
    func showError(title: String, message: String)
}

class AuthenticationViewModel {
    weak var delegate: AuthenticationDelegate?
    
    func userLogin(email: String, password: String) {
        NetworkHelper.login(email: email, password: password)
        { [weak self] (success, errorMessage) in
            if !success {
                self?.delegate?.showError(title: "auth.unableToLogin".localize, message: errorMessage ?? "")
                return
            } 
            self?.routeToHome()
        }
    }
    
    func userSignup(email: String, password: String, repassword: String, verifyCode: String) {
        if password != repassword {
            self.delegate?.showError(title: "auth.passwordDontMatch".localize, message: "auth.passwordDontMatchMessage".localize)
            return
        }
        
        NetworkHelper.signup(email: email, password: password, verifyCode: verifyCode)
        { [weak self] (success, errorMessage) in
            if !success {
                self?.delegate?.showError(title: "auth.unableToSignup".localize, message: errorMessage ?? "")
                return
            }
            self?.routeToHome()
        }
    }
    
    func userUpdatePassword(email:String, newPassword: String, veridyCode: String) {
        NetworkHelper.updatePassword(email: email, newPassword: newPassword, verifyCode: veridyCode)
        { [weak self] (success, errorMessage) in
            if !success {
                self?.delegate?.showError(title: "auth.unableToResetPassword".localize, message: errorMessage ?? "")
                return
            }
            self?.routeToHome()
        }
    }
    
    func sendVerifyCodeForSignup(email: String) {
        NetworkHelper.verifyCodeForSignup(email: email) { [weak self] (success, errorMessage) in
            if !success {
                self?.delegate?.showError(title: "auth.verifyCodeError".localize, message: errorMessage ?? "")
                return
            }
        }
    }
    
    func sendVerifyCodeForResetPassword(email: String) {
        NetworkHelper.verifyCodeForResetPassword(email: email) { [weak self] (success, errorMessage) in
            if !success {
                self?.delegate?.showError(title: "auth.verifyCodeError".localize, message: errorMessage ?? "")
                return
            }
        }
        
    }
    
    private func routeToHome(){
        UserDefaults.standard.setValue(true, forKey: GeneralConstants.UserDefault.isLoginState)
        self.delegate?.routeToHome()
    }
}


