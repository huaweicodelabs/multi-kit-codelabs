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

extension UITextField {
    
    func setIcon(_ image: UIImage) {
        let iconView = UIImageView(frame: CGRect(x: 10, y: 5, width: 20, height: 20))
        iconView.image = image
        
        let iconContainerView: UIView = UIView(frame: CGRect(x: 20, y: 0, width: 35, height: 30))
        iconContainerView.addSubview(iconView)
        leftView = iconContainerView
        leftViewMode = .always
    }
    
    func setDefault(iconName: String, placeHolder: String) {
        let attribute = [ NSAttributedString.Key.foregroundColor: UIColor.pink().withAlphaComponent(0.6) ]
        let attributeStr = NSAttributedString(string: placeHolder.localize, attributes: attribute)
        
        let icon = (UIImage(systemName: iconName)!.withTintColor(UIColor.pink(), renderingMode: .alwaysOriginal))
        
        self.setIcon(icon)
        self.attributedPlaceholder = attributeStr
        self.backgroundColor = UIColor(white: 1, alpha: 0.4)
        self.textColor = UIColor.pink()
        self.layer.cornerRadius = 15
        
    }
}
