
def call(){
    def isHeapError = false;
    def log = readFile(file: "C:\\Users\\User\\.jenkins\\jobs\\${env.JOB_NAME}\\builds\\${env.BUILD_NUMBER}\\log");
    def heapError = log.contains("internal heap limit reached");
    if (heapError) {
        isHeapError = true;
    }
    checkPreviousBuild("Phoenix_pipeline","daily", isHeapError)
} 