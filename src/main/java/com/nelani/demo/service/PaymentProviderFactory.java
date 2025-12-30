package com.nelani.demo.service;

import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.provider.PaymentProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentProviderFactory {

        private final Map<PaymentProviderType, PaymentProvider> providers;

        public PaymentProviderFactory(List<PaymentProvider> providerList) {
                this.providers = providerList.stream()
                                .collect(Collectors.toMap(
                                                PaymentProvider::getType,
                                                provider -> provider));
        }

        public PaymentProvider get(PaymentProviderType type) {
                return Optional.ofNullable(providers.get(type))
                                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + type));
        }
}
