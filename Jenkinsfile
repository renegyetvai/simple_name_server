pipeline {
    agent { label 'agent1' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
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
        stage('Compile') {
            steps {
                sh './gradlew clean compileJava'
            }
        }
        stage('OWASP Scan') {
            steps {
                dependencyCheck additionalArguments: '--format HTML', odcInstallation: 'DP-check'
            }
        }
        stage('Sonarqube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=simple_name_server \
                    -Dsonar.java.binaries=. \
                    -Dsonar.projectKey=simple_name_server '''
                }
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build --scan'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t rgyetvai/simple_name_server-alpine:latest .'
            }
        }
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }
        stage('Docker Push') {
            steps {
                sh 'docker push rgyetvai/simple_name_server-alpine:latest'
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