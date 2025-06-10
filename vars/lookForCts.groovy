@NonCPS
def call() {
    def file = new File("${env.HUDSON_CHANGELOG_FILE}")
    def searchString = ".cts"
    def addString = 'action="ADD"'
    def fileList = []
    String fileName = ""
    file.eachLine {
        line -> if (line.contains(searchString) && line.contains(addString)) {
            println "line: " + line
            line = line.replaceAll(".+%2F", "").replaceAll("scripts%", ""); 
            println "line: " + line
            fileName = line.split(".cts")[0]
            println "fileName: " + fileName
            fileList.push(fileName)
        }
    }
    return fileList
}
