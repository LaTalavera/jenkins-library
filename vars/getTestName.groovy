@NonCPS
def call(){
    try {
        def file = new File("${env.HUDSON_CHANGELOG_FILE}")
            def searchString = ".cts"
            def addString = "ADD"
            def newFileList = []
            String fileName = ""
            file.eachLine{line ->
                if (line.contains(searchString) && line.contains(addString)) {
                    println "Current line contains ${searchString} and ${addString}"
                    line = line.replaceAll(".+%2F", "").replaceAll("scripts%", "");
                    fileName = line.split(".cts")[0]
                    File testsNamesFile = new File("E:/daily_dev/testNames.txt")
                    testsNamesFile.append "${fileName}\n"
                    newFileList.push(fileName)
                }
            }
    }
    catch(Exception e){ println "There is no new tests added"}
}