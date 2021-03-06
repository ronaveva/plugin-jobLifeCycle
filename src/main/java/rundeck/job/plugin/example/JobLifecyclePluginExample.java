package rundeck.job.plugin.example;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.jobs.JobLifecycleStatus;
import com.dtolabs.rundeck.core.jobs.JobOption;
import com.dtolabs.rundeck.core.jobs.JobPersistEvent;
import com.dtolabs.rundeck.core.jobs.JobPreExecutionEvent;
import com.dtolabs.rundeck.core.plugins.JobLifecyclePluginException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.jobs.JobOptionImpl;
import com.dtolabs.rundeck.plugins.jobs.JobOptionImpl.JobOptionImplBuilder;
import com.dtolabs.rundeck.plugins.project.JobLifecyclePlugin;


@Plugin(service=ServiceNameConstants.JobLifecycle, name="JobPluginExample")
@PluginDescription(title="JobPluginExample title", description="JobPluginExample description")
public class JobLifecyclePluginExample implements JobLifecyclePlugin{

	private static final String EXAMPLE_OPTION_NAME = "exampleOptionName";
	
	class JobEventStatusImpl implements JobLifecycleStatus {

		private boolean successful = true;
		private boolean useNewValues = false;
		private Map optionValues = new HashMap<String, String>();
		private SortedSet<JobOption> options = new TreeSet<JobOption>();
		private String errorDescription;
		
		@Override
		public SortedSet<JobOption> getOptions() {
			return options;
		}

		public boolean isUseNewValues() {
			return useNewValues;
		}

		public void setUseNewValues(boolean useNewValues) {
			this.useNewValues = useNewValues;
		}

		public void setSuccessful(boolean successful) {
			this.successful = successful;
		}

		public void setErrorMessage(String errorDescription) {
			this.errorDescription = errorDescription;
		}

		@Override
		public boolean isSuccessful() {
			return this.successful;
		}

		@Override
		public Map getOptionsValues() {
			return this.optionValues;
		}
		@Override
		public String getErrorMessage() {
			return this.errorDescription;
		}
		
	}
	
	//It triggers before the job execution
	//It can prevent the job for being executed and it can also change the options values
	@Override
	public JobLifecycleStatus beforeJobExecution(JobPreExecutionEvent event) throws JobLifecyclePluginException {
		JobEventStatusImpl result = new JobEventStatusImpl();
		for(INodeEntry node :event.getNodes().getNodes()) {
			System.out.println(node.getTags());
			Map<String, String> attributes = node.getAttributes();
			for(String key : attributes.keySet()) { 
				System.out.println(attributes.get(key));
			}
		}
		//it indicates that new options values should be used
		result.setUseNewValues(true);
		//iterates over the job options
		if(event.getOptions() != null) {
			for(JobOption option : event.getOptions()) {
				//It checks if the job have the option "exampleOptionName", if exist, it checks for it to have a value
				if(option.getName().equals(EXAMPLE_OPTION_NAME)) {
					result.getOptionsValues().put(option.getName(), event.getOptionsValues().get(EXAMPLE_OPTION_NAME));
					if(!event.getOptionsValues().containsKey(EXAMPLE_OPTION_NAME) || ((String)event.getOptionsValues().get(EXAMPLE_OPTION_NAME)).trim().isEmpty()) {
						//It indicates that the job should not start, since the "exampleOptionName" does not have a value
						result.setErrorMessage(EXAMPLE_OPTION_NAME + " must have a value");
						result.setSuccessful(false);
					}
				}else {
					//This will change the value of every other option
					result.getOptionsValues().put(option.getName(), "new value for other options");
				}
			}
		}
		return result;
	}
	
	//It runs before a job is persisted
	//It can add or remove job options
	//In this example, we will always add the "exampleOptionName" if it does not exist in the job
	@Override
	public JobLifecycleStatus beforeSaveJob(JobPersistEvent event) throws JobLifecyclePluginException {
		
		//It returns the node filter for the job
		//event.getNodeFilter(); 
		//It returns the nodes obtained by the node filter
		//event.getNodes();
		JobEventStatusImpl result = new JobEventStatusImpl();
		boolean addExampleOption = true;
		
		//Iterates over job options and adds every option to the result
		//if an option is not added to the result it will be removed from the job
		if(event.getOptions() != null) {
			for (JobOption option : event.getOptions()) {
				//add the option to the result
				result.getOptions().add(option);
				//checking whether the option already exist
				if(option.getName().equals(EXAMPLE_OPTION_NAME)) {
					addExampleOption = false;
				}
			}
		}
		
		//Creates a new job option and adds it to the result
		if(addExampleOption) {
			JobOptionImplBuilder builder = JobOptionImpl.builder();
			//Option name
			builder.name(EXAMPLE_OPTION_NAME);
			//It indicates if the option is mandatory
			builder.required(true);
			//Security level
			builder.secureInput(true);
			builder.secureExposed(true);
			//It indicates if the option is an enforced value to be picked from a list
			builder.enforced(false);
			//add the option to the result
			result.getOptions().add(builder.build());
			//It indicates that the process finished successfully
			result.setSuccessful(true);
			//it indicates that the new values should be used
			result.setUseNewValues(true);
		}
		
		return result;
	}	
}