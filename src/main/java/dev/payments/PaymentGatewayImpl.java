package dev.payments;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PaymentGatewayImpl implements PaymentGateway{
    private final static Random rnd = new Random();

    public PaymentGatewayImpl() {

    }
    public boolean processTransaction(){
        return rnd.nextInt(1, 100) > 3;
    }
}
