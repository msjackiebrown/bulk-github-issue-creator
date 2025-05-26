// Jenkinsfile
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                // Add your build steps here
                echo 'Building...'
            }
        }
        stage('Tag & Release') {
            steps {
                withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                    script {
                        def mvnHome = tool name: 'Default', type: 'maven'
                        def mvnVersion = sh(
                            script: "${mvnHome}/bin/mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                            returnStdout: true
                        ).trim()
                        sh """
                            git config user.name "jenkins"
                            git config user.email "jenkins@example.com"
                            git tag v${mvnVersion}
                            git push origin v${mvnVersion}
                            curl -H "Authorization: token \$GITHUB_TOKEN" \
                                 -d '{\"tag_name\": \"v${mvnVersion}\", \"name\": \"Release v${mvnVersion}\"}' \
                                 https://api.github.com/repos/msjackiebrown/bulk-github-issue-creator/releases
                        """
                    }
                }
            }
        }
    }
}