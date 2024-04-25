package com.example.imageapi.repository;

import com.example.imageapi.model.entity.FilterRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilterRequestRepository extends CrudRepository<FilterRequest, Long> {

    Optional<FilterRequest> findByRequestId(String requestId);

    boolean existsByRequestId(String requestId);

}
