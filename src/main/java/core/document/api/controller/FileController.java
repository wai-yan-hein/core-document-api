package core.document.api.controller;

import core.document.api.common.ReturnObject;
import core.document.api.common.Util1;
import core.document.api.entity.CVFile;
import core.document.api.service.CVFileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
public class FileController {

    @Autowired
    private CVFileService cvFileService;

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
    public Mono<?> createFile(@RequestParam String createdBy,
                              @RequestParam String parentId,
                              @RequestParam String filePath,
                              @RequestPart Mono<FilePart> parts) {
        return cvFileService.createFile(createdBy, parentId, filePath, parts);
    }

    @PostMapping(value = "/createFileLocal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<?> createFileLocal(@RequestPart FilePart parts) {
        return saveFile(parts,"data");
    }
    public Mono<File> saveFile(FilePart filePart, String filePath) {
        File file = new File(Paths.get(filePath) + File.separator + filePart.filename());
        try {
            Path directory = Paths.get(filePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            filePart.transferTo(file).subscribe();
            return Mono.just(file);
        } catch (IOException e) {
            return Mono.error(e);
        }
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
