package io.github.ijlijapol.bybit.bybit.model;

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