def call(String stream)
{
    String[] icons = [":goose:", ":goose3:", ":goose4:", ":goosehype:"]
    int randomIndex = (new Random()).nextInt(icons.size())
    String submitter = findItemInChangelog("changeUser")
    String userId = ""
    def userIds = sharedVariables.getUserIds()

    for (user in userIds.keySet()) {
        if (submitter == user){
            userId = userIds.get(user)
        }
    }
    if (userIds.containsValue(userId) ) {
        if (stream == "daily")
        {
            slackSend color : "danger", message: "<@$userId> Build failed" + "${icons[randomIndex]}" + " please check your last commit, here is the build log:", channel: '#jenkins_bot_phoenix'
            slackUploadFile channel: "#jenkins_bot_phoenix",  filePath: "log"
        }
        else if (stream == "dlc_fe")
        {
            slackSend color : "danger", message: "<@$userId> Build failed " + "${icons[randomIndex]}" + " please check your last commit, here is the build log:", channel: '#jenkins_bot_phoenix_dlcfe'
            slackUploadFile channel: "#jenkins_bot_phoenix_dlcfe",  filePath: "log"
        }   
        else if (stream == "dlc_fe_future")
        {
            slackSend color : "danger", message: "<@$userId> Build failed" + "${icons[randomIndex]}" + " please check your last commit, here is the build log:", channel: '#jenkins_bot_phoenix_dlcfe_future'
            slackUploadFile channel: "#jenkins_bot_phoenix_dlcfe_future",  filePath: "log"
        }
    }  
}          



//Refactored version (not tested)

// def call(String stream) {
//     def icons = [":goose:", ":goose3:", ":goose4:", ":goosehype:"]
//     def userIds = sharedVariables.getUserIds()
//     def submitter = findItemInChangelog("changeUser")
//     def userId = userIds[submitter]
//     def channel = "#jenkins_bot_phoenix"
//     def color = "danger"
//     def message = "<@$userId> Build failed ${icons[new Random().nextInt(icons.size())]} please check your last commit, here is the build log:"
//     def filePath = "log"
//     switch (stream) {
//         case "daily":
//             channel += ""
//             break
//         case "dlc_fe":
//             channel += "_dlcfe"
//             break
//         case "dlc_fe_future":
//             channel += "_dlcfe_future"
//             break
//         default:
//             return
//     }
//     slackSend color: color, message: message, channel: channel
//     slackUploadFile channel: channel, filePath: filePath
// }