package test.plugin.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.JobPluginException;
import com.dtolabs.rundeck.core.jobs.JobEventStatus;
import com.dtolabs.rundeck.core.jobs.JobOption;
import com.dtolabs.rundeck.core.jobs.JobPersistEvent;
import com.dtolabs.rundeck.core.jobs.JobPreExecutionEvent;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.jobs.JobOptionImpl;
import com.dtolabs.rundeck.plugins.jobs.JobPlugin;

@Plugin(service=ServiceNameConstants.JobPlugin, name="JobPluginExample")
@PluginDescription(title="JobPluginExample title", description="JobPluginExample description")
public class JobPluginExample implements JobPlugin{

private static final String ITSM_OPTION_NAME = "itsmTicket";
	

	//Custom implementation of interface JobEventStatus
	class JobEventStatusImpl implements JobEventStatus{

		private boolean successful = true;
		private String description;
		private boolean useNewValues = false;
		private Map optionValues = new HashMap<String, String>();
		private SortedSet<JobOption> options = new TreeSet<JobOption>();
		
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

		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public boolean isSuccessful() {
			return this.successful;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public boolean useNewValues() {
			return this.useNewValues;
		}

		@Override
		public Map getOptionsValues() {
			return this.optionValues;
		}
		
	}
	
	//It checks whether itsmTicket option exist, if so, it will check whether it has a value settep up or not
	//if it has no value, it will make the execution to stop.
	@Override
	public JobEventStatus beforeJobExecution(JobPreExecutionEvent event) throws JobPluginException {
		JobEventStatusImpl result = new JobEventStatusImpl();
		boolean checkNodes = false;
		result.setUseNewValues(true);
		for(JobOption option : event.getOptions()) {
			if(option.getName().equals(ITSM_OPTION_NAME)) {
				result.getOptionsValues().put(option.getName(), event.getOptionsValues().get(ITSM_OPTION_NAME));
				if(!event.getOptionsValues().containsKey(ITSM_OPTION_NAME) || ((String)event.getOptionsValues().get(ITSM_OPTION_NAME)).trim().isEmpty()) {
					result.setDescription(ITSM_OPTION_NAME + " is not valid");
					result.setSuccessful(false);
				}
			}else {
				result.getOptionsValues().put(option.getName(),"new value for other options");
			}
		}
		return result;
	}

	//It check whether a node has the prod tag, if so, it adds the itsmTicket option
	//if not, it will remove he option if it is found
	@Override
	public JobEventStatus beforeSaveJob(JobPersistEvent event) throws JobPluginException {
		JobEventStatusImpl result = new JobEventStatusImpl();
		boolean hasItsmOption = false;
		boolean addItsmOption = false;
		
		for (INodeEntry nodeEntry : event.getNodes()) {
			if(nodeEntry.getAttributes().containsKey("tags")) {
				String[] tags = nodeEntry.getAttributes().get("tags").split(",");
				for(String tag : tags) {
					if(tag.trim().equals("prod")) {
						addItsmOption = true;
					}
				}
			}
		}
		
		if(event.getOptions() != null) {
			for (JobOption option : event.getOptions()) {
				if(!option.getName().equals(ITSM_OPTION_NAME)) {
					result.getOptions().add(option);
				} else if(option.getName().equals(ITSM_OPTION_NAME) && addItsmOption) {
					result.getOptions().add(option);
					hasItsmOption = true;
					addItsmOption = false;
				}else if(option.getName().equals(ITSM_OPTION_NAME) && !addItsmOption) {
					hasItsmOption = true;
					addItsmOption = false;
				}
			}	
		}
		if(addItsmOption && !hasItsmOption) {
			JobOptionImpl option = new JobOptionImpl();
			option.setName(ITSM_OPTION_NAME);
			option.setRequired(true);
			option.setSecureInput(true);
			option.setSecureExposed(true);
			option.setEnforced(false);
			result.getOptions().add(option);
			result.setSuccessful(true);
			result.setUseNewValues(true);
		}else if(hasItsmOption && !addItsmOption) {
			result.setSuccessful(true);
			result.setUseNewValues(true);
		}
		
		return result;
	}	

}
