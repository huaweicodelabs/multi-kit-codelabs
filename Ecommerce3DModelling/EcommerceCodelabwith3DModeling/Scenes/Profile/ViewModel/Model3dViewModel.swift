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


import Foundation
import UIKit
import Modeling3dKit
import ZIPFoundation

typealias VoidHandler = () -> Void

protocol Model3dDelegate: AnyObject {
}

class Model3dViewModel {
    weak var delegate: Model3dDelegate?
    var model = [Modeling3dReconstructTaskModel]()
    var currentCellIndex: Int?
    var actionTableReload: VoidHandler?
    var clearImageSet: VoidHandler?
    
    func deleteForUpload(taskId: String){
        Modeling3dReconstructTask.sharedManager().deleteTask(withTaskId: taskId) {
            print("deleteForUpload success...")
        } failureHandler: { retCode, retMsg in
            print("deleteForUpload retCode:", retCode)
            print("deleteForUpload retMsg:", retMsg)
        }
    }
    
    func delete() {
        Modeling3dReconstructTask.sharedManager().deleteTask(withTaskId: self.model[currentCellIndex!].taskId) {
            self.deleteModelFiles(taskId: self.model[self.currentCellIndex!].taskId)
            self.model.remove(at: self.currentCellIndex!)
            self.actionTableReload?()
            print("Delete success...")
        } failureHandler: { retCode, retMsg in
            print("delete retCode:", retCode)
            print("delete retMsg:", retMsg)
        }
    }
    
    func deleteModelFiles(taskId: String) {
        let modelURLs = FileOperations.getModelUrls(taskId: self.model[self.currentCellIndex!].taskId)
        
        let modelZipURL = modelURLs.0
        let modelURL = modelURLs.1
        
        if !FileManager.default.fileExists(atPath: modelZipURL.path) {
            print("there is no zip file.")
            return
        } else {
            FileOperations.deleteZipFile(modelZipURL: modelZipURL)
            if !FileManager.default.fileExists(atPath: modelURL.path) {
                FileOperations.deleteModelFile(modelURL: modelURL)
            }
        }
    }
    
    func upload(imageSet: [UIImage], viewController: UIViewController) {
        Modeling3dReconstructTask.sharedManager().initTask(with: .pictureModel) { taskModel in
            Modeling3dReconstructTask.sharedManager().createLocationUrl(with: taskModel)
            Modeling3dReconstructTask.sharedManager().queryRestriction(withTaskId: taskModel.taskId) { restrictFlag in
                if (restrictFlag.rawValue != 1) {
                    let alert = ProgressAlertView(on: viewController, alertTitle: "viewModel.upload".localize, isUpload: true, taskId: taskModel.taskId)
                    alert.deleteActionForUpload = {
                        self.deleteForUpload(taskId: taskModel.taskId)
                    }
                    
                    Modeling3dReconstructTask.sharedManager().uploadTask(with: taskModel, imageAssets: imageSet) { progress in
                        print("uploadTask progress:", progress)
                    } successHandler: {
                        print("uploadTask success")
                        taskModel.taskStatus = 1
                        taskModel.save()
                        
                        self.model.append(taskModel)
                        self.actionTableReload?()
                        alert.removeAlertView()
                    } progressHandler: { progressValue in
                        print("uploadTask progressValue:", progressValue)
                        alert.updateProgress(progressValue: progressValue)
                    } failureHandler: { retCode, retMsg in
                        print("uploadTask retCode:", retCode)
                        print("uploadTask retMsg:", retMsg)
                    }
                } else {
                    print("The task is restricted, operate before cancel restrict please")
                }
            } failureHandler: { retCode, retMsg in
                print("queryRestriction retCode:", retCode)
                print("queryRestriction retMsg:", retMsg)
            }
        } failureHandler: { retCode, retMsg in
            print("initTask retCode:", retCode)
            print("initTask retMsg:", retMsg)
        }
        
        self.clearImageSet?()
    }

    func query(viewController: UIViewController) {
        let alert = ActivityIndicatorAlertView(on: viewController, alertTitle: "viewModel.query".localize)
        
        let timer = Timer.scheduledTimer(withTimeInterval: 20.0, repeats: true) { timer in
            Modeling3dReconstructTask.sharedManager().queryTask(withTaskId: self.model[self.currentCellIndex!].taskId) { taskStatus, errorMsg in
                if self.model[self.currentCellIndex!].taskStatus == 0 {
                    print("model taskStatus: 0")
                    timer.invalidate()
                }
                if self.model[self.currentCellIndex!].taskStatus != taskStatus.rawValue {
                    print("model taskStatus:", self.model[self.currentCellIndex!].taskStatus)
                    print("return taskStatus: ", taskStatus.rawValue)
                    
                    self.model[self.currentCellIndex!].taskStatus = Int(taskStatus.rawValue)
                    self.model[self.currentCellIndex!].save()
                }
                if taskStatus.rawValue >= 3 {
                    alert.removeAlertView()
                    timer.invalidate()
                }
                print("query is ongoing...")
            } failureHandler: { retCode, retMsg in
                print("queryTask retCode:", retCode)
                print("queryTask retMsg:", retMsg)
            }
        }
        alert.cancelAction = {
            timer.invalidate()
        }
    }
    
    func download(viewController: UIViewController) {
        Modeling3dReconstructTask.sharedManager().queryRestriction(withTaskId: self.self.model[self.currentCellIndex!].taskId) { restrictFlag in
            if (restrictFlag.rawValue != 1) {
                let alert = ProgressAlertView(on: viewController, alertTitle: "viewModel.download".localize, isUpload: false, taskId: self.model[self.currentCellIndex!].taskId)
                
                Modeling3dReconstructTask.sharedManager().downloadTask(withTaskId: self.model[self.currentCellIndex!].taskId, downloadFormat: Modeling3dKit.TaskDownloadFormat.OBJ) {
                    print("downloadTask success...")
                    alert.removeAlertView()
                } progressHandler: { progressValue in
                    print("downloadTask progressValue:", progressValue)
                    alert.updateProgress(progressValue: progressValue)
                } failureHandler: { retCode, retMsg in
                    print("downloadTask retCode:", retCode)
                    print("downloadTask retMsg:", retMsg)
                }
            } else {
                print("The task is restricted, operate before cancel restrict please")
            }
        } failureHandler: { retCode, retMsg in
            print("queryRestriction retCode:", retCode)
            print("queryRestriction retMsg:", retMsg)
        }
    }
    
    func preview(viewController: UIViewController) {
        let modelURLs = FileOperations.getModelUrls(taskId: self.model[self.currentCellIndex!].taskId)
        
        let modelZipURL = modelURLs.0
        
        if !FileManager.default.fileExists(atPath: modelZipURL.path) {
//            let alert = UIAlertController(title: "alert.downloadError".localize, message: "alert.notDownloaded".localize, preferredStyle: .alert)
//           * alert.addAction(UIAlertAction(title: "alert.click".localize, style: .default, handler: nil))
//            viewController.present(alert, animated: true, completion: nil)
            print("there is no zip file.")
            return
        } else {
            let modelURL = modelURLs.1
            let mtlURL = modelURLs.2
            
            if !FileManager.default.fileExists(atPath: modelURL.path) {
                do {
                    try FileManager().createDirectory(at: modelURL, withIntermediateDirectories: true, attributes: nil)
                    try FileManager().unzipItem(at: modelZipURL, to: modelURL)
                    print("unzip done")
                    
                    FileOperations.deleteMtlFile(mtlURL: mtlURL)
                    FileOperations.rewriteMtlFile(mtlURL: mtlURL)
                } catch {
//                    let alert = UIAlertController(title: "alert.error".localize, message: "alert.couldNotShown".localize, preferredStyle: UIAlertController.Style.alert)
//                    alert.addAction(UIAlertAction(title: "alert.click".localize, style: UIAlertAction.Style.default, handler: nil))
//                    viewController.present(alert, animated: true, completion: nil)
                    assertionFailure("Failed unziping to URL: \(modelZipURL), Error: " + error.localizedDescription)
                }
            }
        }
    }
    
    func getCurrentProduct(completion: @escaping (_ product: product3?, _ errorMessage: String?) -> ()) {
        CloudDBPersistance.queryAllProductsWithUserID { data, error in
            if let error = error {
                print(error.localizedDescription)
                completion(nil, error.localizedDescription)
                return
            }
            
            completion(data.last, nil)
        }
    }
}
