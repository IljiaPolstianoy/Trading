package io.github.ilijapol.service;

import io.github.ilijapol.common.model.MarketPatternDto;
import io.github.ilijapol.entity.MarketPattern;

import java.util.List;

public interface MarketPatternService {

    List<MarketPattern> findAll();

    MarketPattern save(MarketPatternDto marketPatternDto);

    void delete(Integer id);
}
