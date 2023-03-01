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
import UIKit
import SnapKit

final class ActivityIndicatorAlertView: NSObject {
    private var targetView = UIView()
    private var alertView = UIView()
    private var titleLabel = UILabel()
    private var activityIndicator = UIActivityIndicatorView(style: .medium)
    private var cancelButton = UIButton()
    
    private var alertTitle: String
    
    var cancelAction: VoidHandler?
    
    init(on viewController: UIViewController, alertTitle: String) {
        self.targetView = viewController.view
        self.alertTitle = alertTitle
        
        super.init()
        self.prepareViews()
        self.configureViews()
    }
    
    private func prepareViews() {
        targetView.addSubview(alertView)
        alertView.addSubview(titleLabel)
        alertView.addSubview(activityIndicator)
        alertView.addSubview(cancelButton)
        
        alertView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.height.equalToSuperview().multipliedBy(0.15)
            make.width.equalToSuperview().multipliedBy(0.9)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(30)
        }
        
        activityIndicator.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(16)
            make.centerX.equalToSuperview()
        }
        
        cancelButton.snp.makeConstraints { make in
            make.top.equalTo(activityIndicator.snp.bottom).offset(20)
            make.leading.trailing.equalToSuperview()
            make.bottom.equalToSuperview()
        }
    }
    
    private func configureViews() {
        self.alertView.layer.cornerRadius = 8
        self.alertView.backgroundColor = .white
        
        self.titleLabel.text = self.alertTitle
        self.titleLabel.textAlignment = .center
        
        self.activityIndicator.startAnimating()
        
        self.cancelButton.layer.cornerRadius = 8
        self.cancelButton.layer.maskedCorners = [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        self.cancelButton.setTitle("alert.cancel".localize, for: .normal)
        self.cancelButton.backgroundColor = UIColor.appButton()
        self.cancelButton.addTarget(self, action: #selector(didTapCancelButton), for: .touchUpInside)
    }
    func removeAlertView() {
        self.activityIndicator.stopAnimating()
        self.alertView.removeFromSuperview()
    }
    
    @objc private func didTapCancelButton() {
        cancelAction?()
        self.removeAlertView()
    }
}
