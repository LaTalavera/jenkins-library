@Library('my-library') _

pipeline {
    parameters {
        // Maximum number of build attempts (Build + Rebuild)
        // Default is 2: 1 Build attempt and 1 Rebuild attempt
        string(name: 'MAX_ATTEMPTS', defaultValue: '2', description: 'Maximum number of build attempts per stage (Build + Rebuild)')
    }

    agent {
        node {
            label 'master'
            customWorkspace 'D:/Perth'
        }
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 75, unit: 'MINUTES')
    }

    stages {
        stage('Get latest') {
            steps {
                script {
                    env.FAILED_STAGE = env.STAGE_NAME
                    slackSend color: "good",
                              message: "Perth pipeline has just been triggered...",
                              channel: '#jenkins_bot_perth'
                }
                checkout perforce(
                    credential: 'P4-Phoenix',
                    populate: syncOnly(
                        force: false,
                        have: true,
                        modtime: false,
                        parallel: [enable: false, minbytes: '1024', minfiles: '1', threads: '8'],
                        pin: '',
                        quiet: true,
                        revert: false
                    ),
                    workspace: manualSpec(
                        charset: 'none',
                        cleanup: false,
                        name: 'jfernandez_PERTH-BUILD-MACHINE_2',
                        pinHost: true,
                        spec: clientSpec(
                            allwrite: false,
                            backup: true,
                            changeView: '',
                            clobber: true,
                            compress: false,
                            line: 'LOCAL',
                            locked: false,
                            modtime: false,
                            rmdir: false,
                            serverID: '',
                            streamName: '//phoenix_stream/daily_dev_minimal',
                            type: 'WRITABLE',
                            view: ''
                        )
                    )
                )
            }
        }

        stage('Build Desktop') {
            steps {
                script {
                    env.FAILED_STAGE = env.STAGE_NAME
                    def msbuild = tool name: 'MS_Build22', type: 'hudson.plugins.msbuild.MsBuildInstallation'

                    // Parse MAX_ATTEMPTS parameter
                    int maxAttempts = params.MAX_ATTEMPTS.toInteger()
                    boolean buildSucceeded = false
                    String buildOutcome = ''

                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                        try {
                            if (attempt == 1) {
                                echo "========== Attempt #${attempt}: BUILD (Desktop) =========="
                                // Initial Build
                                bat "\"${msbuild}\" Perth/Perth.sln /t:Build /m /p:configuration=\"Development\" /p:Platform=\"Gaming.Desktop.X64\" /p:CL_MPCount=16 /p:AdditionalOptions=\"/Zm200\""
                                buildOutcome = 'Desktop Build (first attempt)'
                            } else {
                                echo "========== Attempt #${attempt}: REBUILD (Desktop) =========="
                                // Rebuild on failure
                                bat "\"${msbuild}\" Perth/Perth.sln /t:Rebuild /m /p:configuration=\"Development\" /p:Platform=\"Gaming.Desktop.X64\" /p:CL_MPCount=16 /p:AdditionalOptions=\"/Zm200\""
                                buildOutcome = 'Desktop Rebuild (second attempt)'
                            }
                            buildSucceeded = true
                            echo "SUCCESS on attempt #${attempt} for Desktop."
                            break // Exit loop on success
                        } catch (Exception e) {
                            echo "FAILED on attempt #${attempt} for Desktop."
                            if (attempt < maxAttempts) {
                                echo "Preparing to retry by performing a Rebuild for Desktop..."
                            } else {
                                echo "All build attempts failed for Desktop."
                                throw e // Mark stage as failed
                            }
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        def submitter = findItemInChangelog("changeUser")
                        def clmsg = findItemInChangelog("msg")
                        slackSend color: "good", message: "SUCCESSFUL: Job '${env.STAGE_NAME} [${env.BUILD_NUMBER}]'\n at CL[${changelist: env.P4_CHANGELIST}]-->${clmsg} \n committed by '${submitter}'\n", channel: '#jenkins_bot_perth'

                    }
                }
                failure {
                    script {
                        dir("C:/Users/User/.jenkins/jobs/Build_Desktop_Xbox/builds/${env.BUILD_NUMBER}") {
                            checkHeapError()
                        }
                    }
                }
            }
        }

        stage('Build Xbox') {
            steps {
                script {
                    env.FAILED_STAGE = env.STAGE_NAME
                    def msbuild = tool name: 'MS_Build22', type: 'hudson.plugins.msbuild.MsBuildInstallation'

                    // Parse MAX_ATTEMPTS parameter
                    int maxAttempts = params.MAX_ATTEMPTS.toInteger()
                    boolean buildSucceeded = false
                    String buildOutcome = ''

                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                        try {
                            if (attempt == 1) {
                                echo "========== Attempt #${attempt}: BUILD (Xbox) =========="
                                // Initial Build
                                bat "\"${msbuild}\" Perth/Perth.sln /t:Build /p:configuration=\"Development\" /p:Platform=\"Gaming.Xbox.Scarlett.X64\" /p:DeployOnBuild=true /p:DeployTarget=RemoteMachine /p:RemoteMachine=10.149.1.85 /p:CL_MPCount=16 /p:AdditionalOptions=\"/Zm200\""
                                buildOutcome = 'Xbox Build (first attempt)'
                            } else {
                                echo "========== Attempt #${attempt}: REBUILD (Xbox) =========="
                                // Rebuild on failure
                                bat "\"${msbuild}\" Perth/Perth.sln /t:Rebuild /p:configuration=\"Development\" /p:Platform=\"Gaming.Xbox.Scarlett.X64\" /p:DeployOnBuild=true /p:DeployTarget=RemoteMachine /p:RemoteMachine=10.149.1.85 /p:CL_MPCount=16 /p:AdditionalOptions=\"/Zm200\""
                                buildOutcome = 'Xbox Rebuild (second attempt)'
                            }
                            buildSucceeded = true
                            echo "SUCCESS on attempt #${attempt} for Xbox."
                            break // Exit loop on success
                        } catch (Exception e) {
                            echo "FAILED on attempt #${attempt} for Xbox."
                            if (attempt < maxAttempts) {
                                echo "Preparing to retry by performing a Rebuild for Xbox..."
                            } else {
                                echo "All build attempts failed for Xbox."
                                throw e // Mark stage as failed
                            }
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        def submitter = findItemInChangelog("changeUser")
                        def clmsg = findItemInChangelog("msg")
                        slackSend color: "good", message: "SUCCESSFUL: Job '${env.STAGE_NAME} [${env.BUILD_NUMBER}]'\n at CL[${changelist: env.P4_CHANGELIST}]-->${clmsg} \n committed by '${submitter}'\n", channel: '#jenkins_bot_perth'

                    }
                }
                failure {
                    script {
                        dir("C:/Users/User/.jenkins/jobs/Build_Desktop_Xbox/builds/${env.BUILD_NUMBER}") {
                            checkHeapError()
                        }
                    }
                }
            }
        }

        stage("Write CL to file") {
            steps {
                script {
                    def changelist = env.P4_CHANGELIST
                    writeFile file: 'C:\\Phoenix_daily\\changelist.txt', text: "${changelist}"
                }
            }
        }
    }

    post {
        success {
            script {
                build job: 'clean_folders', propagate: false, wait: false
                def p4_user = findItemInChangelog("changeUser")
                build job: 'smoke_test_desktop',
                      propagate: false,
                      wait: true,
                      parameters: [
                          string(name: 'p4_user', value: p4_user),
                          string(name: 'changelist', value: env.P4_CHANGELIST)
                      ]
            }
        }
    }
}
