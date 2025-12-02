package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Medicines;
import com.hm.pharmacy.repository.MedicinesRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Medicines} entity.
 */
public interface MedicinesSearchRepository extends ElasticsearchRepository<Medicines, Long>, MedicinesSearchRepositoryInternal {}

interface MedicinesSearchRepositoryInternal {
    Stream<Medicines> search(String query);

    Stream<Medicines> search(Query query);

    @Async
    void index(Medicines entity);

    @Async
    void deleteFromIndexById(Long id);
}

class MedicinesSearchRepositoryInternalImpl implements MedicinesSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MedicinesRepository repository;

    MedicinesSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, MedicinesRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Medicines> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Medicines> search(Query query) {
        return elasticsearchTemplate.search(query, Medicines.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Medicines entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Medicines.class);
    }
}
