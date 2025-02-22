pipeline {
    agent any 
    stages {
        stage('Hello accounts main Feb 22') {
            steps {
                echo "Hello from Jenskinfile"
            }
        }
        stage('For the fix branch') {
            when {
            	branch "bank-*"
            }
            steps {
                sh '''
                	cat README.md
                '''
            }
        }
        stage('For the PR') {
            when {
            	branch "PR-*"
            }
            steps {
                echo 'For Pull request only'
            }
        }
    }
}
