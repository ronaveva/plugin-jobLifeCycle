package test.plugin.jobs;

import java.util.Map;

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
		JobLifeCycleStatus JobStatus = new JobLifeCycleStatus(false, "Plugin error description");
		Map<String, Map<String, String>> dataContext = event.getExecutionContext().getDataContext();
		ExecutionLogger executionLogger = event.getExecutionContext().getExecutionLogger();
		executionLogger.log(5, dataContext.toString());
		return JobStatus;
	}

	@Override
	public JobLifeCycleStatus afterJobEnds(JobLifeCycleEvent event) throws JobLifeCycleException {
		JobLifeCycleStatus JobStatus = new JobLifeCycleStatus(false, "Plugin error description afterJobEnds");
		return JobStatus;
	}

}
