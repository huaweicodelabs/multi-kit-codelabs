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


extension UIColor {
    static func pink()-> UIColor {
        return UIColor(named: "pink") ?? .systemPink
    }
    
    static func purple() -> UIColor {
        return UIColor(named: "purple") ?? .systemPurple
    }
    
    static func background()-> UIColor {
        return UIColor(named: "background")  ?? .white
    }
    
    static func appButton()-> UIColor {
        return UIColor(named: "appButton") ?? .systemBlue
    }
    
    static func textColor()-> UIColor {
        return UIColor(named: "textColor") ?? .black 
    }
}
