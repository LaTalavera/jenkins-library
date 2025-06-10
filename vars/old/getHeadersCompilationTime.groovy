def call(){
    def deltaData = new File("E:/daily_dev/CompilationTimes.txt").readLines() 
    def firstMsg = deltaData[0..-1].join('\n')
    return firstMsg
}