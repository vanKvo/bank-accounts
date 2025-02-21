pipeline {
    agent any 
    stages {
        stage('Hello') {
            steps {
                echo "Hello from Jenskinfile"
            }
        }
        stage('For the fix branch') {
            when {
            	branch "fix-*"
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
