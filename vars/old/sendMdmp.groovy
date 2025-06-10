def call(String dirPath){
    def mdmpFile = findLastMdmpFile(dirPath)
    def txtFiles = findLastTxtFiles(dirPath)
    println "estoy aqui"
    copyFiles(mdmpFile)
    copyFiles("C:\\Phoenix_daily\\Game\\AoE2DE_s.exe")
    copyFiles("C:\\Phoenix_daily\\Game\\AoE2DE_s.pdb")
    copyFiles(txtFiles[0])
    copyFiles(txtFiles[1])
    copyFiles(txtFiles[2])
    slackSend color : "danger", message: "Game crashed while running crash_detect test", channel: '#crash_logs'
    zip zipFile: 'crash.zip', archive: true, overwrite: true ,dir: 'C:\\crash'
    slackUploadFile channel: "#crash_logs",  filePath: "*.zip"
}

//find last modified .mdmp file in a folder
@NonCPS
def findLastMdmpFile(String dirPath){
    String mdmpFile = new File(dirPath).listFiles().findAll { it.name.endsWith(".mdmp") }
        .sort { -it.lastModified() }?.head()    
    return mdmpFile
}

//find last 3 modified .txt files in a folder
@NonCPS
def findLastTxtFiles(String dirPath){
    def allTxtFiles = []
    String txtFiles = new File(dirPath).listFiles().findAll { it.name.endsWith(".txt") }
        .sort { -it.lastModified() }?.take(3)
    txtFiles = txtFiles.substring(txtFiles.indexOf("[") + 1);
    txtFiles = txtFiles.substring(0, txtFiles.indexOf("]"));
    txtFiles.split(", ").each { allTxtFiles.add(it) }
    return allTxtFiles
}

//copy files to a folder
def copyFiles(String fullSrcFilePath) {
    def srcFileName = new File(fullSrcFilePath).name
    def escapedSrcFolder = new File(fullSrcFilePath).getParent().replace('/', '\\\\')
    def destFolderPath = "C:\\crash"
    def escapedDestFolderPath = destFolderPath.replace('/', '\\\\')
    println "Copying file ${srcFileName} from ${escapedSrcFolder} to ${escapedDestFolderPath}"
    dir(escapedSrcFolder) {
        fileOperations([
            fileCopyOperation(
                flattenFiles: true,
                includes: srcFileName,
                targetLocation: escapedDestFolderPath
            )
        ])
    }
}

