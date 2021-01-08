package com.sample.webflux.controller;

import java.util.Arrays;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.Duration;
import java.util.stream.Stream;

public class ReactiveTemperatureController {

    private int temperature;
    private String weather;

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route(RequestPredicates.GET("/temperature"), this::temperature).
                andRoute(RequestPredicates.POST("/temperature"), this::postTemperature)
            .andRoute(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), GreetingHandler::hello)
            .andRoute(RequestPredicates.GET("/finite_stream"), this::finiteStream);
    }

    public Mono<ServerResponse> postTemperature(ServerRequest req){
        return  req.bodyToMono(Temperature.class)
                .flatMap(temperature1 -> {
                    this.temperature = temperature1.getTemperature();
                    this.weather = temperature1.getWeather();
                    return ServerResponse.ok().body(Mono.just(temperature1),Temperature.class);
                }).switchIfEmpty(ServerResponse.badRequest().build());
    }


    public Mono<ServerResponse> temperature(ServerRequest req) {
        Stream<Integer> stream = Stream.iterate(0, i -> i + 1);
        Flux<Temperature> mapFlux = Flux.fromStream(stream).zipWith(Flux.interval(Duration.ofSeconds(1)))
                .map(i -> {
                    Temperature templarature = new Temperature();
                    templarature.setTemperature(temperature);
                    templarature.setWeather(weather);
                    return templarature;
                });

        return ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(mapFlux,
                Temperature.class);
    }

  public Mono<ServerResponse> finiteStream(ServerRequest req) {
    String[] array = {"a", "b", "c", "d", "e"};

    //Arrays.stream
    Stream<String> stream = Arrays.stream(array);

    Flux<Temperature> mapFlux = Flux.fromStream(stream).zipWith(Flux.interval(Duration.ofSeconds(1)))
        .map(i -> {
          Temperature templarature = new Temperature();
          templarature.setTemperature(temperature);
          templarature.setWeather(weather);
          return templarature;
        });

    return ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(mapFlux,
        Temperature.class);
  }
}
