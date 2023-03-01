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

class HomePageCollectionViewCell: UICollectionViewCell {

    
    private let homePageMenuImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage()
        imageView.contentMode = .scaleAspectFit
        imageView.clipsToBounds = true
        return imageView
    }()
    
    private let homePageMenuLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = UIColor.black
        label.font = UIFont.appMainBold(fontSize: 18)
        label.layer.masksToBounds = true
        label.layer.cornerRadius = 5
        label.layer.opacity = 0.5
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.backgroundColor = UIColor.white
        contentView.layer.cornerRadius = 15.0
        contentView.addSubview(homePageMenuLabel)
        contentView.addSubview(homePageMenuImageView)
        contentView.clipsToBounds = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        homePageMenuLabel.frame = CGRect(x: 0, y: contentView.frame.size.height-50, width: contentView.frame.size.width, height: 50)
        homePageMenuImageView.frame = CGRect(x: 30, y: 75, width: contentView.frame.size.width-60, height: contentView.frame.size.height-180)
        
    }
    
    public func configure(label: String, image: String) {
        homePageMenuLabel.text = label
        homePageMenuImageView.image = UIImage(named: image)
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        homePageMenuLabel.text = nil
    }
}
