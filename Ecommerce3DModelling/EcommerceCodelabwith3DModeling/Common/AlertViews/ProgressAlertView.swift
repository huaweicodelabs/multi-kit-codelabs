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
import Modeling3dKit

final class ProgressAlertView: NSObject {
    private var targetView = UIView()
    private var alertView = UIView()
    private var titleLabel = UILabel()
    private var progressBar = UIProgressView()
    private var completeLabel = UILabel()
    private var cancelButton = UIButton()
    
    private var alertTitle: String
    private var isUpload: Bool
    private var taskId: String
    
    var deleteActionForUpload: VoidHandler?
    
    init(on viewController: UIViewController, alertTitle: String, isUpload: Bool, taskId: String) {
        self.targetView = viewController.view
        self.alertTitle = alertTitle
        self.isUpload = isUpload
        self.taskId = taskId
        
        super.init()
        self.prepareViews()
        self.configureViews()
    }
    
    private func prepareViews() {
        targetView.addSubview(alertView)
        alertView.addSubview(titleLabel)
        alertView.addSubview(progressBar)
        alertView.addSubview(completeLabel)
        alertView.addSubview(cancelButton)
        
        alertView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.height.equalToSuperview().multipliedBy(0.2)
            make.width.equalToSuperview().multipliedBy(0.9)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(30)
        }
        
        progressBar.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(20)
            make.leading.equalToSuperview().offset(20)
            make.trailing.equalToSuperview().offset(-20)
            make.height.equalTo(10)
        }

        completeLabel.snp.makeConstraints { make in
            make.top.equalTo(progressBar.snp.bottom).offset(20)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(20)
        }

        cancelButton.snp.makeConstraints { make in
            make.top.equalTo(completeLabel.snp.bottom).offset(20)
            make.leading.trailing.equalToSuperview()
            make.bottom.equalToSuperview()
        }
    }
    
    private func configureViews() {
        self.alertView.layer.cornerRadius = 8
        self.alertView.backgroundColor = .white
        
        self.titleLabel.text = self.alertTitle
        self.titleLabel.textAlignment = .center
        
        self.completeLabel.textAlignment = .center
        
        self.cancelButton.layer.cornerRadius = 8
        self.cancelButton.layer.maskedCorners  =  [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        self.cancelButton.setTitle("alert.cancel".localize, for: .normal)
        self.cancelButton.backgroundColor = UIColor.appButton()
        self.cancelButton.addTarget(self, action: #selector(didTapCancelButton), for: .touchUpInside)
    }
    
    @objc private func didTapCancelButton() {
        if (!isUpload) {
           Modeling3dReconstructTask.sharedManager().cancelDownloadTask(withTaskId: taskId) { retCode, retMsg in
               print("cancelDownloadTask retCode:", retCode)
               print("cancelDownloadTask retMsg:", retMsg)
               self.removeAlertView()
           }
       } else {
           Modeling3dReconstructTask.sharedManager().cancelUploadTask(withTaskId: taskId) { retCode, retMsg in
               print("cancelUploadTask retCode:", retCode)
               print("cancelUploadTask retMsg:", retMsg)
               self.deleteActionForUpload?()
               self.removeAlertView()
           }
       }
    }
    
    func updateProgress(progressValue: Float) {
        self.progressBar.setProgress(progressValue, animated: true)
        self.completeLabel.text = "progressAlertView.completed".localize + String((progressValue * 100).rounded())
    }
    
    func removeAlertView() {
        self.alertView.removeFromSuperview()
    }
}
