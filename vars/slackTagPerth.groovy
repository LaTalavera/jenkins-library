def call()
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
    //TODO check why log is not being uploaded maybe @NonCPS is needed or maybe we need to extract functionality to aonother file
    String message = " <@$userId> Build failed" + "${icons[randomIndex]}" + " please check your last commit, here is the build log:"
    if (userIds.containsValue(userId) ) {
        slackSend color : "danger", message: "<@$userId> Build failed" + "${icons[randomIndex]}" + " please check your last commit, here is the build log:", channel: '#jenkins_bot_perth'
        slackUploadFile channel: "#jenkins_bot_perth",  filePath: "log"
    } 
}