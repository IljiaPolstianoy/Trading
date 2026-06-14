package io.github.ilijapol.model;

import io.github.ilijapol.common.model.CandleDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.TreeSet;

@Builder
@ToString
@Getter
public class PatternDto {
    private final TreeSet<CandleDTO> candleDirections;
}