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
                    sh '''
                        git config user.name "jenkins"
                        git config user.email "jenkins@example.com"
                        git tag v${BUILD_NUMBER}
                        git push origin v${BUILD_NUMBER}
                        curl -H "Authorization: token $GITHUB_TOKEN" \
                             -d '{"tag_name": "v${BUILD_NUMBER}", "name": "Release v${BUILD_NUMBER}"}' \
                             https://api.github.com/repos/msjackiebrown/bulk-github-issue-creator/releases
                    '''
                }
            }
        }
    }
}