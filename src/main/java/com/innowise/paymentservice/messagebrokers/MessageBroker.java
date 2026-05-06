package com.innowise.paymentservice.messagebrokers;

public interface MessageBroker {
    boolean sendMessage(Object message);
}
