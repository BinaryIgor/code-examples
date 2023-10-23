package com.binaryigor.restapitests.infra;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientEntityRepository extends CrudRepository<ClientEntity, UUID> {

}
