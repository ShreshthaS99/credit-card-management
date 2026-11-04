package com.example.ccmps.client;

import feign.FeignException;
import java.util.function.Predicate;

public class RemoteFailurePredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable failure) {
        return !(failure instanceof FeignException remote
                && remote.status() >= 400 && remote.status() < 500);
    }
}
