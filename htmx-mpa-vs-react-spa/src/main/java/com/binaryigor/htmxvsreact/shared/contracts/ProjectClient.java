package com.binaryigor.htmxvsreact.shared.contracts;

import java.util.Collection;
import java.util.UUID;

public interface ProjectClient {

    UUID idOfName(String name);

    Collection<UUID> idsOfNames(Collection<String> names);

    Collection<ProjectView> allOfOwner(UUID ownerId);

    ProjectView ofId(UUID id);
}
