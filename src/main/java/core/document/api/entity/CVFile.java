package core.document.api.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("files")
public class CVFile {
    //file_id, file_name, file_path, file_size, file_description, file_parent_id, file_type, created_by, created_date, updated_by, updated_date, deleted
    @Id
    @Column("file_id")
    private String fileId;
    @Column("file_name")
    private String fileName;
    @Column("file_size")
    private Long fileSize;
    @Column("file_description")
    private String description;
    @Column("file_parent_id")
    private String parentId;
    @Column("file_type")
    private Integer fileType;
    @Column("file_content")
    private String fileContent;
    @Column("file_extension")
    private String fileExtension;
    @Column("file_path")
    private String filePath;
    @Column("file_link")
    private String fileLink;
    @Column("created_by")
    private String createdBy;
    @Column("created_date")
    private LocalDateTime createdDate;
    @Column("updated_by")
    private String updatedBy;
    @Column("updated_date")
    private LocalDateTime updatedDate;
    @Column("deleted")
    private boolean deleted;
}
