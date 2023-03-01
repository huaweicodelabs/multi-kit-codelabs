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

class ProductViewCell: UICollectionViewCell, Model3dDelegate {
    private lazy var productStackView = UIStackView()
    private lazy var productImageView = UIImageView()
    private lazy var productNameLabel = UILabel()
    private lazy var productPriceLabel = UILabel()
    private lazy var popupMenuButton = UIButton()
    
    private lazy var controller = UIViewController()

    var viewModel: Model3dViewModel! {
        didSet {
            viewModel.delegate = self
        }
    }
    
    func configurecell(image: UIImage, nameLabel: String, priceLabel: String, viewController: UIViewController){
        productImageView.image = image
        productNameLabel.text = nameLabel
        productPriceLabel.text = priceLabel
        self.controller = viewController
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.prepareViews()
        self.configureViews()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.prepareViews()
        self.configureViews()
    }
    
    private func prepareViews() {
        addSubview(productStackView)
        self.productStackView.addArrangedSubview(self.productImageView)
        self.productStackView.addArrangedSubview(self.productNameLabel)
        self.productStackView.addArrangedSubview(self.productPriceLabel)
        addSubview(popupMenuButton)
        
        self.productStackView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(10)
            make.leading.equalToSuperview().offset(10)
            make.trailing.equalToSuperview().offset(-10)
            make.bottom.equalToSuperview().offset(-10)
        }
        
        self.productNameLabel.snp.makeConstraints { make in
            make.height.equalTo(20)
        }
        
        self.productPriceLabel.snp.makeConstraints { make in
            make.height.equalTo(20)
        }
        
        self.popupMenuButton.snp.makeConstraints { make in
            make.bottom.equalTo(self.productStackView.snp.bottom).offset(-5)
            make.trailing.equalTo(self.productStackView.snp.trailing)
        }
    }
    
    private func configureViews() {
        backgroundColor = .white
        clipsToBounds = true
        layer.cornerRadius = 9
        
        self.productStackView.axis = .vertical
        self.productStackView.spacing = 5
        self.productStackView.alignment = .fill
        self.productStackView.distribution = .fill
        
        self.productImageView.clipsToBounds = true
        self.productImageView.layer.cornerRadius = 9
        
        self.productNameLabel.textColor = UIColor.textColor()

        self.productPriceLabel.textColor = UIColor.textColor()
        
        let query = UIAction(title: "productCell.query".localize, image: nil) { (action) in
            self.viewModel.query(viewController: self.controller)
         }

        let download = UIAction(title: "productCell.download".localize, image: nil) { (action) in
             self.viewModel.download(viewController: self.controller)
         }
        
        let delete = UIAction(title: "productCell.delete".localize, image: nil) { (action) in
            self.viewModel.delete()
        }

        let menu = UIMenu(options: .displayInline, children: [query, download, delete])
        let icon = (UIImage(systemName: "ellipsis.circle.fill")!.withTintColor(UIColor.appButton(), renderingMode: .alwaysOriginal))
        self.popupMenuButton.setImage(icon, for: .normal)
        self.popupMenuButton.menu = menu
        self.popupMenuButton.showsMenuAsPrimaryAction = true
    }
}
