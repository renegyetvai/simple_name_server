pipeline {
    agent { label 'java-docker-node-1' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }
    tools {
        gradle 'gradle751'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        DOCKERHUB_CREDENTIALS = credentials('rgyetvai-dockerhub')
    }
    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'server_cicd', changelog: false, poll: false, url: 'https://github.com/renegyetvai/simple_name_server.git'
            }
        }
        stage('Compile Sources') {
            steps {
                sh './gradlew clean compileJava'
            }
        }
        stage('OWASP Scan') {
            steps {
                dependencyCheck additionalArguments: '', odcInstallation: 'DP-check'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
        stage('Sonarqube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=simple_name_server \
                    -Dsonar.java.binaries=. \
                    -Dsonar.projectKey=simple_name_server \
                    -Dsonar.exclusions=dependency-check-report.html '''
                }
            }
        }
        stage('Build Project') {
            steps {
                sh './gradlew build --scan'
            }
        }
        stage('Docker - Build Container Image') {
            steps {
                sh 'docker build -t rgyetvai/simple_name_server:latest .'
            }
        }
        stage('Trivy Image Scan') {
            steps {
                sh 'trivy image --exit-code 1 --severity MEDIUM,HIGH,CRITICAL rgyetvai/simple_name_server:latest'
            }
        }
        stage('Docker Push') {
            steps {
                script {
                    sh 'docker login -u $DOCKERHUB_CREDENTIALS_USR -p $DOCKERHUB_CREDENTIALS_PSW'
                    sh 'docker push rgyetvai/simple_name_server:latest'
                }
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
        success {
            echo 'Build Success'
        }
        failure {
            echo 'Build Failed'
        }
        unstable {
            echo 'Build Unstable'
        }
        changed {
            echo 'Build Changed'
        }
    }
}