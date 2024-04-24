package com.example.filtergray.repository;

import com.example.filtergray.model.entity.Processed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedRepository extends CrudRepository<Processed, Long> {

    Optional<Processed> findByOriginalAndRequest(String original, String request);

}
