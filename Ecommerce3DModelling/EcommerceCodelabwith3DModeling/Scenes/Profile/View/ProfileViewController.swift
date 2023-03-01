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
import PhotosUI
import Modeling3dKit

final class ProfileViewController: UIViewController, UINavigationControllerDelegate, PHPickerViewControllerDelegate, Model3dDelegate {
    let layout = UICollectionViewFlowLayout()
    private lazy var collectionView = UICollectionView(frame: .zero, collectionViewLayout: UICollectionViewFlowLayout())
    private var addNewProductButton = UIButton()
    
    
    private var products = [product3]()
    private var currentProduct = product3()
    private var counter = 0
    private var imageSet = [[UIImage]]()
    
    var viewModel: Model3dViewModel! {
        didSet {
            viewModel.delegate = self
            viewModel.actionTableReload = {
                self.collectionView.reloadData()
            }
            viewModel.clearImageSet = {
                self.imageSet.removeAll()
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.setHidesBackButton(true, animated: true)
        self.modalPresentationStyle = .fullScreen
        collectionView.dataSource = self
        collectionView.delegate = self
        
        self.prepareViews()
        self.configureViews()
        
        self.viewModel.getCurrentProduct { [weak self] product, errorMessage in
            guard errorMessage == nil, let product = product else {
                return
            }
            
            self?.currentProduct = product
            
            self?.getListData()
        }
    }
    
    private func prepareViews() {
        self.view.backgroundColor = UIColor.background()
        self.navigationItem.setHidesBackButton(true, animated: true)
        
        self.view.addSubview(self.collectionView)
        self.view.addSubview(self.addNewProductButton)
        
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top)
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }
        
        self.addNewProductButton.snp.makeConstraints { make in
            make.bottom.equalTo(self.collectionView.snp.bottom).offset(-15)
            make.trailing.equalTo(self.collectionView.snp.trailing).offset(-5)
            make.height.equalTo(60)
            make.width.equalTo(60)
        }
    }
    
    private func configureViews() {
        self.collectionView.register(ProductViewCell.self, forCellWithReuseIdentifier: "Cell")
        self.collectionView.backgroundColor = .clear
        
        self.addNewProductButton.backgroundColor = UIColor.appButton()
        self.addNewProductButton.layer.cornerRadius = 30
        let icon = (UIImage(systemName: "plus")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        self.addNewProductButton.setImage(icon, for: .normal)
        self.addNewProductButton.addTarget(self, action: #selector(goToAddProductVC), for: .touchUpInside)
    }
    
    @objc func goToAddProductVC() {
        let vc = AddProductViewController()
        vc.delegate = self
        vc.viewModel = AddProductViewModel()
        vc.modalPresentationStyle = .fullScreen
        self.show(vc, sender: self)
    }
    
    func pickPhotos(){
        var config = PHPickerConfiguration()
        config.selectionLimit = 200
        config.filter = PHPickerFilter.images
        config.preferredAssetRepresentationMode = .current
        
        let imagePicker = PHPickerViewController(configuration: config)
        imagePicker.delegate = self
        
        present(imagePicker, animated: true, completion: nil)
    }
    
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        
        if results.isEmpty {
            picker.dismiss(animated: true)
            return
        }
        
        let dispatchGroup = DispatchGroup()
        var images = [UIImage]()
        
        results.forEach { result in
            dispatchGroup.enter()
            result.itemProvider.loadObject(ofClass: UIImage.self) { (object, error) in
                defer {
                    dispatchGroup.leave()
                }
                guard let image = object as? UIImage, error == nil else {
                    return
                }
                images.append(image)
            }
        }
        
        dispatchGroup.notify(queue: .main) {
            self.imageSet.append(images)
            self.collectionView.reloadData()
            self.model3dUpload()
        }
    }
    
    func model3dUpload() {
        self.viewModel.upload(imageSet: self.imageSet[0], viewController: self)
    }
    
    func getListData() {
        self.viewModel.model.removeAll()
        let storedModel = Modeling3dReconstructTask.sharedManager().getLocationFileArray()
        if let models = storedModel as? [Modeling3dReconstructTaskModel] {
            self.viewModel.model.append(contentsOf: models)
            
            DispatchQueue.main.async {
                self.collectionView.reloadData()
            }
        }
    }
}

extension ProfileViewController: UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.width/2.1, height: collectionView.frame.height/3)
    }
}

extension ProfileViewController: UICollectionViewDelegate, UICollectionViewDataSource{
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.viewModel.model.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "Cell",
                                                            for: indexPath) as? ProductViewCell else { return UICollectionViewCell() }
        let image = Modeling3dReconstructTask.sharedManager().getCoverImage(withTaskId: self.viewModel.model[indexPath.item].taskId)
        viewModel.currentCellIndex = indexPath.item
        cell.viewModel = viewModel
        cell.configurecell(image: image,
                           nameLabel: self.currentProduct.name,
                           priceLabel:"$\(self.currentProduct.price)",
                           viewController: self)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.viewModel.preview(viewController: self)
        let objFile = FileOperations.getObjFile(taskId: self.viewModel.model[indexPath.item].taskId)
        
        if FileManager.default.fileExists(atPath: objFile.path) {
            let previewvc = PreviewViewController(objURL: objFile)
            self.show(previewvc, sender: self)
        }
    }
}

extension ProfileViewController: AddProductViewControllerDelegate {
    
    func didAddProduct() {
        self.viewModel.getCurrentProduct { [weak self] product, errorMessage in
            guard errorMessage == nil, let product = product else {
                return
            }
            
            self?.currentProduct = product
            
            DispatchQueue.main.async {
                self?.pickPhotos()
            }
        }
    }
}
