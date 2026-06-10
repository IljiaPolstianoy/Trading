package io.github.ilijapol.bybit.mapper;

import io.github.ilijapol.common.model.DirectionCandle;

@FunctionalInterface
public interface CreateDirectionCandle {
    DirectionCandle createDirectionCandle(String openPriceInput, String closePriceInput);
}
