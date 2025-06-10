def call(){
    def msg = findItemInChangelog("msg")
    def merging = msg.find("Merging")
    def merge = msg.find("Merge")
    if (merging == "Merging" || merge == "Merge"){
       slackSend color : "warning", message: "Merge detected, testing old scenarios", channel: '#jenkins_bot_phoenix_dlc'
       build job: 'old_scenarios', propagate: true, wait: false 
    }
}
