package io.github.ilijapol.entity;

import io.github.ilijapol.common.model.DirectionCandle;
import io.github.ilijapol.common.model.TimeFrame;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "candle")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Candle implements Comparable<Candle> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "time_frame", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType((PostgreSQLEnumJdbcType.class))
    private TimeFrame timeFrame;

    @Column(name = "max_price", nullable = false)
    private BigDecimal maxPrice;

    @Column(name = "min_price", nullable = false)
    private BigDecimal minPrice;

    @Column(name = "open_price", nullable = false)
    private BigDecimal openPrice;

    @Column(name = "close_price", nullable = false)
    private BigDecimal closePrice;

    @Column(name = "volume", nullable = false)
    private BigDecimal volume;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @JdbcType((PostgreSQLEnumJdbcType.class))
    @Column(name = "direction", nullable = false)
    private DirectionCandle direction;

    @Override
    public int compareTo(Candle o) {
        if (o == null || o.getStartTime() == null) {
            throw new NullPointerException("Candle or startTime is null");
        }

        return this.getStartTime().compareTo(o.getStartTime());
    }
}
