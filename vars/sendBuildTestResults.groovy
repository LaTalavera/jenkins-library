//This code gets the last modified file in a directory, parses the json file and prints the test name and result.
//Code is used in a Jenkins Pipeline to post the test results in a slack channel

import groovy.json.JsonSlurperClassic 
import com.cloudbees.groovy.cps.NonCPS
import groovy.io.FileType

@NonCPS
Map readData(String reportPath){
    String path = "${reportPath}"
    String jsonFileToRead = new File(path).listFiles().findAll { it.name.endsWith(".json") }
        .sort { -it.lastModified() }?.head()
    def jsonSlurper = new JsonSlurperClassic()
    return jsonSlurper.parse(new File(jsonFileToRead))
}

// Note: Must not be @NonCPS because pipeline steps are called!
def call(String branch, String reportPath){   
    println "Report Path: " + reportPath
    Map data = readData(reportPath)
    Map testResults = [:]
    String msg = ""
    //Map test names and results from json file
    for (int i = 0; i < data.results.size(); i++) {
        testResults.put(i, [data['results'][i]['TestName'], data['results'][i]['result'], data['results'][i]['expected'], data['results'][i]['actual']])
    }

    //Iterate through the map and send to slack test name and results and then a summary of the total tests, failures and success
    for (test in testResults.values()){
        String testName =  test[0]
        String testResult = test[1]
        String expected = test[2]
        String actual = test[3]

        if(testResult == "fail")
        {
            msg = "Test: " + testName + ", Result: " + testResult + "\nExpected result: " + expected + "\nActual result: " + actual
        }
        else if(testResult == "pass")
        {
            msg = "Test: " + testName + ", Result: " + testResult

        }
        else if (testResult == "timeout")
        {
            msg = "Test: " + testName + ", Result: " + testResult
        }
        else
        {
            println "Unknown test result: " + testResult
        }
    }

    if (branch == "daily"){
            try {
                slackSend color : "warning", message: "${msg}", channel: '#test_daily'
            } catch (Throwable e) {
                error "Caught ${e.toString()}" 
            } 
    }
    else if (branch == "dlc_fe"){
            try {
                slackSend color : "warning", message: "${msg}", channel: '#test_harness_logs_dlcfe'
            } catch (Throwable e) {
                error "Caught ${e.toString()}" 
            } 
    }
    else if (branch == "dlc_fe_future") {
            try {
                slackSend color : "warning", message: "${msg}", channel: '#test_harness_logs_dlcfe_future'
            } catch (Throwable e) {
                error "Caught ${e.toString()}" 
            }        
    }
}

