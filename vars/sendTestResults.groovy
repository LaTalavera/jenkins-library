//This code gets the last modified file in a directory, parses the json file and prints the test name and result.
//Code is used in a Jenkins Pipeline to post the test results in a slack channel

import groovy.json.JsonSlurperClassic 
import com.cloudbees.groovy.cps.NonCPS
import groovy.io.FileType

@NonCPS
Map readData(String reporPath){
    String path = "${reporPath}"
    String jsonFileToRead = new File(path).listFiles().findAll { it.name.startsWith("ResultsSummary") && it.name.endsWith(".json") }
        .sort { -it.lastModified() }?.head()
    def jsonSlurper = new JsonSlurperClassic()
    return jsonSlurper.parse(new File(jsonFileToRead))
}
// Note: Must not be @NonCPS because pipeline steps are called!
def call(String buildMachineName, String reportPath, String p4_user = "test", String changelistNumber = "0"){
    println "Changelist number: ${changelistNumber}"
    def pipelineName = env.JOB_NAME
    Map data = readData(reportPath)
    Map testResults = [:]
    int totalTests = data.results.size()
    int totalFailures = 0
    int totalSuccess = 0
    int totalTimeouts = 0
    String slackChannel = ""
    String clNumber = ""
    String userHome = System.getProperty("user.home")
    String userID = ""

    //Map test names and results from json file
    for (int i = 0; i < data.results.size(); i++) {
        testResults.put(i, [data['results'][i]['TestName'], data['results'][i]['result'], data['results'][i]['expected'], data['results'][i]['actual']])
    }

    // Check build machine name
    switch (buildMachineName) {
        case "test":
            slackChannel = "#bvt_test"
            clNumber = changelistNumber
            break
        case "Steam Nightly":
            slackChannel = "#bvt_results_steam"
            clNumber = changelistNumber
            userID = "76561199151607024"
            break
        case "Steam Release":
            slackChannel = "#bvt_results_steam"
            clNumber = changelistNumber
            userID = "76561199151607024"
            break
        case "MSIXVC":
            slackChannel = "#bvt_results_microsoft"
            clNumber = changelistNumber
            userID = "2814618477733836"
            break
        case "Perth":
            slackChannel = "#bvt_results_perth"
            clNumber = changelistNumber
            break  
        case "Perth_Build_Machine":
            slackChannel = "#jenkins_bot_perth"
            clNumber = changelistNumber
            userID = "2814678134729384"
            break
        case "Phoenix_Build_Machine":
            slackChannel = "#jenkins_bot_phoenix"
            clNumber = changelistNumber
            userID = "76561199081473870"
            break
        case "Phoenix_dlc_Build_Machine":
            slackChannel = "#jenkins_bot_phoenix_dlcfe"
            clNumber = changelistNumber
            userID = "76561199081473870"
            break
        case "Phoenix_dlc_future_Build_Machine":
            slackChannel = "#jenkins_bot_phoenix_dlcfe_future"
            clNumber = changelistNumber
            userID = "76561199081473870"
            break
        default:
            slackChannel = "#test_results_steam"
            clNumber = changelistNumber
            userID = "76561199151607024"
            break
    }

    //Iterate through the map and send to slack test name and results and then a summary of the total tests, failures and success
    for (test in testResults.values()){
        String testName =  test[0]
        String testResult = test[1]
        String expected = test[2]
        String actual = test[3]
        String msgSuccess = "Test: " + testName + ", Result: " + testResult
        String msgFailure = "Test: " + testName + ", Result: " + testResult + "\nExpected result: " + expected + "\nActual result: " + actual

        switch (testResult) {
            case "fail":
                totalFailures++ 
                try {
                    slackSend color: "danger", message: "${msgFailure}", channel: "${slackChannel}"
                } catch (Throwable e) {
                    error "Caught ${e.toString()}" 
                }   
                break
            case "pass":
                totalSuccess++
                try {
                    slackSend color: "good", message: "${msgSuccess}", channel: "${slackChannel}"
                } catch (Throwable e) {
                    error "Caught ${e.toString()}" 
                }            
                break
            default:
                println "Unknown test result: " + testResult
                break
        }
        Thread.sleep(500) // Wait for one second to avoid slack api rate limit
    }
    //Send summary of the test results
    def msgHead ="${env.JOB_NAME} pipeline finished \n "
    def msgThread = "Tests executed in CL: " + "${clNumber}" + "\n Total Tests: " + totalTests + " \n Total Failures: " + totalFailures + " \n Total Success: " + totalSuccess + " \n Total Timeouts: " + totalTimeouts
    def slackResponse = slackSend(color : "#0000ff", message: "${msgHead}", channel: "${slackChannel}")
    slackSend(channel: slackResponse.threadId,  message: "${msgThread}")
    
     //Upload test logs to slack thread
    String logsFilePath = "${env.WORKSPACE}\\test_logs.zip"
    File logs = new File(logsFilePath)
    if (logs.exists()) { 
        logs.delete()
    }  
    zip zipFile: 'test_logs.zip', archive: true, overwrite: false ,dir: "${userHome}\\Games\\Age of Empires 2 DE\\testharness\\reports"
    def uploadResponse = slackUploadFile(channel: slackResponse.threadId, filePath: "test_logs.zip")

    //Upload replays to slack thread
    String replaysFilePath = "${env.WORKSPACE}\\replay_recs.zip"
    File replays = new File(replaysFilePath)
    if (replays.exists()) {
        replays.delete()
    }
    zip zipFile: 'replay_recs.zip', archive: true, overwrite: false ,dir: "${userHome}\\Games\\Age of Empires 2 DE\\${userID}\\savegame"
    def uploadResponse2 = slackUploadFile(channel: slackResponse.threadId, filePath: "replay_recs.zip")

    //TODO enable once the tests are working in console
    // if (totalFailures > 0 && p4_user != "test"){
    //     println "======= DEBUG: p4_user = ${p4_user} =========="
    //     def committer = tagUserWhoMadeLastCommit(p4_user)
    //     slackSend(channel: slackResponse.threadId,  message: "Tests failed after your last commit <@$committer>")
    // }
}
