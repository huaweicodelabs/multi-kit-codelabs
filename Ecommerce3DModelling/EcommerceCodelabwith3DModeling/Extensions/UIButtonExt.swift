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

extension UIButton {
    
    private func setDefaultButton(buttonName: String) {
        self.setTitle(buttonName.localize, for: .normal)
        self.setTitleColor(UIColor.pink(), for: .normal)
        self.layer.cornerRadius = 15
    }
    
    func setDefaultClearButton(buttonName: String, fontSize: CGFloat) {
        setDefaultButton(buttonName: buttonName)
        self.titleLabel!.font = UIFont.appMainBold(fontSize: fontSize)
        self.backgroundColor = .clear
    }
    
    func setDefaultAppButton(buttonName: String, fontSize: CGFloat) {
        setDefaultButton(buttonName: buttonName)
        self.titleLabel!.font = UIFont.appMainBold(fontSize: fontSize)
        self.backgroundColor = UIColor.appButton()
    }
    
}
