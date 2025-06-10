def call(int numberOfLines, ArrayList linesToWrite){
    if(numberOfLines == 6){
        linesToWrite.eachWithIndex {it, index ->
            if(index == 0  || index == 5){
                File resultsFile = new File("C:\\Users\\Usuario\\.jenkins\\workspace\\bvt_test\\results.txt");
                resultsFile << it;
                resultsFile << "\n";  
            }
        }
    }

    if(numberOfLines == 8){
        linesToWrite.eachWithIndex {it, index ->
            if(index == 0 || index == 5){
                File resultsFile = new File("C:\\Users\\Usuario\\.jenkins\\workspace\\bvt_test\\results.txt");
                resultsFile << it;
                resultsFile << "\n";   
            }
        }
    }
}