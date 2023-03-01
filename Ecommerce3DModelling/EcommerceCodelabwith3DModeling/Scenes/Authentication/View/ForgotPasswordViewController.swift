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
import SnapKit

final class ForgotPasswordViewController: UIViewController {
    
//  TODO: Add scrollView
    private lazy var appIconImageView = UIImageView()
    private lazy var forgotPasswordStackView = UIStackView()
    private lazy var emailTextField = UITextField()
    private lazy var newPasswordTextField = UITextField()
    private lazy var verifyCodeTextField = UITextField()
    private lazy var verifyCodeButton = UIButton()
    private lazy var resetPasswordButton = UIButton()
    
    var viewModel: AuthenticationViewModel! {
        didSet {
            viewModel.delegate = self
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        self.prepareViews()
        self.configureViews()
    }
    
    func prepareViews() {
        self.view.backgroundColor = UIColor.background()
        self.navigationItem.setHidesBackButton(true, animated: true)
        self.view.addSubview(self.appIconImageView)
        self.view.addSubview(self.forgotPasswordStackView)
        
        self.forgotPasswordStackView.addArrangedSubview(self.emailTextField)
        self.forgotPasswordStackView.addArrangedSubview(self.newPasswordTextField)
        self.forgotPasswordStackView.addArrangedSubview(self.verifyCodeTextField)
        self.forgotPasswordStackView.addArrangedSubview(self.resetPasswordButton)
        
        self.appIconImageView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(40)
            make.centerX.equalToSuperview()
            make.height.width.equalTo(100)
        }
        
        self.forgotPasswordStackView.snp.makeConstraints { make in
            make.top.equalTo(self.appIconImageView.snp.bottom).offset(15)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }
        
        self.emailTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.newPasswordTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.verifyCodeTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.resetPasswordButton.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
    }
    
    func configureViews() {
        self.forgotPasswordStackView.axis = .vertical
        self.forgotPasswordStackView.spacing = 12
        
        self.appIconImageView.image = UIImage(named: "appLogo")
        self.emailTextField.setDefault(iconName: "envelope", placeHolder: "forgotPassword.email".localize)
        self.newPasswordTextField.setDefault(iconName: "lock", placeHolder: "forgotPassword.newPassword".localize)
        self.newPasswordTextField.isSecureTextEntry = true
        self.verifyCodeTextField.setDefault(iconName: "envelope.badge.fill", placeHolder: "forgotPassword.verifyCode".localize)
        self.verifyCodeButton.setDefaultClearButton(buttonName: "forgotPassword.sendVerifyCode".localize, fontSize: 15)
        self.verifyCodeButton.addTarget(self, action: #selector(verifyCodeButtonAction), for: .touchUpInside)
        self.verifyCodeTextField.rightView = self.verifyCodeButton
        self.verifyCodeTextField.rightViewMode = .always
        
        self.resetPasswordButton.setDefaultAppButton(buttonName: "forgotPassword.resetPasswordButton".localize, fontSize: 16)
        self.resetPasswordButton.addTarget(self, action: #selector(resetPasswordButtonAction), for: .touchUpInside)
    }
    
    @objc func verifyCodeButtonAction() {
        let email = self.emailTextField.text ?? ""
        self.viewModel.sendVerifyCodeForResetPassword(email: email)
    }
    
    @objc func resetPasswordButtonAction() {
        let email = self.emailTextField.text ?? ""
        let newPassword = self.newPasswordTextField.text ?? ""
        let verifyCode = self.verifyCodeTextField.text ?? ""
        self.viewModel.userUpdatePassword(email: email, newPassword: newPassword, veridyCode: verifyCode)
    }
}
