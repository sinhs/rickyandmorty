package com.sid.rickmorty.repository;

import com.sid.rickmorty.model.entity.LocationEntity;
import org.springframework.data.repository.CrudRepository;

public interface LocationRepository extends CrudRepository<LocationEntity, Integer> {
}
