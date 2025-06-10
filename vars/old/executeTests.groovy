def call(String test){ 
    def rlAccount = "guest"
    if (test.contains ("host")) {
        rlAccount = "host"
    } 
    Exception caughtException = null
    catchError(buildResult: 'SUCCESS', stageResult: 'ABORTED') { 
    try { 
        env.FILE = test
        bat (returnStatus: true, script: "C:/Phoenix_daily/Game/Tools_Builds/TestHarness.exe --mode comparisionTest --game Phoenix --scripts ${env.FILE}  --multipleCopies --rlAccount ${rlAccount}  --PlatForm Steam")
        sleep time: 2, unit: 'SECONDS'
    } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
        writeFile(file: "${env.FILE}.txt", text: "Test ${env.FILE} has TIMEOUT")
        writeFile(file: "C:\\Users\\Usuario\\Games\\Age of Empires 2 DE\\testharness\\reports\\${env.FILE}.txt", text: "Test ${env.FILE} has TIMEOUT") 
        def readContentUpdated = readFile 'results.txt'
        writeFile file: 'results.txt', text: readContentUpdated + "${env.FILE} \n TIMEOUT" +  "\n"  
        sendSlackNotifcationTimeout(Tests)
        error "Caught ${e.toString()}" 
    } catch (Throwable e) {
        caughtException = e
    }
    }
    if (caughtException) {
        error caughtException.message
    }                
}