import groovy.util.XmlSlurper
@NonCPS
def call(String msgOrUser) {
    def changelogFile = env.HUDSON_CHANGELOG_FILE
    def changelog = new XmlSlurper().parseText(new File(changelogFile).text)
    def firstChange = changelog.entry[0]
    def commitUser = firstChange.changenumber.changeUser.text()
    def commitMsg = firstChange.changenumber.msg.text()
    if (msgOrUser == "changeUser") {
        if(commitUser == ""){
            return "Jesus"
        }
        return commitUser        
    } else if (msgOrUser == "msg") {
        if(commitMsg == ""){
            return "Build executed manually"
        }
        return commitMsg
    } else {
        throw new Exception("Invalid parameter. Expected msg or changeUser.")
    }
}