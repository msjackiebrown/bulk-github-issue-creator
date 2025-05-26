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
                script {
                    def mvnHome = tool name: 'Default', type: 'maven'
                    sh "${mvnHome}/bin/mvn clean package"
                }
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
                        """
                        // Create the release and upload the JAR
                        def response = sh(
                            script: """curl -H "Authorization: token ${GITHUB_TOKEN}" \
                                -d '{\"tag_name\": \"v${mvnVersion}\", \"name\": \"Release v${mvnVersion}\"}' \
                                https://api.github.com/repos/msjackiebrown/bulk-github-issue-creator/releases""",
                            returnStdout: true
                        ).trim()
                        def uploadUrl = new groovy.json.JsonSlurperClassic().parseText(response).upload_url
                        uploadUrl = uploadUrl.replace("{?name,label}", "?name=bulk-github-issue-creator-${mvnVersion}.jar")
                        sh """
                            curl -H "Authorization: token ${GITHUB_TOKEN}" \
                                 -H "Content-Type: application/java-archive" \
                                 --data-binary @target/bulk-github-issue-creator-${mvnVersion}.jar \
                                 "${uploadUrl}"
                        """
                    }
                }
            }
        }
    }
}