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

class TabBarController: UITabBarController, UITabBarControllerDelegate {
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let vc1 = UINavigationController(rootViewController: HomePageViewController())
               
        let vc2c = ProfileViewController()
        vc2c.viewModel = Model3dViewModel()
        let vc2 = UINavigationController(rootViewController: vc2c)
        
        let image1 = (UIImage(systemName: "house")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        let selectedImage1 = (UIImage(systemName: "house.fill")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        let icon1 = UITabBarItem(title: "tabBar.home".localize, image: image1, selectedImage: selectedImage1)
        
        let image2 = (UIImage(systemName: "person")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        let selectedImage2 = (UIImage(systemName: "person.fill")!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        let icon2 = UITabBarItem(title: "tabBar.me".localize, image: image2, selectedImage: selectedImage2)
        
        vc1.tabBarItem = icon1
        vc2.tabBarItem = icon2
        
        vc1.title = "tabBar.home".localize
        vc2.title = "tabBar.me".localize
        
        let appearance = tabBar.standardAppearance
        appearance.configureWithDefaultBackground()
        appearance.backgroundColor = .white
        if #available(iOS 15.0, *) {
            tabBar.scrollEdgeAppearance = appearance
        } else {
            tabBar.standardAppearance = appearance
        }

        setViewControllers([vc1, vc2], animated: true)
    }
    
    func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {
            return true;
    }
}

