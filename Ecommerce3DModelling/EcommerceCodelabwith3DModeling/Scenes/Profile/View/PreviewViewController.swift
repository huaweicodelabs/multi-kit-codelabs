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


import SceneKit
import SnapKit
import UIKit

final class PreviewViewController: UIViewController {
    private var sceneView = SCNView()
    private var objURL: URL
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.prepareViews()
        self.configureViews()
    }
    
    init(objURL: URL) {
        self.objURL = objURL
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func prepareViews() {
        self.view.addSubview(self.sceneView)
        
        self.sceneView.snp.makeConstraints { make in
            make.top.bottom.trailing.leading.equalToSuperview()
        }
    }
    
    private func configureViews() {
        let scene = try! SCNScene(url: self.objURL)
        
        let cameraNode = SCNNode()
        cameraNode.camera = SCNCamera()
        cameraNode.position = SCNVector3(x: 0, y: 10, z: 35)
        scene.rootNode.addChildNode(cameraNode)

        let lightNode = SCNNode()
        lightNode.light = SCNLight()
        lightNode.light?.type = .omni
        lightNode.position = SCNVector3(x: 0, y: 10, z: 35)
        scene.rootNode.addChildNode(lightNode)

        let ambientLightNode = SCNNode()
        ambientLightNode.light = SCNLight()
        ambientLightNode.light?.type = .ambient
        ambientLightNode.light?.color = UIColor.white
        scene.rootNode.addChildNode(ambientLightNode)

        self.sceneView.allowsCameraControl = true
        self.sceneView.backgroundColor = UIColor.white
        self.sceneView.cameraControlConfiguration.allowsTranslation = false
        self.sceneView.scene = scene
    }
}
