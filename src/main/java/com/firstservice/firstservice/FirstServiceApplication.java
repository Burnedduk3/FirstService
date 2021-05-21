package com.firstservice.firstservice;

import com.firstservice.firstservice.models.pojo.FileStorageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class FirstServiceApplication {

    @Value("${queue.name}")
    String queueName;

    @Value("${spring.rabbitmq.port}")
    Integer queuePort;

    @Value("${spring.rabbitmq.username}")
    String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    String rabbitPassword;

    @Value("${spring.rabbitmq.host}")
    String queueHost;

    public static void main(String[] args) {
        SpringApplication.run(FirstServiceApplication.class, args);
    }

    @Bean
    public Queue myQueue(){
        return new Queue(queueName, false);
    }

}
