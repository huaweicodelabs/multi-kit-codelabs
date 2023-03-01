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

struct FileOperations {
    static func getModelUrls(taskId: String) -> (URL, URL, URL) {
        var downloadModelURL: URL {
            get {
                return FileManager.default.urls(for: .libraryDirectory, in: .userDomainMask)[0].appendingPathComponent(GeneralConstants.ViewModelConstants.caches).appendingPathComponent(GeneralConstants.ViewModelConstants.dataTask).appendingPathComponent(taskId).appendingPathComponent(GeneralConstants.ViewModelConstants.downloadModel)
            }
        }
        let modelZipURL = downloadModelURL.appendingPathComponent(GeneralConstants.ViewModelConstants.modelZip)
        let modelURL = downloadModelURL.appendingPathComponent(GeneralConstants.ViewModelConstants.model)
        let mtlURL = modelURL.appendingPathComponent(GeneralConstants.ViewModelConstants.mtl)
        
        return (modelZipURL, modelURL, mtlURL)
    }
    
    static func deleteModelFile(modelURL: URL) {
        do {
            try FileManager.default.removeItem(atPath: modelURL.path)
            return
        }
        catch {
            assertionFailure("Failed deleting to URL: \(modelURL), Error: " + error.localizedDescription)
        }
    }
    
    static func deleteZipFile(modelZipURL: URL) {
        do {
            try FileManager.default.removeItem(atPath: modelZipURL.path)
            return
        }
        catch {
            assertionFailure("Failed deleting to URL: \(modelZipURL), Error: " + error.localizedDescription)
        }
    }
    
    static func deleteMtlFile(mtlURL: URL) {
        do {
            try FileManager.default.removeItem(atPath: mtlURL.path)
            return
        }
        catch {
            assertionFailure("Failed deleting to URL: \(mtlURL), Error: " + error.localizedDescription)
        }
    }

    static func rewriteMtlFile(mtlURL: URL) {
        let outString = GeneralConstants.ViewModelConstants.stringOfMtl
        do {
            try outString.write(to: mtlURL, atomically: true, encoding: .utf8)
        } catch {
            assertionFailure("Failed writing to URL: \(mtlURL), Error: " + error.localizedDescription)
        }
    }
    
    static func getObjFile(taskId: String) -> URL {
        var downloadModelURL: URL {
            get {
                return FileManager.default.urls(for: .libraryDirectory, in: .userDomainMask)[0].appendingPathComponent(GeneralConstants.ViewModelConstants.caches).appendingPathComponent(GeneralConstants.ViewModelConstants.dataTask).appendingPathComponent(taskId).appendingPathComponent(GeneralConstants.ViewModelConstants.downloadModel)
            }
        }
        let modelURL = downloadModelURL.appendingPathComponent(GeneralConstants.ViewModelConstants.model)
        let objURL = modelURL.appendingPathComponent(GeneralConstants.ViewModelConstants.obj)
        
        return objURL
    }
}
