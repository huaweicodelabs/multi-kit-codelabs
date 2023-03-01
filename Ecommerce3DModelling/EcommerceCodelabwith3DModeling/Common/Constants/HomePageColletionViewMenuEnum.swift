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

enum HomePageColletionViewMenuEnum: String, CaseIterable, RawRepresentable {
    case Shoe
    case Bag
    case Sport
    case Watch
    case Phone
    case Television
    case Food
    case Decoration
    case Activity
    case Jewellery
    case Glasses
    case Cosmetics
    case Parts
    case Pets
    case Healthcare
        var image: UIImage {
            switch self {
            case .Shoe: return UIImage()
            case .Bag: return UIImage()
            case .Sport: return UIImage()
            case .Watch: return UIImage()
            case .Phone: return UIImage()
            case .Television: return UIImage()
            case .Food: return UIImage()
            case .Decoration: return UIImage()
            case .Activity: return UIImage()
            case .Jewellery: return UIImage()
            case .Glasses: return UIImage()
            case .Cosmetics: return UIImage()
            case .Parts: return UIImage()
            case .Pets: return UIImage()
            case .Healthcare: return UIImage()
            }
        }
}


