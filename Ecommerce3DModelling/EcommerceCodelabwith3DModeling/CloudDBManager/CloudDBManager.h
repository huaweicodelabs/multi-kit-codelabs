/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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

#import <Foundation/Foundation.h>

#import "user3.h"
#import "product3.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM (NSInteger, CloudDBManagerSortType) {
    CloudDBManagerSortTypeAsc,
    CloudDBManagerSortTypeDesc
};

@interface CloudDBManager : NSObject

/// Login state
@property (nonatomic, readonly, assign) BOOL isLogin;

+ (instancetype)shareInsatnce;


- (void)prepareForCloudDB:(void (^)(BOOL success, NSError *error))complete;

/**
 Init AGConnectCloudDB if it has not been initialized.
 If init default failed, you should to check whether executing illegal operation,
 such as downgrade the system version.
 
 * @param complete  A callback for prepare. YES succeeds, NO fails
 */
- (void)prepareForCloudDB:(void (^)(BOOL success, NSError *error))complete;

/**
 Log in AGCCloudDatabase
 
 * @param complete  A callback for login. YES succeeds, NO fails
 */
- (void)loginAGCWithComplete:(void (^)(BOOL success, NSError *error))complete;


/**
 Log out AGCCloudDatabase
 
 * @param complete Log out of the logon callback. YES succeeds, NO fails
 */
- (void)logoutAGCWithComplete:(void (^)(BOOL success))complete;

/**
 Add AGC listener when data updae
 
 * @param complete  block of new data
 */
- (void)subscribeSnapshotComplete:(void (^)(NSArray *bookList, NSError *error))complete;

/**
 Add book message by AGC mehod "executeUpsert"
 
 * @param dataModel  A data model for books
 */
- (void)executeUpsertWithUser:(user3 *__nonnull)user2 userId:(NSInteger)userId complete:(void (^)(BOOL success, NSError *error))complete;

/**
 Add book message by AGC mehod "executeUpsert"
 
 * @param dataModel  A data model for books
 */
- (void)executeUpsertWithProduct:(product3 *__nonnull)product productId:(NSInteger)productID complete:(void (^)(BOOL success, NSError *error))complete;

/**
 Update book message by AGC mehod "executeUpsert"
 
 * @param book  A data model for books
 */
- (void)executeUpdateWithUser:(user3 *__nonnull)user complete:(void (^)(BOOL success, NSError *error))complete;

/**
 Update book message by AGC mehod "executeUpsert"
 
 * @param book  A data model for books
 */
- (void)executeUpdateWithProduct:(product3 *__nonnull)product3 complete:(void (^)(BOOL success, NSError *error))complete;


/**
 Delete the book by its title by AGC mehod "executeDelete"
 
 * @param bookID Book id
 * @warning Advance queries are performed before deletion is performed
 */
- (void)deleteAGCDataWithUserID:(NSInteger)userID
                       complete:(void (^)(BOOL success, NSError *error))complete;

/**
Delete the book by its title by AGC mehod "executeDelete"

* @param bookID Book id
* @warning Advance queries are performed before deletion is performed
*/
- (void)deleteAGCDataWithProductID:(NSInteger)productID
                      complete:(void (^)(BOOL success, NSError *error))complete;

/**
 Query data with table name .use AGC method "executeQuery".
 
 * @param results query results
 */
- (void)queryAllBooksWithResults:(void (^)(NSArray *userList, NSError *error))results;

/**
 Fuzzy query book by book model.  pass nil, means query all. Add query criteria through AGCCloudDBQuery
 
 * @param bookInfo book info
 * @param results query results
 */
//- (void)fuzzyQueryAGCDataWithBookInfo:(nonnull BMQueryBookDataModel *)bookInfo
//                              results:(void (^)(NSArray *bookList, NSError *error))results;

/**
 Query book by fieldName and sortType. Add query criteria through AGCCloudDBQuery
 Use AGC method "executeQuery"
 
 * @param fieldName BookInfo key
 * @param sortType desc or asc
 * @param results query results
 */
- (void)queryAGCProductDataWithFieldName:(NSString *)fieldName
                                   value:(NSInteger)value
                                 sortType:(CloudDBManagerSortType)sortType
                                  results:(void (^)(NSArray *bookList, NSError *error))results;

@end

NS_ASSUME_NONNULL_END
