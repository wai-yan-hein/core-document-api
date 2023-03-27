package core.document.api.controller;

import core.document.api.common.ReturnObject;
import core.document.api.common.Util1;
import core.document.api.entity.CVFile;
import core.document.api.service.CVFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
public class FileController {

    private final CVFileService cvFileService;

    public FileController(CVFileService cvFileService) {
        this.cvFileService = cvFileService;
    }


    @GetMapping(path = "/getFileHead")
    public Flux<?> getFileHead() {
        return cvFileService.getFileHead();
    }

    @PostMapping(path = "/createFileHead")
    public Mono<?> createFileHead(@RequestBody CVFile file) {
        ReturnObject ro = new ReturnObject();
        if (isValidFileHead(file, ro)) {
            return cvFileService.createFileHead(file);
        }
        return Mono.just(ro);
    }

    @PostMapping(path = "/createFolder")
    public Mono<?> createFolder(@RequestBody CVFile file) {
        ReturnObject ro = new ReturnObject();
        if (isValidFolder(file, ro)) {
            return cvFileService.createFolder(file);
        }
        return Mono.just(ro);
    }


    @PostMapping(value = "/createFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<?> createFile(@RequestParam String createdBy,
                              @RequestParam String parentId,
                              @RequestParam String filePath,
                              @RequestPart Mono<FilePart> parts) {
        return cvFileService.createFile(createdBy, parentId,filePath, parts);
    }

    private boolean isValidFileHead(CVFile f, ReturnObject ro) {
        if (Util1.isNullOrEmpty(f.getFileName())) {
            ro.setMessage("Invalid File Name.");
            return false;
        } else if (Util1.isNullOrEmpty(f.getDescription())) {
            ro.setMessage("Invalid Description.");
            return false;
        } else if (Util1.isNullOrEmpty(f.getCreatedBy())) {
            ro.setMessage("Invalid Created By.");
            return false;
        }
        return true;
    }

    private boolean isValidFolder(CVFile f, ReturnObject ro) {
        if (Util1.isNullOrEmpty(f.getFileName())) {
            ro.setMessage("Invalid File Name.");
            return false;
        } else if (Util1.isNullOrEmpty(f.getDescription())) {
            ro.setMessage("Invalid Description.");
            return false;
        } else if (Util1.isNullOrEmpty(f.getParentId())) {
            ro.setMessage("Invalid Parent Id.");
            return false;
        } else if (Util1.isNullOrEmpty(f.getFilePath())) {
            ro.setMessage("Invalid File Path.");
            return false;
        }
        return true;
    }


    @GetMapping(value = "/getFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<?> getFile(@RequestParam String filePath) {
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            return Mono.just(e.getMessage());
        }
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        return Mono.just(resource);
    }
}
