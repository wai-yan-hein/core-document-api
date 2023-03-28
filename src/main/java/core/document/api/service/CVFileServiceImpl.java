package core.document.api.service;

import core.document.api.common.CVFileUtil;
import core.document.api.common.Util1;
import core.document.api.entity.CVFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@Slf4j
public class CVFileServiceImpl implements CVFileService {
    @Autowired
    private SeqService seqService;
    @Autowired
    private R2dbcEntityTemplate template;

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

    private Mono<File> saveFileToDisk(FilePart filePart, Path path) {
        String fileName = path.toString() + File.separator + filePart.filename();
        File file = new File(fileName);
        try {
            Files.createDirectories(file.getParentFile().toPath());
            filePart.transferTo(file).then();
        } catch (IOException e) {
            return Mono.error(e);
        }
        return Mono.just(file);
    }


    @Override
    public Mono<?> createFile(String createdBy, String parentId, String filePath, Mono<FilePart> files) {
        return files
                .flatMap(filePart -> {
                    File file = new File(Paths.get(filePath) + File.separator + filePart.filename());
                    try {
                        Files.createDirectories(file.getParentFile().toPath());
                        filePart.transferTo(file).subscribe();
                        String fileName = file.getName();
                        long fileSize = 10;
                        String contentType = determineContentType(filePath + File.separator + fileName);
                        CVFile c = new CVFile();
                        c.setParentId(parentId);
                        c.setFileName(fileName);
                        c.setDescription(fileName);
                        c.setFileSize(fileSize);
                        c.setFileType(CVFileUtil.FILE);
                        c.setFileContent(contentType);
                        c.setFileExtension(CVFileUtil.getFileExtension(filePart));
                        c.setCreatedBy(createdBy);
                        c.setCreatedDate(LocalDateTime.now());
                        c.setDeleted(false);
                        return save(c);
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }

    private static String determineContentType(String filename) {
        Path path = Paths.get(filename);
        String contentType = null;
        try {
            contentType = Files.probeContentType(path);
        } catch (Exception e) {
            // Unable to determine content type from file extension
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
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
