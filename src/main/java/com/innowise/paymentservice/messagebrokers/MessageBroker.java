package com.innowise.paymentservice.messagebrokers;

public interface MessageBroker {
    void sendMessage(Object message);
}
