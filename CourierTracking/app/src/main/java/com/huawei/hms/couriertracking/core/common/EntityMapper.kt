package com.huawei.hms.couriertracking.core.common

interface EntityMapper<E, M> {
    fun toEntity(model: M): E
    fun fromEntity(entity: E): M
}