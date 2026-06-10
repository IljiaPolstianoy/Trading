package io.github.ilijapol.service;

import io.github.ilijapol.common.model.MarketPatternDto;
import io.github.ilijapol.entity.MarketPattern;

public interface MarketPatternService {
    MarketPattern save(MarketPatternDto marketPatternDto);

    void delete(Integer id);
}
