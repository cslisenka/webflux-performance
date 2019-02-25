package com.example.demoreactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.function.Supplier;

@RestController
@RequestMapping("/demo")
@SpringBootApplication
public class DemoReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoReactiveApplication.class, args);
	}

    @Bean(name = "first")
    public Scheduler scheduler2() {
        return Schedulers.newParallel("first", 15);
    }

	@Bean(name = "second")
	public Scheduler scheduler1() {
	    return Schedulers.newParallel("second", 5);
    }

    @Autowired
    @Qualifier("first")
    private Scheduler first;

    @Autowired
    @Qualifier("second")
    private Scheduler second;

	// By default reactive HTTP connector has 4 threads on my machine
    @GetMapping("/{id}")
	public Mono<DataTransferObject> getDataReactive(@PathVariable String id) {
        Supplier<DataTransferObject> supplier = () -> getDTO(id);
//        return Mono.just(supplier.get());

        if (id.equals("1")) {
            return Mono.fromSupplier(supplier).subscribeOn(first);
        } else {
            return Mono.fromSupplier(supplier).subscribeOn(second);
        }
	}

//	@GetMapping("/{id}")
	public DataTransferObject getDataSync(@PathVariable String id) {
        return getDTO(id);
    }

    private DataTransferObject getDTO(String id) {
        DataTransferObject dto = new DataTransferObject();

        // Doing blocking call
        long val = 0;
        if (id.equals("1")) {
            val = blockingCall(100);
        } else {
            val = blockingCall(5000);
        }

        dto.setV1(id + "_" + val);
        dto.setV2(id + "_" + Thread.currentThread().getName());
        return dto;
    }

	private long blockingCall(int delay) {
	    long startTime = System.currentTimeMillis();

        try {
            System.out.println("blocking call " + Thread.currentThread().getName());
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }

        return System.currentTimeMillis() - startTime;
    }
}