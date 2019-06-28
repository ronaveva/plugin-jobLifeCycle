package test.plugin.jobs;

import java.util.Collection;
import java.util.Map;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.execution.ExecutionLogger;
import com.dtolabs.rundeck.core.execution.JobLifeCycleException;
import com.dtolabs.rundeck.core.jobs.JobLifeCycleEvent;
import com.dtolabs.rundeck.core.jobs.JobLifeCycleStatus;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.jobs.JobLifeCyclePlugin;

@Plugin(service=ServiceNameConstants.JobLifeCycle, name="JobLifeCycleTestPlugin")
@PluginDescription(title="JobLifeCycleTestPlugin title", description="JobLifeCycleTestPlugin description")
public class JobLifeCycleTestPlugin implements JobLifeCyclePlugin{

	@Override
	public JobLifeCycleStatus beforeJobStarts(JobLifeCycleEvent event) throws JobLifeCycleException{
		//This error will be shown in the job log output
		JobLifeCycleStatus JobStatus = new JobLifeCycleStatus(false, "Plugin error description");
		Map<String, Map<String, String>> dataContext = event.getExecutionContext().getDataContext();
		ExecutionLogger executionLogger = event.getExecutionLogger();
		//Right way to log
		//Data context has options values, project name, user name, etc ...
		executionLogger.log(5, dataContext.toString());
		//Job execution id (correlative)
		executionLogger.log(5, event.getExecutionId());
		//User that triggers the job
		executionLogger.log(5, event.getUserName());
		//Options setted up for the job
		executionLogger.log(5, event.getOptions().toString());
		//Here i have obtained the node list for the specific job
		INodeSet nodesSet = event.getExecutionContext().getNodes();
		Collection<INodeEntry> nodesSelected = nodesSet.getNodes();
		
		boolean runExternalCall = runExternalCall(nodesSelected, event.getUserName());
		JobStatus.setSuccessful(runExternalCall);
		return JobStatus;
	}

	//DUMMY METHOD to validate node, username or else. Just an example
	//It will only fail if one of the nodes selected have the attribute "prod"
	private boolean runExternalCall(Collection<INodeEntry> nodesSelected, String userName) throws JobLifeCycleException {
		for (INodeEntry node : nodesSelected) {
			//This will be shown in the jog log output, but is not recommended, just leaved as an example
			System.out.println("Nodename : " + node.getNodename());
			System.out.println(node.getAttributes().get("prod"));
			if (node.getAttributes().get("prod") != null) {
				//If the selected node has the attribute "prod" in the configuration, the job will fail
				throw new JobLifeCycleException("Production node, this user is not allowed to use production environment");
			}
		}
		return true;
	}

	//No implementation required, can be leaved as is, it wont affect the job life cycle
	@Override
	public JobLifeCycleStatus afterJobEnds(JobLifeCycleEvent event) throws JobLifeCycleException {
		// TODO Auto-generated method stub
		return null;
	}

}
