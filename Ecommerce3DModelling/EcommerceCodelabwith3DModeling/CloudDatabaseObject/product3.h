/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */

#import <AGConnectDatabase/AGConnectDatabase.h>

NS_ASSUME_NONNULL_BEGIN

@interface product3 : AGCCloudDBObject
@property (nonatomic, strong) NSNumber<AGCLong> *id;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *category;
@property (nonatomic, strong) NSNumber<AGCFloat> *price;
@property (nonatomic, copy) NSString *image;
@property (nonatomic, strong) NSNumber<AGCLong> *userid;
@end

NS_ASSUME_NONNULL_END