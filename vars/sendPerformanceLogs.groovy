def  getFileSize(file) {
    long fileSizeInBytes = file.length();
    long fileSizeInKB = fileSizeInBytes / 1024;
    long fileSizeInMB = fileSizeInKB / 1024;

    return fileSizeInBytes
}

def checkFileSize(fileSize) {
    if (fileSize > 0) {
        return true
    } else {
        return false
    }
}

def checkAllFilesSize(folder) {
    def files =  new File(folder).listFiles()
    for (file in files) {
        if (file.getName().endsWith(".csv") && file.getName().startsWith("output")) {
            def fileSize = getFileSize(file)
            if (fileSize != null) {
                if (checkFileSize(fileSize) == true) {
                    println "file ${file} size is ${fileSize}"
                    return true
                }
                else {
                    println "file ${file} size is ${fileSize}"
                }
            }
        }
    }
    return false
}
//copy files to a folder
def copyFiles(String fullSrcFilePath) {
    def srcFileName = new File(fullSrcFilePath).name
    def escapedSrcFolder = new File(fullSrcFilePath).getParent().replace('/', '\\\\')
    def destFolderPath = "C:\\performance_logs"
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

//List all output csv files in a folder
def getCsvFiles(folder) {
    def csvFiles =   new File(folder).listFiles().findAll { it.name.endsWith(".csv")  && it.name.startsWith("output")}
    return csvFiles
}

//delete all csv files starting with output within a folder
def deleteOutputFiles(folder) {
    def files =  new File(folder).listFiles()
    for (file in files) {
        if (file.getName().endsWith(".csv") && file.getName().startsWith("output")) {
            file.delete()
        }
    }
}

def call(String path) {
    def userId = ["kwright": "U02AKU8U5RQ"]
    def value = userId["kwright"]
    def anyErrors = checkAllFilesSize(path)
    def csvFiles =  getCsvFiles(path)
    if (anyErrors == true) {
        for (file in csvFiles) {
            if (checkFileSize(getFileSize(file)) == true) {
                copyFiles(file.toString())
            }
        }
        slackSend color : "danger", message: "Performance test failed <@$value>", channel: '#performance_test'
        zip zipFile: 'performance_fail_logs.zip', archive: true, overwrite: true ,dir: 'C:\\performance_logs'
        slackUploadFile channel: "#performance_test",  filePath: "*.zip"
    }
    else {
        slackSend color : "good", message: "Performance test passed", channel: '#performance_test'
    }
    deleteOutputFiles(path)
}