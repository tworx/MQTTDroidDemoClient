pipeline {
    environment {
        androidSDKImageName = "tworx/eud-builder:2022-03-22"
        registryCredentials = 'dockerhub-repo-credentials'
        nexusCredentials = credentials('nexus-build-agent-credentials')
        debugKeyStore = credentials('android-debug-keystore')
        debugKeyStorePwd = credentials('android-debug-keystore-pwd')
        debugKeyStoreAlias = credentials('android-debug-keystore-alias')
        debugKeyPwd = credentials('android-debug-key-password')
    }
    agent any
    stages {
        stage('Build sample client apps') {
            steps {
                script {                    
                    // need to save the host name of the repo as we are using docker compose network which does not resolve inside docker image
                    sh "getent hosts nginx | awk '{ print \$1 }' > repo-ip.txt"
                    docker.withRegistry("${TWORX_DOCKER_REPO}", "${registryCredentials}") {
                        docker.image("${androidSDKImageName}").inside {
                            sh "./gradlew -Ptworxrepo=http://\$(cat repo-ip.txt) -Pkeystore=${debugKeyStore} -PstorePass=${debugKeyStorePwd} -Palias=${debugKeyStoreAlias} -PkeyPass=${debugKeyPwd} test assembleDebug"
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            sendNotifications currentBuild.result, (currentBuild.getPreviousBuild() ? currentBuild.getPreviousBuild().result : null)
        }
    }
}
