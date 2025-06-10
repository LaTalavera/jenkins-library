import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.logging.Logger

def findLatestJsonFile(String userFolder) {
    def folderPath = "${userFolder}\\Games\\Age of Empires 2 DE\\testharness\\reports"
    def latestFile = null
    try {
        def folder = new File(folderPath)
        if (!folder.exists() || !folder.isDirectory()) {
            echo "The provided path is not a valid directory."
            return null
        }
        def jsonFiles = folder.listFiles().findAll { it.name.startsWith("ResultsSummary") && it.name.endsWith(".json") }
        if (jsonFiles.isEmpty()) {
            echo "No JSON files found in the directory."
            return null
        }
        latestFile = jsonFiles.max { it.lastModified() }
    } catch (Exception e) {
        echo "Error finding latest JSON file: ${e.message}"
    }
    return latestFile
}

def executeCommand(String command) {
    try {
        def process = command.execute()
        def output = new StringBuffer()
        def error = new StringBuffer()
        process.consumeProcessOutput(output, error)
        process.waitFor()
        if (error) {
            echo "Command Error: ${error}"
        }
        return output.toString().trim()
    } catch (Exception e) {
        echo "Error executing command: ${e.message}"
        return null
    }
}

def extractBugNumber(actualMessage) {
    def matcher = actualMessage =~ /Bug (\d+)/
    return matcher.find() ? matcher.group(1) : null
}

def queryWorkItems(String tag = "Assert") {
    echo "Querying workitems for tag '${tag}'..."
    def azPath = "\"C:\\Program Files\\Microsoft SDKs\\Azure\\CLI2\\wbin\\az.cmd\""
    def command = """
        ${azPath} boards query --wiql "SELECT [System.Id], [System.Title], [System.State] FROM WorkItems WHERE [System.TeamProject] = 'Phoenix' AND [System.ChangedDate] > @today - 180 
        AND [System.WorkItemType] = 'Bug' AND [System.Tags] CONTAINS '${tag}'" 
    """
    def output = executeCommand(command)
    if (output) {
        def jsonOutput = new JsonSlurper().parseText(output)
        echo "Queried workitems and received output."
        return jsonOutput
    } else {
        echo "No output for the tag queried."
    }
    return null
}

def createWorkItem(String assertMessage, String assignee, String testName, String clNumber, String stream, String platform, String title) {
    echo "Creating workitem for '${assertMessage}'..."
    def azPath = "\"C:\\Program Files\\Microsoft SDKs\\Azure\\CLI2\\wbin\\az.cmd\""
    def reproSteps = "Assert found while running test: ${testName} in ${platform}"
    def discussion = "Full Assert Message: ${assertMessage}"
    
    // Truncate the title if it exceeds 256 characters
    if (title.length() > 230) {
        title = title.substring(0, 230)
        echo "Title truncated to: ${title}"
    }

    def tags = "Assert;bvt_report;TestHarnessDetected" // Add your additional tag here
    
    def command = """${azPath} boards work-item create --organization https://dev.azure.com/Worlds-Edge --project Phoenix --type Bug --title "[Phoenix][${platform}][Assert]: ${title}" --assigned-to "${assignee}" 
        --discussion "${reproSteps}" --fields "Custom.FoundinBranch=${stream}" "Microsoft.VSTS.Build.FoundIn=${clNumber}" "System.Tags=${tags}" "Microsoft.VSTS.TCM.ReproSteps=${reproSteps}" "Microsoft.VSTS.Common.Severity=3 - Medium" "Microsoft.VSTS.Common.Priority=3" "System.AreaPath=Phoenix\\Forgotten Empires"
    """
    echo "Executing command: ${command}"
    def output = executeCommand(command)
    if (output) {
        echo "Workitem created for '${title}': ${output}"
    } else {
        echo "Failed to create workitem for '${title}'."
    }
}

def generateTitle(String assertMessage) {
    def titleMatcher = assertMessage =~ /FILE='([^']+)'\/(\d+)/
    def title = ""
    if (titleMatcher.find()) {
        title = "${titleMatcher.group(1)}/${titleMatcher.group(2)}"
    }
    
    def messageMatcher = assertMessage =~ /FILE='([^']+)'\/(\d+)\s+(.+?)\s+(C:\\)/
    if (messageMatcher.find()) {
        title = "${messageMatcher.group(1)}/${messageMatcher.group(2)} ${messageMatcher.group(3)}"
    }
    
    if (title.length() > 230) {
        title = title.substring(0, 230)
        echo "Title truncated to: ${title}"
    }
    return title
}

def handleExistingBug(workItems, bugNumber, assertMessage, testName, clNumber, stream, platform) {
    println ("HANDLE EXISTING BUG")
    def workItem = workItems.find { it.fields.'System.Id' == bugNumber.toInteger() }
    if (workItem) {
        def bugNumberStatus = workItem.fields.'System.State'
        if (bugNumberStatus == "New" || bugNumberStatus == "Active") {
            echo "Bug number ${bugNumber} is already in status '${bugNumberStatus}', skipping creation."
            return
        }
    }
    echo "Creating workitem for bug number ${bugNumber}..."
    createWorkItem(assertMessage, '', testName, clNumber, stream, platform, generateTitle(assertMessage)) //check if assigned must be empty or Unassigned
}

def handleNewBug(workItems, assertMessage, testName, clNumber, stream, platform) {
    println("HANDLE NEW BUG")
    def title = generateTitle(assertMessage)
    def workItem = workItems.find { it.fields.'System.Title'.contains(title) }
    if (!workItem) {
        echo "Calling createWorkItem() function..."
        createWorkItem(assertMessage, '', testName, clNumber, stream, platform, title) //check if assigned must be empty or Unassigned
    } else {
        echo "Workitem already exists for title '${title}'."
    }
}

def call(String platform) {
    def userFolder = System.getProperty("user.home")
    def clNumber = null
    if (platform.contains("release")) {
        stream = "//phoenix_stream/release"
        clNumber = readFile(file: 'C:\\Phoenix_release\\changelist.txt').trim()
    } else if (platform.contains("daily")) {
        stream = "//phoenix_stream/daily_dev"
        clNumber = readFile(file: 'C:\\Phoenix_daily\\changelist.txt').trim()
    } else {
        echo "Invalid platform provided."
        return
    }

    if (clNumber == null) {
        echo "CL Number information not found in changelist.txt."
        return
    }

    def jsonFile = findLatestJsonFile(userFolder)
    if (jsonFile == null || !jsonFile.exists()) {
        echo "JSON file 'ResultSummary.json' not found."
        return
    }
    def jsonContent = new JsonSlurper().parseText(jsonFile.text)
    def testResults = jsonContent.results
    echo "Test Results: ${JsonOutput.toJson(testResults)}"
    def workItems = queryWorkItems()
    if (workItems == null) {
        echo "No workitems found."
        return
    }

    def workItemIds = workItems.collect { it.fields.'System.Id' }
    echo "Work Item IDs: ${workItemIds}"
    def uniqueAsserts = new HashSet<String>()

    for (test in testResults) {
        def testName = test.TestName
        def testResult = test.result
        echo "Processing test: ${testName}"
        if (test.actual?.contains("ASSERT")) {
            def assertMessage = test.actual
            println("ASSERT MESSAGE: ${assertMessage}")
            if (uniqueAsserts.add(assertMessage)) {
                def bugNumber = extractBugNumber(assertMessage)
                println "BUG NUMBER: ${bugNumber}"
                if (bugNumber && workItemIds.contains(bugNumber.toInteger())) {
                    handleExistingBug(workItems, bugNumber, assertMessage, testName, clNumber, stream, platform)
                } else {
                    handleNewBug(workItems, assertMessage, testName, clNumber, stream, platform)
                }
            } else {
                echo "Assert message '${assertMessage}' already processed."
            }
        }
    }
}