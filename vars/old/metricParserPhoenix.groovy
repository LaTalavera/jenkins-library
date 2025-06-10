import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject;
import groovy.io.FileType


class MetricTest{
  String metric_name
  String help
  String type
  List<String> labels
  List<TestValues> values
  @NonCPS
  String getMetric_name() {
    return metric_name
  }
  @NonCPS
  void setMetric_name(String metric_name) {
    this.metric_name = metric_name
  }
  @NonCPS
  String getHelp() {
    return help
  }
  @NonCPS
  void setHelp(String help) {
    this.help = help
  }
  @NonCPS
  String getType() {
    return type
  }
  @NonCPS
  void setType(String type) {
    this.type = type
  }
  @NonCPS
  List<String> getLabels() {
    return labels
  }
  @NonCPS
  void setLabels(List<String> labels) {
    this.labels = labels
  }
  @NonCPS
  List<TestValues> getValues() {
    return this.values
  }
  @NonCPS
  void setValues(List<TestValues> values) {
    this.values = values
  }
}

class TestValues{
  int value
  List<String> labels
  @NonCPS
  int getValue() {
    return value
  }
  @NonCPS
  void setValue(int value) {
    this.value = value
  }
  @NonCPS
  List<String> getLabels() {
    return labels
  }
  @NonCPS
  void setLabels(List<String> labels) {
    this.labels = labels
  }
}


def call(){

  Gson gson = new Gson();

  def jsonFileToRead = findFile()
  String rawData = new File(jsonFileToRead).getText("UTF-8")
  JsonObject json = gson.fromJson(rawData, JsonObject.class);
  JsonArray allData = json.get("results");
  test(allData)

}

@NonCPS
def findFile(){
    String pathSummary = "C:\\Users\\Usuario\\Games\\Age of Empires 2 DE\\testharness\\reports"
    String jsonFileToRead = new File(pathSummary).listFiles().findAll { it.name.endsWith(".json") }
          .sort { -it.lastModified() }?.head()
    return jsonFileToRead
}

@NonCPS
def test(JsonArray allData){
    def pathFormattedMetric = "C:\\Users\\Usuario\\Desktop\\monitoring\\Json_exporter\\json_exporter\\json\\"

    for (JsonObject obj : allData) {
    def testName = obj.get("TestName")?:"";
    def group = obj.get("Group")?:"";
    def result = obj.get("result")?:"";
    def time = obj.get("time")?:""
    def customMetric = 0
    def testFile = new File(pathFormattedMetric + "${testName.asString}.json")
    Gson updateGson = new Gson()
    List<MetricTest> metricTests = new ArrayList<>()

    
    if (testFile.exists()) {
      metricTests = updateGson.fromJson(new FileReader(testFile.absolutePath), List.class);
      println "Result in testFIle: ${result}" 
      MetricTest pos0 = metricTests.get(0)
      MetricTest pos1 = metricTests.get(1)
      MetricTest pos2 = metricTests.get(2)
    
      List<TestValues> values0 = pos0.getValues()
      TestValues testValue0 = values0.get(0)
      testValue0.setValue(customMetric)
      values0.set(0, testValue0)
      pos0.setValues(values0)

      if (result.asString == "pass"){
        List<TestValues> values1 = pos1.getValues()
        TestValues testValue1 = values1.get(0)
        testValue1.setValue(testValue1.getValue() + 1)
        values1.set(0, testValue1)
        pos1.setValues(values1)
      } else {
        List<TestValues> values2 = pos2.getValues()
        TestValues testValue2 = values2.get(0)
        testValue2.setValue(testValue2.getValue() + 1)
        values2.set(0, testValue2)
        pos2.setValues(values2)
      }

      metricTests.set(0, pos0)
      metricTests.set(1, pos1)
      metricTests.set(2, pos2)

    } else {

      MetricTest newTest0 = createNewMetricTest(0, testName.asString, result.toString(), customMetric, group.asString)
      MetricTest newTest1 = createNewMetricTest(1, testName.asString, result.toString(), customMetric, group.asString)
      MetricTest newTest2 = createNewMetricTest(2, testName.asString, result.toString(), customMetric, group.asString)

      metricTests.add(newTest0)
      metricTests.add(newTest1)
      metricTests.add(newTest2)
    }

    Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create()
    //JsonElement jsonTree = gsonBuilder.toJsonTree(metricTests)
    //gsonBuilder.toJson(jsonTree, new FileWriter(testFile.absolutePath));
    String jsonText = gsonBuilder.toJson(metricTests.toArray())
    println testFile
    println jsonText
    testFile.write(jsonText)

    // createNewMetricTest()
    println "debug"
  }
}

  @NonCPS
  def createNewMetricTest(int index, String testName, String result, int customMetric, String group) {
    MetricTest metricTest = new MetricTest()

    if(index == 0){
      println testName
      metricTest.setMetric_name(testName)
      metricTest.setType("gauge")
    } else if (index == 1){
      metricTest.setMetric_name(testName + "_pass_counter")
      metricTest.setType("counter")
    } else if (index == 2){
      metricTest.setMetric_name(testName + "_fail_counter")
      metricTest.setType("counter")
    }
    metricTest.setHelp("test")
      List<String> labelsName = ["test_name", "category"]
    metricTest.setLabels(labelsName)

    TestValues testValues = new TestValues()
    if (index == 0){
      testValues.setValue(customMetric)
    } else if (index == 1 && result == "pass"){
      testValues.setValue(1)
    } else if (index == 1 && result == "fail"){
      testValues.setValue(0)
    }  else if (index == 2 && result == "pass"){
      testValues.setValue(0)
    }  else if (index == 2 && result == "fail"){
      testValues.setValue(1)
    }
    List<TestValues> listTV = new ArrayList<>()
    List<String> testLabelsValue = new ArrayList<>()
    testLabelsValue.add(testName)
    testLabelsValue.add(group)
    testValues.setLabels(testLabelsValue)
    listTV.add(testValues)
    metricTest.setValues(listTV)


    return metricTest
  }
