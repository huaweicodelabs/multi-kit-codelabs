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

#import "CloudDBManager.h"

#import <AGConnectDatabase/AGConnectDatabase.h>
#import <AGConnectAuth/AGCAuth.h>

#import "AGCCloudDBObjectTypeHeaders.h"

@interface CloudDBManager ()

@property (nonatomic, strong) AGConnectCloudDB *agcConnectCloudDB;

@property (nonatomic, strong) AGCCloudDBZone *dbZone;

@property (nonatomic, assign) BOOL loginState;

@end

@implementation CloudDBManager

static CloudDBManager *_shareInsatnce = nil;

+ (instancetype)shareInsatnce {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _shareInsatnce = [[CloudDBManager alloc] init];
    });
    return _shareInsatnce;
}

#pragma mark - setter && getter
- (BOOL)isLogin {
    return self.loginState;
}

#pragma mark - login serve
- (void)prepareForCloudDB:(void (^)(BOOL success, NSError *error))complete {
    //Step 1: Initializes cloud db
    //This step simply initializes the local database
    NSError *error = nil;
    [AGConnectCloudDB initEnvironment:&error];
    if (error) {
        NSLog(@"init cloud db failed with reson：%@", error.description);
        if (complete) {
            complete(NO, error);
        }
        return;
    }

    NSLog(@"init cloud db suceess");
    self.agcConnectCloudDB = [AGConnectCloudDB shareInstance];

    // Step 2:create database object
    // After successful initialization cloud db, build the database
    NSError *createError = nil;
    [self.agcConnectCloudDB createObjectType:[AGCCloudDBObjectTypeInfoHelper obtainObjectTypeInfo]
                                       error:&createError];
    if (createError) {
        NSLog(@"created cloud database Object failed with reson:%@", createError);
    }

    // Step 3: Set the caching policy and Synchronization strategies.
    NSString *zoneName = @"ECOM3DCODL";

    AGCCloudDBZoneConfig *zoneConfig =
    [[AGCCloudDBZoneConfig alloc] initWithZoneName:zoneName
                                          syncMode:AGCCloudDBZoneSyncModeCloudCache
                                        accessMode:AGCCloudDBZoneAccessModePublic];
    zoneConfig.persistence = NO;

    // Step 4: Open cloud db zone.
    __weak typeof(self) weakSelf = self;
    [self.agcConnectCloudDB openCloudDBZone2:zoneConfig
                                 allowCreate:YES
                                    callback:^(AGCCloudDBZone * _Nullable zone, NSError * _Nullable error) {
        if (error) {
            NSLog(@"created cloud database zone failed with reson:%@", error);
            if (complete) {
                complete(NO, error);
            }
        } else {
            weakSelf.dbZone = zone;
            if (complete) {
                complete(YES, nil);
            }
        }
    }];
}


- (void)logoutAGCWithComplete:(void (^)(BOOL success))complete {
    [[AGCAuth getInstance] signOut];
    if (complete) {
        complete(YES);
    }
}

#pragma mark - Cloud Database Method

#pragma mark - add AGC Listener
- (void)subscribeSnapshotComplete:(void (^)(NSArray *bookList, NSError *error))complete {
    AGCCloudDBQuery *query = [AGCCloudDBQuery where:[user3 class]];
    [query equalTo:@YES forField:@"shadowFlag"];
    
    [self.dbZone subscribeSnapshotWithQuery:query
                                     policy:AGCCloudDBQueryPolicyCloud
                                   listener:^(AGCCloudDBSnapshot *_Nullable snapshot, NSError *_Nullable error) {
        if (snapshot != nil) {
            NSArray *bookList = snapshot.snapshotObjects;
            if (complete) {
                complete(bookList, nil);
            }
        } else {
            if (complete) {
                complete(nil, error);
            }
        }
    }];
}

#pragma mark - add data user
- (void)executeUpsertWithUser:(user3 *__nonnull)user3 userId:(NSInteger)userId complete:(void (^)(BOOL success, NSError *error))complete {
    if (user3 == nil) {
        return;
    }
    
    if (user3.username.length == 0) {
        return;
    }
    
    user3.id = @(userId);
    
    // insert data
    [self.dbZone executeUpsertOne:user3
                      onCompleted:^(NSInteger count, NSError *_Nullable error) {
        if (error) {
            if (complete) {
                complete(NO, error);
            }
        } else {
            if (complete) {
                complete(YES, nil);
            }
        }
    }];
}

#pragma mark - add data product
- (void)executeUpsertWithProduct:(product3 *)product3 productId:(NSInteger)productId complete:(void (^)(BOOL, NSError * _Nonnull))complete {
    if (product3 == nil) {
        return;
    }
    
    if (product3.name.length == 0) {
        return;
    }
    
    product3.id = @(productId);
    
    // insert data
    [self.dbZone executeUpsertOne:product3
                      onCompleted:^(NSInteger count, NSError *_Nullable error) {
        if (error) {
            if (complete) {
                complete(NO, error);
            }
        } else {
            if (complete) {
                complete(YES, nil);
            }
        }
    }];
}

#pragma mark - updata data
- (void)executeUpdateWithUser:(user3 *__nonnull)user3
                     complete:(void (^)(BOOL success, NSError *error))complete {
    if (user3 == nil) {
        return;
    }
    
    if (user3.username.length == 0) {
        return;
    }
    
    // update data
    [self.dbZone executeUpsertOne:user3
                      onCompleted:^(NSInteger count, NSError *_Nullable error) {
        if (error) {
            if (complete) {
                complete(NO, error);
            }
        } else {
            if (complete) {
                complete(YES, nil);
            }
        }
    }];
}

#pragma mark - updata data
- (void)executeUpdateWithProduct:(product3 *__nonnull)product3
                     complete:(void (^)(BOOL success, NSError *error))complete {
    if (product3 == nil) {
        return;
    }
    
    if (product3.name.length == 0) {
        return;
    }
    
    // update data
    [self.dbZone executeUpsertOne:product3
                      onCompleted:^(NSInteger count, NSError *_Nullable error) {
        if (error) {
            if (complete) {
                complete(NO, error);
            }
        } else {
            if (complete) {
                complete(YES, nil);
            }
        }
    }];
}

#pragma mark - delete data
- (void)deleteAGCDataWithUserID:(NSInteger)userID
                       complete:(void (^)(BOOL success, NSError *error))complete;
{
    AGCCloudDBQuery *query = [AGCCloudDBQuery where:[user3 class]];
    [query equalTo:@(userID) forField:@"id"];
    __weak typeof(self) weakSelf = self;
    [self.dbZone executeQuery:query
                       policy:AGCCloudDBQueryPolicyCloud
                  onCompleted:^(AGCCloudDBSnapshot *_Nullable snapshot, NSError *_Nullable error) {
        if (snapshot != nil) {
            [weakSelf.dbZone executeDelete:snapshot.snapshotObjects
                               onCompleted:^(NSInteger count, NSError *_Nullable error) {
                if (error) {
                    if (complete) {
                        complete(NO, error);
                    }
                } else {
                    if (complete) {
                        complete(YES, nil);
                    }
                }
            }];
        }
    }];
}

#pragma mark - delete data
- (void)deleteAGCDataWithProductID:(NSInteger)productID
                       complete:(void (^)(BOOL success, NSError *error))complete;
{
    AGCCloudDBQuery *query = [AGCCloudDBQuery where:[product3 class]];
    [query equalTo:@(productID) forField:@"id"];
    __weak typeof(self) weakSelf = self;
    [self.dbZone executeQuery:query
                       policy:AGCCloudDBQueryPolicyCloud
                  onCompleted:^(AGCCloudDBSnapshot *_Nullable snapshot, NSError *_Nullable error) {
        if (snapshot != nil) {
            [weakSelf.dbZone executeDelete:snapshot.snapshotObjects
                               onCompleted:^(NSInteger count, NSError *_Nullable error) {
                if (error) {
                    if (complete) {
                        complete(NO, error);
                    }
                } else {
                    if (complete) {
                        complete(YES, nil);
                    }
                }
            }];
        }
    }];
}

#pragma mark - query table all data
- (void)queryAllUsersWithUserResults:(void (^)(NSArray *userList, NSError *error))results {
    AGCCloudDBQuery *query = [AGCCloudDBQuery where:[user3 class]];
    
    [self.dbZone executeQuery:query
                       policy:AGCCloudDBQueryPolicyCloud
                  onCompleted:^(AGCCloudDBSnapshot *_Nullable snapshot, NSError *_Nullable error) {
        if (error) {
            NSLog(@"query user list failed with reson : %@", error);
            if (results) {
                results(nil, error);
            }
        } else {
            NSArray *userList = snapshot.snapshotObjects;
            if (results) {
                results(userList, nil);
            }
        }
    }];
}

#pragma mark - query order by ASC or DESC
- (void)queryAGCProductDataWithFieldName:(NSString *)fieldName
                           value:(NSInteger)value
                         sortType:(CloudDBManagerSortType)sortType
                          results:(void (^)(NSArray *productList, NSError *error))results {
    AGCCloudDBQuery *query = [AGCCloudDBQuery where:[product3 class]];
    
    if (sortType == CloudDBManagerSortTypeAsc) {
        [query orderByAsc:fieldName];
    } else {
        [query orderByDesc:fieldName];
    }
    
    [query equalTo:@(value) forField:fieldName];
    
    [self.dbZone executeQuery:query
                       policy:AGCCloudDBQueryPolicyCloud
                  onCompleted:^(AGCCloudDBSnapshot *_Nullable snapshot, NSError *_Nullable error) {
        if (error) {
            NSLog(@"query product list error : %@", error);
            if (results) {
                results(nil, error);
            }
        } else {
            NSArray *productList = snapshot.snapshotObjects;
            if (results) {
                results(productList, nil);
            }
        }
    }];
}

- (NSString *)getCurrentTimestamp {
    NSDate *date = [NSDate dateWithTimeIntervalSinceNow:0];
    NSTimeInterval time = [date timeIntervalSince1970] * 1000;
    NSString *timeString = [NSString stringWithFormat:@"%.0f", time];
    return timeString;
}

@end
