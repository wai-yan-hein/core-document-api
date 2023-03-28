package core.document.api.service;

import core.document.api.entity.CVFile;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CVFileService {
    Mono<?> createFile(String createdBy, String parentId, String filePath, Mono<FilePart> files);

    Mono<?> createFolder(CVFile file);

    Mono<?> createFileHead(CVFile file);

    Mono<CVFile> save(CVFile file);

    Mono<CVFile> findById(String fileId);

    Flux<CVFile> getFileHead();
}
