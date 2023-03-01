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


import SnapKit
import UIKit

class HomePageViewController: UIViewController {

    private lazy var headerView: UILabel = {
        let headerView = UILabel()
        headerView.text = GeneralConstants.HomePageViewControllerConstans.headerView
        headerView.backgroundColor = UIColor.background()
        headerView.textAlignment = .center
        headerView.textColor = UIColor.black
        headerView.font = UIFont.appMainBold(fontSize: 18)
        return headerView
    }()
    
    private lazy var homePageCollectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .vertical
        layout.minimumLineSpacing = 10
        layout.minimumInteritemSpacing = 3
        layout.sectionInset = UIEdgeInsets(top: 10.0, left: 10.0, bottom: 10, right: 10.0)
        
        layout.itemSize = CGSize(width: (view.frame.size.width/3.5) - 3,
                                 height: (view.frame.size.height/3.5) - 118.5)
    
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.register(HomePageCollectionViewCell.self, forCellWithReuseIdentifier: GeneralConstants.HomePageViewControllerConstans.identifier)
        collectionView.dataSource = self
        collectionView.delegate = self
        return collectionView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.setNavigationBarHidden(true, animated: true)
        self.prepareViews()
        self.configureViews()
    }
    
    private func prepareViews() {
        self.view.addSubview(headerView)
        self.view.addSubview(homePageCollectionView)
        headerView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top)
            make.leading.equalToSuperview()
            make.trailing.equalToSuperview()
            make.height.equalTo(44)
        }
        
        homePageCollectionView.snp.makeConstraints({ make in
            make.top.equalTo(self.headerView.snp.bottom)
            make.leading.equalToSuperview()
            make.trailing.equalToSuperview()
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom)
        })
    }
    
    private func configureViews() {
        self.view.backgroundColor = UIColor.background()
        self.headerView.backgroundColor =  UIColor.background()
        self.homePageCollectionView.backgroundColor = UIColor.background()
    }
}

extension HomePageViewController: UICollectionViewDataSource , UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return HomePageColletionViewMenuEnum.allCases.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: GeneralConstants.HomePageViewControllerConstans.identifier, for: indexPath) as! HomePageCollectionViewCell
        cell.configure(label: HomePageColletionViewMenuEnum.allCases[indexPath.row].rawValue,
                        image: HomePageColletionViewMenuEnum.allCases[indexPath.row].rawValue)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    
    }
}

