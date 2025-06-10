def call(File fileToPrint, String testResult){
    ArrayList lines = fileToPrint.readLines();
    if (testResult == "failed"){
        getLastLines(lines, 8);    
    }
    else if(testResult == "success"){
        getLastLines(lines, 6);        
    }
    else if (testResult == "timeout"){
        getLastLines(lines, 1);           
    }
}