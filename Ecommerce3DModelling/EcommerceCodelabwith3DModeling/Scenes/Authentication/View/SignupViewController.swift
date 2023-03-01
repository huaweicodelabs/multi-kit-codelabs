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

final class SignupViewController: UIViewController {
//    TODO: Add scrollView
    private lazy var appIconImageView = UIImageView()
    private lazy var signupStackView = UIStackView()
    private lazy var emailTextField = UITextField()
    private lazy var fullNameTextField = UITextField()
    private lazy var userNameTextField = UITextField()
    private lazy var passwordTextField = UITextField()
    private lazy var repasswordTextField = UITextField()
    private lazy var verifyCodeTextField = UITextField()
    private lazy var verifyCodeButton = UIButton()
    private lazy var signupButton = UIButton()
    private lazy var bottomStackView = UIStackView()
    private lazy var loginLabel = UILabel()
    private lazy var loginButton = UIButton()
    
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
        self.view.addSubview(self.signupStackView)
        self.view.addSubview(self.bottomStackView)
        
        self.signupStackView.addArrangedSubview(self.emailTextField)
        self.signupStackView.addArrangedSubview(self.fullNameTextField)
        self.signupStackView.addArrangedSubview(self.userNameTextField)
        self.signupStackView.addArrangedSubview(self.passwordTextField)
        self.signupStackView.addArrangedSubview(self.repasswordTextField)
        self.signupStackView.addArrangedSubview(self.verifyCodeTextField)
        self.signupStackView.addArrangedSubview(self.signupButton)
        
        self.bottomStackView.addArrangedSubview(self.loginLabel)
        self.bottomStackView.addArrangedSubview(self.loginButton)
        
        self.appIconImageView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(10)
            make.centerX.equalToSuperview()
        }
        
        self.signupStackView.snp.makeConstraints { make in
            make.top.equalTo(self.appIconImageView.snp.bottom).offset(15)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }
        
        self.emailTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.fullNameTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.userNameTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.passwordTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.repasswordTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.verifyCodeTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.signupButton.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.bottomStackView.snp.makeConstraints { make in
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom)
            make.centerX.equalToSuperview()
        }
    }
    
    func configureViews() {
        self.signupStackView.axis = .vertical
        self.signupStackView.spacing = 12
        
        self.appIconImageView.image = UIImage(named: "appLogo")
        self.emailTextField.setDefault(iconName: "envelope", placeHolder: "sign.email".localize)
        self.fullNameTextField.setDefault(iconName: "person", placeHolder: "sign.fullName".localize)
        self.userNameTextField.setDefault(iconName: "person.fill", placeHolder: "sign.userName".localize)
        self.passwordTextField.setDefault(iconName: "lock", placeHolder: "sign.password".localize)
        self.passwordTextField.isSecureTextEntry = true
        self.repasswordTextField.setDefault(iconName: "lock.fill", placeHolder: "sign.repassword".localize)
        self.repasswordTextField.isSecureTextEntry = true
        self.verifyCodeTextField.setDefault(iconName: "envelope.badge.fill", placeHolder: "sign.verifyCode".localize)
        
        self.verifyCodeButton.setDefaultClearButton(buttonName: "sign.sendVerifyCodeButton".localize, fontSize: 15)
        self.verifyCodeButton.addTarget(self, action: #selector(verifyCodeButtonAction), for: .touchUpInside)
        self.verifyCodeTextField.rightView = self.verifyCodeButton
        self.verifyCodeTextField.rightViewMode = .always
        
        self.signupButton.setDefaultAppButton(buttonName: "sign.signupButton".localize, fontSize: 16)
        self.signupButton.addTarget(self, action: #selector(signupButtonAction), for: .touchUpInside)
        
        self.bottomStackView.axis = .horizontal
        self.bottomStackView.spacing = 5
        
        self.loginLabel.textColor = UIColor.pink()
        self.loginLabel.font = UIFont.appMainRegular(fontSize: 15)
        self.loginLabel.text = "login.doHaveAnAccount".localize
        self.loginLabel.backgroundColor = .clear
        self.loginLabel.textAlignment = .center
        
        self.loginButton.setDefaultClearButton(buttonName: "sign.loginButton", fontSize: 16)
        self.loginButton.addTarget(self, action: #selector(loginButtonAction), for: .touchUpInside)
    }
    
    @objc func verifyCodeButtonAction() {
        let email = self.emailTextField.text ?? ""
        self.viewModel.sendVerifyCodeForSignup(email: email)
    }
    
    @objc func signupButtonAction() {
        let email = self.emailTextField.text ?? ""
        let fullName = self.fullNameTextField.text ?? ""
        let userName = self.userNameTextField.text ?? ""
        let password = self.passwordTextField.text ?? ""
        let repassword = self.repasswordTextField.text ?? ""
        let verifyCode = self.verifyCodeTextField.text ?? ""
        
        self.viewModel.userSignup(email: email, password: password, repassword: repassword, verifyCode: verifyCode)
    }
    
    @objc func loginButtonAction() {
        let vc = LoginViewController()
        vc.viewModel = viewModel
        vc.modalPresentationStyle = .fullScreen
        self.show(vc, sender: self)
    }
}
