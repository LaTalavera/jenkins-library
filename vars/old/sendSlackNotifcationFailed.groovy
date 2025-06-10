def call() 
{
    slackSend color : "#FF0000", message: "FAILED Job:  ${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n failed at '${STAGE_NAME}' stage", channel: '#test_harness_logs_phoenix'
}