def call(){
    slackUploadFile channel: "#jenkins_bot_phoenix",  filePath: "C:/Users/User/.jenkins/jobs/Phoenix_pipeline/builds//${env.BUILD_NUMBER}/log"
}