//reserve a jenkins job in the queue
def job = Jenkins.instance.queue.schedule2(Jenkins.instance.getItemByFullName("test"), 0).getItem()
//wait until the job is running
while (job.isBuilding() == false) {
    sleep 1000
}
//get the executor
def executor = job.getAssignedExecutable().getOwner()
//get the computer
def computer = executor.getOwner()
//get the node
def node = computer.getNode()
//get the node name
def nodeName = node.getNodeName()
//get the node label
def nodeLabel = node.getLabelString()
//get the node description
def nodeDescription = node.getNodeDescription()
//get the node number of executors
def nodeNumExecutors = node.getNumExecutors()

