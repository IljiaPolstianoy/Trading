package io.github.ilijapol.common.model.order;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ModifiedOrder extends Order {

    private String orderID;
}