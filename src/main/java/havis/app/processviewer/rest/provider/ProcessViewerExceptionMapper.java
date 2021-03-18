package havis.app.processviewer.rest.provider;

import havis.app.processviewer.ProcessViewerException;
import havis.net.rest.shared.data.SerializableValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ProcessViewerExceptionMapper implements ExceptionMapper<ProcessViewerException> {

	@Override
	public Response toResponse(ProcessViewerException e) {
		return Response.status(Response.Status.BAD_REQUEST).entity(new SerializableValue<String>(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
	}
}