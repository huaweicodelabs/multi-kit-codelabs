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
import SnapKit
import UIKit

protocol AddProductViewControllerDelegate: AnyObject {
    
    func didAddProduct()
    
}


final class AddProductViewController: UIViewController, AddProductDelegate {

    var containerStackView = UIStackView()
    var pageNameLabel = UILabel()
    var nameTextField = UITextField()
    var priceTextField = UITextField()
    var categoryTextField = UITextField()
    var addImageButtonView = UIView()
    var addImageButton = UIButton()
    var images = [UIImage]()
    let screenSize: CGRect = UIScreen.main.bounds
    var count = 0
    
    weak var delegate: AddProductViewControllerDelegate?
    
    var viewModel: AddProductViewModel! {
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
        self.view.addSubview(containerStackView)
        self.containerStackView.addArrangedSubview(self.nameTextField)
        self.containerStackView.addArrangedSubview(self.categoryTextField)
        self.containerStackView.addArrangedSubview(self.priceTextField)
        self.containerStackView.addArrangedSubview(self.addImageButton)
        self.containerStackView.addArrangedSubview(self.addImageButtonView)
        self.addImageButtonView.addSubview(self.addImageButton)
        self.containerStackView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(screenSize.height/2-250)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.bottom.equalToSuperview().offset(-(screenSize.height/2-90))
        }
        
        self.nameTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.priceTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.categoryTextField.snp.makeConstraints { make in
            make.height.equalTo(50)
        }
        
        self.addImageButtonView.snp.makeConstraints { make in
            make.height.equalTo(90)
            make.width.equalTo(60)
        }
        
        self.addImageButton.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.bottom.equalToSuperview()
            make.leading.equalToSuperview().offset(150)
            make.trailing.equalToSuperview().offset(-150)
            make.height.equalTo(90)
            make.width.equalTo(60)
        }
    }
    
    private func configureViews() {
        
        self.containerStackView.axis = .vertical
        self.containerStackView.spacing = 12
        
        self.nameTextField.text = ""
        self.priceTextField.text = ""
        self.categoryTextField.text = ""
        
        self.nameTextField.textAlignment = .center
        self.priceTextField.textAlignment = .center
        self.categoryTextField.textAlignment = .center
        
        self.nameTextField.setDefault(iconName: "lock", placeHolder: "Product Name")
        self.priceTextField.setDefault(iconName: "lock", placeHolder: "Product Price")
        self.categoryTextField.setDefault(iconName: "lock", placeHolder: "Product Category")
        
        let icon = (UIImage(named: "AddImage")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        self.addImageButton.setImage(icon, for: .normal)
        self.addImageButton.addTarget(self, action: #selector(addNewImageButtonAction), for: .touchUpInside)
        
    }
    
    @objc func addNewImageButtonAction(){
        guard let addN = nameTextField.text, let addC = categoryTextField.text, let addP = priceTextField.text else {
            return
        }
        
        self.viewModel.addProduct(nameTextField: addN,
                                  categoryTextField: addC,
                                  priceTextField: addP)
        { [weak self] isSuccess in
            if !isSuccess {
                return
            }
            
            DispatchQueue.main.async {
                self?.dismiss(animated: true, completion: {
                    self?.delegate?.didAddProduct()
                })
            }
        }
        
    }
    
    func routeToProfilePage() {
        

        
    }

}
