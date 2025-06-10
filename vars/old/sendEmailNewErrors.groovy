import groovy.json.JsonSlurper

@NonCPS
def call(){
    def submitter = findItemInChangelog("changeUser")
    def emails = [
        "charlieh": "charbord@forgottenempires.net",
        "e-sobra": "sobraztsov@forgottenempires.net",
        "ivank": "ikirichenko@forgottenempires.net",
        "jcarr": "jcarr@forgottenempires.net",
        "Marius Beck": "mbeck@forgottenempires.net",
        "raymik": "rklingers@forgottenempires.net",
        "tapsa": "mpartonen@forgottenempires.net",
        "cgourdie":  "cgourdie@forgottenempires.net",
        "aoe_scout": "emarinov@forgottenempires.net",
        "kwright": "kwright@forgottenempires.net",
        "mwinocur" : "mwinocur@forgottenempires.net",
        "idjordjevic" : "idjordjevic@forgottenempires.net",
        "jfernandez": "jfernandez@forgottenempires.net"
    ]
    def email = "jfernandez@forgottenempires.net"
    def msgList = []
    def url = "http://localhost:8080/job/${JOB_NAME}/${BUILD_NUMBER}/cppcheck/new/api/json"
    def json = new JsonSlurper().parseText(new URL(url).text)

    for (key in emails.keySet()) {
        if (submitter == key){
            email = emails.get(key)
        }
    }

    json.issues.each{issue->
        def msg = "New ERROR found in static analysis, IN BUILD NUMBER ${BUILD_NUMBER}, TYPE OF ERROR ${issue.type}"+
            ", SEVERITY: ${issue.severity}, ERROR MESSAGE: ${issue.message}"+
            ", FILE ${issue.fileName} AT LINE: ${issue.lineStart}"
        msgList.add(msg)
    }

    sendOneMailPerError(msgList, email)    
}
