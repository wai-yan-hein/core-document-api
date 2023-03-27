package core.document.api.service;

import core.document.api.entity.SeqTable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Transactional
@Service
public class SeqServiceImpl implements SeqService {
    private final R2dbcEntityTemplate template;

    public SeqServiceImpl(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Integer> getSequence(String period, String option) {
        SeqTable seq = new SeqTable();
        seq.setPeriod(period);
        seq.setOption(option);
        seq.setSeqNo(1);
        Query q = Query.query(Criteria.where("seq_period")
                .is(period).and("seq_option")
                .is(option));
        return template.select(SeqTable.class)
                .matching(q)
                .one()
                .flatMap(seqTable -> {
                    seqTable.setSeqNo(seqTable.getSeqNo() + 1);
                    return update(seqTable).map(SeqTable::getSeqNo);
                }).switchIfEmpty(save(seq).map(SeqTable::getSeqNo));
    }

    private Mono<SeqTable> save(SeqTable s) {
        return template.insert(s);
    }

    private Mono<SeqTable> update(SeqTable s) {
        return template.update(s);
    }
}
