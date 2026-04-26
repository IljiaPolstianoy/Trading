package io.github.ijlijapol.contract;

import io.github.ijlijapol.bybit.exception.InsufficientFundsException;
import io.github.ijlijapol.bybit.exception.NotFoundOrderException;
import io.github.ijlijapol.bybit.model.Symbol;
import io.github.ijlijapol.bybit.model.order.ModifiedOrder;
import io.github.ijlijapol.bybit.model.order.Order;
import io.github.ijlijapol.bybit.model.order.Side;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;

import java.math.BigDecimal;


public interface ExchangeConnector {

    /**
     * Создает и отправляет новый ордер на биржу.
     *
     * @param order ордер для исполнения с параметрами:
     *              <ul>
     *                  <li><b>symbol</b>: {@link Symbol} торгуемая пара</li>
     *                  <li><b>side</b>: {@link Side#BUY BUY} (покупка) или {@link Side#SELL SELL} (продажа)</li>
     *                  <li><b>orderType</b>:
     *                      {@link TradeOrderType#MARKET MARKET} (рыночный) или
     *                      {@link TradeOrderType#LIMIT LIMIT} (лимитный)</li>
     *                  <li><b>price</b>:
     *                          <br>&mdash; для LIMIT ордеров: обязателен, должен быть > 0
     *                          <br>&mdash; для MARKET ордеров: не используется (можно null)</li>
     *                  <li><b>amount</b>: {@link BigDecimal} количество торгуемого актива</li>
     *                  <li><b>Сам объект</b>: не может быть null</li>
     *              </ul>
     * @return {@code true} если операция прошла успешно
     * @throws InsufficientFundsException если недостаточно средств для покупки
     */

    void createNewOrder(Order order);

    /**
     * Изменяет существующий ордер на бирже.
     *
     * @param modifiedOrder объект с изменениями ордера. Учитываются только следующие поля:
     *                      <ul>
     *                          <li><b>Идентификатор (обязательный)</b>:
     *                              <ul>
     *                                  <li><b>orderID</b>: {@code String}
     *                                  — ID ордера для изменения (не может быть {@code null})</li>
     *                              </ul>
     *                          </li>
     *                          <li><b>Изменяемые параметры (опциональные)</b>:
     *                              <ul>
     *                                  <li><b>price</b>: {@link BigDecimal}
     *                                  — новая цена ордера. Если {@code null}, цена не меняется.</li>
     *                                  <li><b>amount</b>: {@link BigDecimal}
     *                                  — новое количество торгуемого актива.
     *                                      Если {@code null}, количество не меняется.</li>
     *                              </ul>
     *                          </li>
     *                      </ul>
     *                      <p><b>Важно:</b> Поля {@code symbol}, {@code side} и {@code orderType} игнорируются
     *                      этим методом.</p>
     * @return {@code true} если изменение выполнено успешно
     * @throws NotFoundOrderException     если orderID не указан или ордер не найден
     * @throws InsufficientFundsException если недостаточно средств для изменения цены
     */
    boolean changeOldOrder(ModifiedOrder modifiedOrder);

    /**
     * Отменяет активный ордер
     *
     * @param orderID ID активного ордера
     * @return {@code true} если отмена выполнена успешно
     * @throws NotFoundOrderException если orderID не указан или ордер не найден
     */
    boolean cancelOrder(String orderID);

    /**
     * Отменяет все ордера по <b>типу</b> ордера
     *
     * @param orderType тип ордера:
     *                  <ul>
     *                      <li>{@link TradeOrderType#MARKET MARKET}</li>
     *                      <li>{@link TradeOrderType#LIMIT LIMTI}</li>
     *                  </ul>
     * @return {@code true} если операция выполнена успешно
     */
    boolean cancelAllOrders(TradeOrderType orderType);


    /**
     * Отеняются все ордера по <b>коду</b> торгуемого актива
     *
     * @param symbol код торгуемого актива
     * @return {@code true} если операция выполнена успешно
     */
    boolean cancelAllOrders(Symbol symbol);

}
