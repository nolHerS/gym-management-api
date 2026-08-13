package com.imanol.gym.common.service;

import com.imanol.gym.common.entity.BaseEntity;

public interface BaseService<T extends BaseEntity, ID> {

    T create(T entity);

    T findById(ID id);
}