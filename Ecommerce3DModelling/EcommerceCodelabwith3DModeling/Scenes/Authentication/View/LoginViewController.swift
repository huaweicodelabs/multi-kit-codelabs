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
import AGConnectAuth

final class LoginViewController: UIViewController {
    // TODO: Add scrollView
    private lazy var topStackView = UIStackView()
    private lazy var appIconImageView = UIImageView()
    private lazy var appNameLabel = UILabel()
    private lazy var loginStackView = UIStackView()
    private lazy var emailTextField = UITextField()
    private lazy var forgotPasswordContainerView = UIView()
    private lazy var passwordTextField = UITextField()
    private lazy var forgotPasswordButton = UIButton()
    private lazy var loginButton = UIButton()
    private lazy var bottomStackView = UIStackView()
    private lazy var signupLabel = UILabel()
    private lazy var signupButton = UIButton()
    
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
    
    private func prepareViews() {
        self.view.backgroundColor = UIColor.background()
        self.navigationItem.setHidesBackButton(true, animated: true)
        self.view.addSubview(self.topStackView)
        self.view.addSubview(self.loginStackView)
        self.view.addSubview(self.bottomStackView)
        
        self.topStackView.addArrangedSubview(self.appIconImageView)
        self.topStackView.addArrangedSubview(self.appNameLabel)
        
        self.forgotPasswordContainerView.addSubview(self.forgotPasswordButton)
        self.loginStackView.addArrangedSubview(self.emailTextField)
        self.loginStackView.addArrangedSubview(self.passwordTextField)
        self.loginStackView.addArrangedSubview(self.forgotPasswordContainerView)
        self.loginStackView.addArrangedSubview(self.loginButton)
        self.bottomStackView.addArrangedSubview(self.signupLabel)
        self.bottomStackView.addArrangedSubview(self.signupButton)
        
        self.topStackView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(35)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }
        
        self.appIconImageView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.height.width.equalTo(220)
        }
        
        self.loginStackView.snp.makeConstraints { make in
            make.top.equalTo(self.topStackView.snp.bottom).offset(30)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }
        
        self.emailTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.passwordTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.forgotPasswordButton.snp.makeConstraints { make in
            make.top.bottom.equalToSuperview()
            make.trailing.equalToSuperview().offset(-16)
            make.height.equalTo(20)
        }
        
        self.loginButton.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.bottomStackView.snp.makeConstraints { make in
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom)
            make.centerX.equalToSuperview()
        }
    }
    
    private func configureViews() {
        self.topStackView.axis = .vertical
        self.topStackView.spacing = 12
        self.topStackView.alignment = .center
        
        self.appIconImageView.image = UIImage(named: "appLogo")
        self.appNameLabel.text = "login.3D-Shopping".localize
        self.appNameLabel.textAlignment = .center
        self.appNameLabel.font = UIFont.appMainBold(fontSize: 24)
        self.appNameLabel.textColor = UIColor.purple()
        
        self.loginStackView.axis = .vertical
        self.loginStackView.spacing = 12
        
        self.emailTextField.setDefault(iconName: "envelope", placeHolder: "login.email".localize)
        self.passwordTextField.setDefault(iconName: "lock", placeHolder: "login.password".localize)
        self.passwordTextField.isSecureTextEntry = true
        
        self.forgotPasswordButton.setDefaultClearButton(buttonName: "login.forgotPasswordButton".localize, fontSize: 16)
        self.forgotPasswordButton.addTarget(self, action: #selector(forgotPasswordButtonAction), for: .touchUpInside)
        
        self.loginButton.setDefaultAppButton(buttonName: "login.loginButton".localize, fontSize: 20)
        self.loginButton.addTarget(self, action: #selector(loginButtonAction), for: .touchUpInside)
        
        self.bottomStackView.axis = .horizontal
        self.bottomStackView.spacing = 5
        
        self.signupLabel.textColor = UIColor.pink()
        self.signupLabel.font = UIFont.appMainRegular(fontSize: 15)
        self.signupLabel.text = "login.doHaveAnAccount".localize
        self.signupLabel.backgroundColor = .clear
        self.signupLabel.textAlignment = .center
        
        self.signupButton.setDefaultClearButton(buttonName: "login.signUp".localize, fontSize: 16)
        self.signupButton.addTarget(self, action: #selector(signupButtonAction), for: .touchUpInside)
    }
    
    @objc func forgotPasswordButtonAction(_ sender: UIButton) {
        let vc = ForgotPasswordViewController()
        vc.viewModel = viewModel
        vc.modalPresentationStyle = .fullScreen
        self.show(vc, sender: self)
    }
    
    @objc func loginButtonAction(_ sender: UIButton) {
        let email = self.emailTextField.text ?? ""
        let password = self.passwordTextField.text ?? ""
        self.viewModel.userLogin(email: email, password:password)
    }
    
    @objc func signupButtonAction(_ sender: UIButton) {
        let vc = SignupViewController()
        vc.viewModel = viewModel
        vc.modalPresentationStyle = .fullScreen
        self.show(vc, sender: self)
    }
}
