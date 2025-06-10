import groovy.json.JsonSlurperClassic
import groovy.io.FileType

@NonCPS
Map readData(String reportPath) {
    String path = "${reportPath}"
    def jsonFiles = new File(path).listFiles().findAll { it.name.endsWith(".json") }
    if (jsonFiles.isEmpty()) {
        println "No JSON files found in the directory: ${path}"
        return null
    }
    String jsonFileToRead = jsonFiles.sort { -it.lastModified() }?.head()
    def jsonSlurper = new JsonSlurperClassic()
    return jsonSlurper.parse(new File(jsonFileToRead))
}

def call(String buildMachineName, String reportPath, String p4_user = "test") {
    println "Report Path: " + reportPath
    Map data = readData(reportPath)
    if (data == null) {
        println "No data to process."
        return
    }
    def pipelineName = env.JOB_NAME
    List<Map> allData = data.results
    int testPassed = 0
    int testTimeout = 0
    int testFailed = 0
    String userHome = System.getProperty("user.home")
    String clNumber = getBuildNumber("${userHome}\\Games\\Age of Empires 2 DE\\logs\\Age2SessionData.txt")
    List<String> timeoutTestName = []
    List<String> failedTestName = []

    for (def obj : allData) {
        def testName = obj.TestName
        def result = obj.result

        if (result == "pass") {
            testPassed++
        } else if (result == "fail") {
            testFailed++
            failedTestName.add(testName)
        } else if (result == "timeout") {
            testTimeout++
            timeoutTestName.add(testName)
        }
    }

    switch (buildMachineName) {
        case "Steam":
            slackChannel = "#bvt_results_steam"
            clNumber = getBuildNumber("${userHome}\\Games\\Age of Empires 2 DE\\logs\\Age2SessionData.txt") 
            break
        case "MSStore":
            slackChannel = "#bvt_results_microsoft"
            clNumber = changelistNumber
            break
        case "Perth":
            slackChannel = "#bvt_results_perth"
            clNumber = changelistNumber
            break  
        case "Phoenix_Build_Machine":
            slackChannel = "#jenkins_bot_phoenix"
            clNumber = changelistNumber
            break
        case "Perth_Build_Machine":
            slackChannel = "#jenkins_bot_perth"
            clNumber = changelistNumber
            break
        case "Phoenix_dlc_Build_Machine":
            slackChannel = "#jenkins_bot_phoenix_dlcfe"
            clNumber = changelistNumber
            break
        default:
            slackChannel = "#test_results_steam"
            clNumber = changelistNumber
            break
    }

    def testResult = (testFailed == 0 && testTimeout == 0) ? "Pass" : "Fail"
    def msgHead = "${env.JOB_NAME} finished Result: ${testResult}\n"
    
    if(pipelineName == "Phoenix_balance_set") {
        if (testFailed > 0 && p4_user == "btortosa") {
            def committer = tagUserWhoMadeLastCommit(p4_user)        
            msgHead += "Some balance test failed, have a look at the attached logs <@$committer>"
        }
    }

    StringBuilder sb = new StringBuilder()
    sb.append(" Tests executed on CL:${clNumber}.").append("\n")
    sb.append("- Total Tests: ").append(allData.size()).append("\n")

    def slackResponse
    if (testFailed == 0 && testTimeout == 0) {
        sb.append("All tests passed successfully\n")
        slackResponse = slackSend(color: "#32CD32", message: "${msgHead}${sb.toString()}", channel: "${slackChannel}")
    } else {
        sb.append("- Tests Passed: ").append(testPassed).append("\n")
        sb.append("- Tests Failed: ").append(testFailed).append("\n")
        if (testTimeout > 0) {
            sb.append("- Tests Timeout: ").append(testTimeout).append("\n")
            sb.append("Timeout test: ").append(timeoutTestName.toString()).append("\n")
        }
        sb.append("- Tests failed: ").append(failedTestName.toString()).append("\n")

        def msgThread = sb.toString()
        println msgThread
        println msgHead
        slackResponse = sendMsg(msgHead, msgThread, userHome)
    }

    String filePath = "${env.WORKSPACE}\\logs.zip"
    File file = new File(filePath)
    if (file.exists()) {
        file.delete()
    }
    zip zipFile: 'logs.zip', archive: true, overwrite: false, dir: "${userHome}\\Games\\Age of Empires 2 DE\\testharness\\reports"

    file = new File(filePath)
    if (file.exists()) {
        println "ZIP file created successfully: ${filePath}"
        slackUploadFile(channel: slackResponse.threadId, filePath: 'logs.zip')
    } else {
        println "ZIP file was not created: ${filePath}"
    }
}

def sendMsg(String msg1, String msg2, String userHome) {
    println "SENDING SLACK MESSAGE"
    def slackResponse = slackSend(color: "danger", message: msg1, channel: "${slackChannel}")
    slackSend(channel: slackResponse.threadId, message: msg2)
    return slackResponse
}
