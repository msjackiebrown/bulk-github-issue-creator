// Jenkinsfile
pipeline {
    agent any
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
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
                        echo "mvnVersion: ${mvnVersion}"
                        sh "ls -l target"
                        sh """
                            git config user.name "jenkins"
                            git config user.email "jenkins@example.com"
                            git tag -d v${mvnVersion} || true
                            git push --delete origin v${mvnVersion} || true
                            git tag v${mvnVersion}
                            git push origin v${mvnVersion}
                        """
                        // Create the release and upload the JAR
                        def response = sh(
                            script: """curl -s -H "Authorization: token \$GITHUB_TOKEN" \
                                -d '{"tag_name": "v${mvnVersion}", "name": "Release v${mvnVersion}"}' \
                                https://api.github.com/repos/msjackiebrown/bulk-github-issue-creator/releases""",
                            returnStdout: true
                        ).trim()
                        def uploadUrl = sh(
                            script: """echo '${response}' | jq -r .upload_url | sed 's/{?name,label}/?name=${artifactName}/'""",
                            returnStdout: true
                        ).trim()
                        def artifactId = "bulk-github-issue-creator" // change if needed
                        def artifactName = "bulk-github-issue-creator-${mvnVersion}-jar-with-dependencies.jar"
                        def artifactPath = "target/${artifactName}"
                        sh """
                            curl -H "Authorization: token \$GITHUB_TOKEN" \
                                 -H "Content-Type: application/java-archive" \
                                 --data-binary @${artifactPath} \
                                 "${uploadUrl}"
                        """
                    }
                }
            }
        }
        stage('Deploy to GitHub Packages') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'GITHUB_PACKAGES', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
                    script {
                        def mvnHome = tool name: 'Default', type: 'maven'
                        sh "${mvnHome}/bin/mvn deploy -DskipTests=true -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/msjackiebrown/bulk-github-issue-creator -Dgithub.username=$GITHUB_USER -Dgithub.token=$GITHUB_TOKEN"
                    }
                }
            }
        }
    }
}