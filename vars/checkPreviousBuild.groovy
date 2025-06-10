@NonCPS
def call(String pipelineName, String stream, Boolean heapError) {
    def job = Jenkins.instance.getItemByFullName(pipelineName)
    def previousBuild = job.getLastBuild()?.getPreviousBuild()

    switch(pipelineName) {
        case "Phoenix_pipeline":
            if (heapError) {
                slackSend color : "danger", message: "BUILD FAILED BECAUSE OF INTERNAL HEAP LIMIT REACHED", channel: '#jenkins_bot_phoenix'
            } else if (previousBuild?.result == Result.SUCCESS) {
                slackTagPhoenix(stream)
                println ("SLACK TAG PHOENIX")
            }
            break;
        case "phoenix_dlcfe":
            if (heapError) {
                slackSend color : "danger", message: "BUILD FAILED BECAUSE OF INTERNAL HEAP LIMIT REACHED", channel: '#jenkins_bot_phoenix_dlcfe'
            } 
            else if (previousBuild?.result == Result.SUCCESS) {
                slackTagPhoenix(stream)
                println ("SLACK TAG PHOENIX DLCFE")
            }
            break;
        case "Phoenix_dlcfe_future":
            if (heapError) {
                slackSend color : "danger", message: "BUILD FAILED BECAUSE OF INTERNAL HEAP LIMIT REACHED", channel: '#jenkins_bot_phoenix_dlcfe_future'
            } 
            else if (previousBuild?.result == Result.SUCCESS) {
                slackTagPhoenix(stream)
                println ("SLACK TAG PHOENIX DLCFE FUTURE")
            }
            break;
        case "Perth_pipeline":
            if (heapError) {
                slackSend color : "danger", message: "BUILD FAILED BECAUSE OF INTERNAL HEAP LIMIT REACHED", channel: '#jenkins_bot_perth'
            } 
            else if (previousBuild?.result == Result.SUCCESS) {
                slackTagPerth()
                println ("SLACK TAG PERTH")
            }
            break;
        default:
            println("Invalid pipeline name")
    }
    println 'I am finishing the checkPreviousBuild.groovy script'
}


