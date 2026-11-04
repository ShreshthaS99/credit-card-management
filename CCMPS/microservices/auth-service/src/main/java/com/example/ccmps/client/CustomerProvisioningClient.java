package com.example.ccmps.client;

import com.example.ccmps.contract.ProvisionCustomerRequest;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "customer-service", contextId = "customerProvisioning")
public interface CustomerProvisioningClient {
    @PutMapping("/internal/customers/{customerId}")
    void provision(@PathVariable("customerId") UUID customerId,
                   @RequestBody ProvisionCustomerRequest request,
                   @RequestHeader("Authorization") String authorization);
}
