pipeline{
    pipeline {
    agent any
    tools {
        maven 'M3'
    }
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/wambugucoder/SPRING-CLOUD-MULTI_AZ-WITH-LOAD-BALANCER-HEALTH-CHECK/tree/main/auth-microservice', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                dir('example-service') {
                    sh 'mvn clean package'
                }
            }
        }
        stage('Run') {
            steps {
                dir('example-service') {
                    sh 'nohup java -jar -DEUREKA_URL=http://localhost:8761/eureka target/example-service-1.0-SNAPSHOT.jar 1>/dev/null 2>logs/runlog &'
                }
            }
        }
    }
}
}