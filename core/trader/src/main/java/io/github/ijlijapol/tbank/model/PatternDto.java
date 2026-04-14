package io.github.ijlijapol.tbank.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@ToString
@Getter
public class PatternDto {
    private final List<Boolean> candleDirections;
}