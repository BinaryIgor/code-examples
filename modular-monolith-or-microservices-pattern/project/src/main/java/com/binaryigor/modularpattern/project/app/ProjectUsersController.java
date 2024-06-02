package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.ProjectUsersSync;
import com.binaryigor.modularpattern.shared.NdJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@ProjectControllerTag
@RestController
@RequestMapping("/project-users")
public class ProjectUsersController {

    private final ProjectUsersSync sync;
    private final ObjectMapper objectMapper;

    public ProjectUsersController(ProjectUsersSync sync,
                                  ObjectMapper objectMapper) {
        this.sync = sync;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/sync-all")
    void syncAll(@RequestParam(name = "chunkSize", required = false, defaultValue = "1000") int chunkSize) {
        sync.syncAll(chunkSize);
    }

    @GetMapping
    ResponseEntity<StreamingResponseBody> allUsers() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_NDJSON)
            .body(out -> NdJson.writeTo(sync.allAvailable(), objectMapper, out));
    }
}
