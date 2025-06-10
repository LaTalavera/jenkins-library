@NonCPS
// We generate a new file with the full content of the confluent page that we want to updated + the new row with our test
// That will be the body of put PUT request
def call(CLmsg){
        def fileList = []
        def content = ""
        
        File contentFile = new File('E:/daily_dev/content.txt')
        content = contentFile.getText("UTF-8")

        int indexEnd = CLmsg.indexOf("]")
        String cleanText = CLmsg.substring(indexEnd +1)
        def file = new File("E:/daily_dev/testNames.txt").eachLine { line ->
            fileList.push("<tr><td><p>${line}</p></td><td><p>${cleanText}</p></td><td><p /></td></tr>")                  
            }
            fileList.each{
            updatedContent = content.replace("</tbody>", "${fileList}</tbody>").replace("[", "").replace("]", "").replace(", ", "")
            }           

        File newTable = new File("E:/daily_dev/newTable.txt")
            newTable.write "${updatedContent}" 

        }