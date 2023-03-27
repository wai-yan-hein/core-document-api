package core.document.api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("seq_table")
public class SeqTable {
    @Id
    @Column("seq_option")
    private String option;
    @Column("seq_period")
    private String period;
    @Column("seq_no")
    private int seqNo;
}
