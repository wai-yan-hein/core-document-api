package core.document.api.service;

import core.document.api.common.CVFileUtil;
import core.document.api.common.Util1;
import core.document.api.entity.CVFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@Slf4j
public class CVFileServiceImpl implements CVFileService {
    private final R2dbcEntityTemplate template;
    private final SeqService seqService;

    public CVFileServiceImpl(R2dbcEntityTemplate template, SeqService seqService) {
        this.template = template;
        this.seqService = seqService;
    }


    @Override
    public Mono<CVFile> save(CVFile f) {
        String option = "File";
        String period = Util1.toDateStr(Util1.getTodayDate(), "MMyy");
        return seqService.getSequence(period, option)
                .flatMap(integer -> {
                    String fileId = period.concat("-") + String.format("%010d", integer);
                    log.info(fileId);
                    f.setFileId(fileId);
                    return template.insert(f).single();
                });
    }

    @Override
    public Mono<CVFile> findById(String fileId) {
        Query q = Query.query(Criteria.where("file_id").is(fileId));
        return template.select(CVFile.class).matching(q).one();
    }

    @Override
    public Flux<CVFile> getFileHead() {
        Query q = Query.query(Criteria.where("file_parent_id").is("#").and("deleted").isFalse());
        return template.select(q, CVFile.class);
    }

    private Mono<Void> saveFileToDisk(FilePart filePart, Path path) {
        return filePart.transferTo(path)
                .doOnSuccess(result -> log.info("file saved successfully"))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<String> createFile(String createdBy, String parentId, String filePath, Mono<FilePart> files) {
        if (CVFileUtil.isEmpty(filePath)) {
            return files.flatMap(file -> {
                String fileName = file.filename();
                long fileSize = file.headers().getContentLength();
                String contentType = Objects.requireNonNull(file.headers().getContentType()).toString();
                Path path = Paths.get(filePath);
                CVFile c = new CVFile();
                c.setParentId(parentId);
                c.setFileName(fileName);
                c.setDescription(fileName);
                c.setFileSize(fileSize);
                c.setFileType(CVFileUtil.FILE);
                c.setFileContent(contentType);
                c.setFileExtension(CVFileUtil.getFileExtension(file));
                c.setCreatedBy(createdBy);
                c.setCreatedDate(LocalDateTime.now());
                c.setDeleted(false);
                return saveFileToDisk(file, path).then(save(c)).flatMap(file1 -> Mono.just("success"));
            });
        }
        return Mono.just("File already exists.");
    }

    @Override
    public Mono<?> createFolder(CVFile file) {
        file.setFileType(CVFileUtil.FOLDER);
        String filePath = file.getFilePath();
        String path = CVFileUtil.ROOT + File.separator + filePath + File.separator;
        if (CVFileUtil.isEmpty(path)) {
            return save(file).flatMap((f) -> {
                boolean success = CVFileUtil.createFolderOS(path);
                if (success) {
                    return Mono.just(f);
                }
                return Mono.just("Something went wrong.");
            });
        }
        return Mono.just("Folder already exists.");
    }


    @Override
    public Mono<?> createFileHead(CVFile f) {
        f.setParentId("#");
        f.setFileType(CVFileUtil.FOLDER);
        boolean success = CVFileUtil.createFolderOS(CVFileUtil.ROOT + File.separator + f.getFileName());
        if (success) {
            return save(f);
        }
        return Mono.just("File Already Exists.");
    }

}
