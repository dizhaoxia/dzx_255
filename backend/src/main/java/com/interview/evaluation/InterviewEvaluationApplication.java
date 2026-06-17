package com.interview.evaluation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.interview.evaluation.mapper")
public class InterviewEvaluationApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewEvaluationApplication.class, args);
    }
}
