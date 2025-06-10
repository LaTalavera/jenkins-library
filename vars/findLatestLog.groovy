@NonCPS
def call(String reports_path) {
    def dir = new File(reports_path)
    def files = dir.listFiles()
    def logFiles = []  
    files.each {
        if (it.name.endsWith('.txt')) {
            logFiles.add(it)
        }
    }
    if (logFiles.isEmpty()) {
        return null  // Handle the case where no log files are found
    }
    def newestLogFile = logFiles.sort { it.lastModified() }.last()
    return newestLogFile.getName()  // Ensure this returns a string
}