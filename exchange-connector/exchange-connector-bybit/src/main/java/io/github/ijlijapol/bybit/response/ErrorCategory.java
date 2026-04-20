package io.github.ijlijapol.bybit.response;

public enum ErrorCategory {
    API_BUSINESS,    // бизнес-ошибка API (недостаточно средств, неверный символ)
    API_AUTH,        // ошибка аутентификации (неверный ключ, подпись)
    API_LIMIT,       // превышение лимитов запросов
    NETWORK,         // сетевая ошибка (тайм-аут, DNS)
    FORMAT,          // ошибка формата ответа
    UNKNOWN;         // неизвестная ошибка

    public static ErrorCategory fromApiErrorCode(Integer errorCode) {
        if (errorCode == null) return UNKNOWN;
        return switch (errorCode) {
            case 10003, 10004, 10005, 10010, 100607, 100608, 100609 -> API_AUTH;
            case 10006, 30073 -> API_LIMIT;
            case 10001, 10002, 10017, 10021, 10022, 100400, 100401,
                 11041, 11043, 11044, 30063 -> API_BUSINESS;
            default -> API_BUSINESS;
        };
    }
}
