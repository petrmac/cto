package de.conrad.ccp.comline.service;


import de.conrad.ccp.comline.api.model.Product;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Product> getProductByCtoNr(String ctoNr, String accessToken);

    class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }

    class InvalidResponseException extends RuntimeException {
        public InvalidResponseException(String message) {
            super(message);
        }

        public InvalidResponseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
