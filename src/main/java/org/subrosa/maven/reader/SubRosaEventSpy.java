package org.subrosa.maven.reader;

import java.io.File;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.model.building.ModelProcessor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component(role = EventSpy.class)
public class SubRosaEventSpy extends AbstractEventSpy {

	@Requirement
	private Logger log;

	@Requirement(role = ModelProcessor.class)
	private SubRosaModelProcessor modelProcessor;

	@Override
	public void onEvent(final Object event) throws Exception {
		if (event instanceof ExecutionEvent) {
			final ExecutionEvent ee = (ExecutionEvent) event;
			final ExecutionEvent.Type type = ee.getType();
			if (type == Type.ProjectDiscoveryStarted) {
				if (ee.getSession() != null) {
					File multiModuleProjectDirectory = ee.getSession().getRequest().getMultiModuleProjectDirectory();
					modelProcessor.init(multiModuleProjectDirectory);
				}
			}
		}
	}
}
