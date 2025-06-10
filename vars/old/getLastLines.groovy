def call(ArrayList fileLines, int numberOfLines){
    int listSize = fileLines.size();
    int firstLine = listSize - numberOfLines;
    ArrayList listReversedLines = new ArrayList<>();        
    for(int i = listSize;i> firstLine; i--){
        listReversedLines.add(fileLines.get(i-1));
    } 
    ArrayList lastLines = listReversedLines.reverse();
    writeResultsToFile(numberOfLines, lastLines);
}