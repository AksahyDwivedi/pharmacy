package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Purchases;
import com.hm.pharmacy.repository.PurchasesRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Purchases} entity.
 */
public interface PurchasesSearchRepository extends ElasticsearchRepository<Purchases, Long>, PurchasesSearchRepositoryInternal {}

interface PurchasesSearchRepositoryInternal {
    Stream<Purchases> search(String query);

    Stream<Purchases> search(Query query);

    @Async
    void index(Purchases entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PurchasesSearchRepositoryInternalImpl implements PurchasesSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PurchasesRepository repository;

    PurchasesSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PurchasesRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Purchases> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Purchases> search(Query query) {
        return elasticsearchTemplate.search(query, Purchases.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Purchases entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Purchases.class);
    }
}
