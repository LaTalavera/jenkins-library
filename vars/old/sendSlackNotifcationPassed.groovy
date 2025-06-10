def call(Tests) 
{
    def j = 0
    def success = "true"
    def data = readFile(file: "C:\\Users\\Usuario\\Games\\Age of Empires 2 DE\\testharness\\reports\\${FILE}.log")
    def lines = data.readLines()
    def result = lines.findAll { it.contains('SUCCESS') }
    def lastLines = []    
    
    if ( result.toString().contains("SUCCESS") )
    {
        Tests[0] += 1
        for (i = (lines.size()-1); i > (lines.size() -7); i--) {
            if (i != 1 && i !=2) {
                lastLines[j] = lines[i]
                j++
            }
        }     
        sendMessage(lastLines, success)
    }
    else{
        Tests[1] += 1
        for (i = (lines.size()-1); i > (lines.size() -9); i--) {
            if (i != 1 && i !=2) {
                lastLines[j] = lines[i]
                j++
            }
        }
        success = "false"
        sendMessage(lastLines, success)
    }
    return Tests
}