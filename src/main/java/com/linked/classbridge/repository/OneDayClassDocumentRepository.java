package com.linked.classbridge.repository;

import com.linked.classbridge.domain.document.OneDayClassDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OneDayClassDocumentRepository extends ElasticsearchRepository<OneDayClassDocument, Long> {

}
