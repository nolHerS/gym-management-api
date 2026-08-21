package com.imanol.gym.common.service;

import com.imanol.gym.common.entity.BaseEntity;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.common.repository.BaseRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseServiceImpl<
        T extends BaseEntity,
        ID
        > implements BaseService<T, ID> {

    private final BaseRepository<T, ID> repository;

    @Override
    public T create(T entity) {
        return repository.save(entity);
    }

    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );
    }
}