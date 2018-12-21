package org.lfenergy.operatorfabric.cards.consultation.config.webflux;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.operatorfabric.cards.consultation.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Configuration
public class CardRoutesConfig {

    private final CardRepository cardRepository;

    @Autowired
    public CardRoutesConfig(CardRepository cardRepository){
        this.cardRepository = cardRepository;
    }

    /**
     * Card route configuration
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> cardRoutes() {
        return RouterFunctions
                .route(RequestPredicates.GET("/cards/{id}"),cardGetRoute())
                .andRoute(RequestPredicates.OPTIONS("/cards/{id}"),cardOptionRoute());
    }


    private HandlerFunction<ServerResponse> cardGetRoute() {
        return request ->{
            return cardRepository.findById(request.pathVariable("id"))
                    .flatMap(card-> ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(card)))
                    .switchIfEmpty(notFound().build());
        };
    }

    private HandlerFunction<ServerResponse> cardOptionRoute() {
        return request -> ok().build();
    }
}