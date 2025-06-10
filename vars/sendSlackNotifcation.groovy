def call(String channel, String submitter = null, String changelist = null) {
    if (submitter == null && changelist == null) {
        submitter = findItemInChangelog("changeUser")
        clmsg = findItemInChangelog("msg")
    } else {
        submitter = submitter
        changelist = changelist
        clmsg = "No message provided"
    }
    def buildSummary = ""

    if (currentBuild.currentResult == "SUCCESS") {
        buildSummary = "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n at CL[${changelist != null ? changelist : env.P4_CHANGELIST}]-->${clmsg} \n committed by '${submitter}'\n"
        def slackResponse = slackSend(color: "good", message: "${buildSummary}", channel: channel) 
        slackSend message: "Starting smoke test pipeline...", channel: slackResponse.threadId
    } else {
        buildSummary = "FAILED Job:  ${env.JOB_NAME} [${env.BUILD_NUMBER}]' \n at CL[${changelist != null ? changelist : env.P4_CHANGELIST}]-->${clmsg} \n committed by '${submitter}'\n failed at '${FAILED_STAGE}' stage \n"
        slackSend(color: "danger", message: "${buildSummary}", channel: channel)
    }
}