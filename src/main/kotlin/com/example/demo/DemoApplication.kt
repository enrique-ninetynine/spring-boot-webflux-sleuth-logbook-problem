package com.example.demo


import brave.http.HttpTracing
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookServerHandler
import reactor.netty.http.brave.ReactorNettyHttpTracing
import reactor.netty.http.server.HttpServer

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@RestController
@RequestMapping("/test")
class TestController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun test(): SomeResponse {
        logger.info("this trace has trace id")
        return SomeResponse()
    }
}

data class SomeResponse(val someValue: String = "VALUE")

@Configuration
class LogbookNettyConfiguration {

    @Bean
    fun reactorNettyHttpTracing(httpTracing: HttpTracing): ReactorNettyHttpTracing =
        ReactorNettyHttpTracing.create(httpTracing)

    @Bean
    fun logbookNettyServerCustomizer(logbook: Logbook, tracing: ReactorNettyHttpTracing): NettyServerCustomizer {
        return NettyServerCustomizer { httpServer: HttpServer ->
            tracing.decorateHttpServer(httpServer).doOnConnection { it.addHandlerLast(LogbookServerHandler(logbook)) }
        }
    }

}
