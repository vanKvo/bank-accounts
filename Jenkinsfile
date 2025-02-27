pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "dolphin2002/accounts:main"
        DOCKER_HUB_CREDENTIALS_ID = "dockerhub-token"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/vanKvo/bank-accounts.git'
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    // If using Maven, compile, test, and package the code.
                    sh 'mvn clean package'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    //sh "docker build -t ${DOCKER_IMAGE}:latest ."
                    sh 'mvn compile jib:dockerBuild'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withDockerRegistry([credentialsId: DOCKER_HUB_CREDENTIALS_ID, url: ""]) {
                        sh "docker push ${DOCKER_IMAGE}:main"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh """
                    docker stop accounts-main-container || true
                    docker rm accounts-main-container || true
                    docker run -d --name accounts-main-container -p 8081:8081 ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Deployment successful!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
