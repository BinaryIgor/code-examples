package com.binaryigor.apitests.infra;

import com.binaryigor.apitests.domain.Client;
import com.binaryigor.apitests.domain.ClientRepository;
import com.binaryigor.apitests.domain.ClientValidationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlClientRepository implements ClientRepository {

    private final ClientEntityRepository entityRepository;
    private final JdbcAggregateTemplate aggregateTemplate;

    public SqlClientRepository(ClientEntityRepository entityRepository,
                               JdbcAggregateTemplate aggregateTemplate) {
        this.entityRepository = entityRepository;
        this.aggregateTemplate = aggregateTemplate;
    }

    @Override
    public void create(Client client) {
        aggregateTemplate.insert(ClientEntity.fromClient(client));
    }

    @Override
    public void update(Client client) {
        try {
            aggregateTemplate.update(ClientEntity.fromClient(client));
        } catch (DbActionExecutionException e) {
            if (e.getCause() instanceof DuplicateKeyException) {
                throw new ClientValidationException("Given email is taken already");
            }
            throw e;
        }
    }

    @Override
    public Optional<Client> getById(UUID id) {
        return entityRepository.findById(id).map(ClientEntity::toClient);
    }
}
