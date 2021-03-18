package havis.app.processviewer.rest;

import havis.app.processviewer.Configuration;
import havis.app.processviewer.ConfigurationManagerException;
import havis.app.processviewer.HistoryEntry;
import havis.app.processviewer.HistoryManagerException;
import havis.app.processviewer.ImageManager;
import havis.app.processviewer.ProcessViewerException;
import havis.app.processviewer.ReadPoint;
import havis.app.processviewer.Task;
import havis.app.processviewer.TaskManager;
import havis.net.rest.shared.data.SerializableValue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("processviewer")
public class ProcessViewerService {
	private TaskManager taskManager;
	private ImageManager imgManager;

	public ProcessViewerService(TaskManager main) {
		this.taskManager = main;
		try {
			imgManager = new ImageManager();
		} catch (ConfigurationManagerException e) {
			// ignore
		}

	}

	@PermitAll
	@GET
	@Path("tasks")
	@Produces({ MediaType.APPLICATION_JSON })
	public Configuration getTasks() {
		return taskManager.getTasks();
	}

	@PermitAll
	@POST
	@Path("tasks")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void addTask(Task task) throws ProcessViewerException {
		try {
			taskManager.addTask(task);
		} catch (ConfigurationManagerException e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@GET
	@Path("tasks/{name}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Task getTask(@PathParam("name") String name) {
		return taskManager.getTask(name);
	}

	@PermitAll
	@PUT
	@Path("tasks/{name}")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void updateTask(@PathParam("name") String name, Task task) throws ProcessViewerException {
		try {
			taskManager.updateTask(name, task);
			if (!name.equals(task.getName())) {
				imgManager.removeImage(name);
			}
		} catch (ConfigurationManagerException e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@DELETE
	@Path("tasks/{name}")
	public void deleteTask(@PathParam("name") String name) throws ProcessViewerException {
		try {
			taskManager.deleteTask(name);
			imgManager.removeImage(name);
		} catch (ConfigurationManagerException e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@GET
	@Path("tasks/{name}/readpoints")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<ReadPoint> getReadPoints(@PathParam("name") String name) {
		return taskManager.getReadPoints(name);
	}

	@PermitAll
	@POST
	@Path("tasks/{name}/readpoints")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void addReadPoint(@PathParam("name") String name, ReadPoint readPoint) throws ProcessViewerException {
		try {
			taskManager.addReadpoint(name, readPoint);
		} catch (Exception e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@GET
	@Path("tasks/{task}/readpoints/{name}")
	@Produces({ MediaType.APPLICATION_JSON })
	public ReadPoint getReadPoint(@PathParam("task") String task, @PathParam("name") String name) {
		return taskManager.getReadPoint(task, name);
	}

	@PermitAll
	@PUT
	@Path("tasks/{task}/readpoints/{name}")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void updateReadPoint(@PathParam("task") String task, @PathParam("name") String name, ReadPoint readPoint) throws ProcessViewerException {
		try {
			taskManager.updateReadpoint(task, name, readPoint);
		} catch (Exception e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@DELETE
	@Path("tasks/{task}/readpoints/{name}")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void deleteReadPoint(@PathParam("task") String task, @PathParam("name") String name) throws ProcessViewerException {
		try {
			taskManager.deleteReadPoint(task, name);
		} catch (ConfigurationManagerException e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@DELETE
	@Path("tasks/history")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void deleteHistory() throws ProcessViewerException {
		try {
			taskManager.deleteHistory();
		} catch (HistoryManagerException e) {
			throw new ProcessViewerException(e.getMessage());
		}
	}

	@PermitAll
	@GET
	@Path("tasks/history")
	@Produces({ MediaType.APPLICATION_JSON })
	public SerializableValue<Integer> getHistorySize() throws ProcessViewerException {
		try {
			SerializableValue<Integer> value = new SerializableValue<Integer>(taskManager.getHistorySize());
			return value;
		} catch (HistoryManagerException e) {
			throw new ProcessViewerException("Failed to get history size", e);
		}
	}

	@PermitAll
	@GET
	@Path("tasks/history/{limit}/{offset}")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<HistoryEntry> getHistory(@PathParam("limit") int limit, @PathParam("offset") int offset, @QueryParam("epc") String epc)
			throws ProcessViewerException {
		try {
			if (epc != null) {
				return taskManager.getHistory(limit, offset, epc);
			} else {
				return taskManager.getHistory(limit, offset);
			}
		} catch (HistoryManagerException e) {
			throw new ProcessViewerException("Failed to get history for tag " + epc, e);
		}
	}

	@PermitAll
	@GET
	@Path("tasks/history/export")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportHistory() throws HistoryManagerException {
		StringWriter writer = new StringWriter();
		try {
			taskManager.marshal(writer);
			String filename = String.format("History_%s.txt", new SimpleDateFormat("yyyyMMdd").format(new Date()));
			byte[] data = writer.toString().getBytes(StandardCharsets.UTF_8);
			return Response.ok(data, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
					.header("Content-Type", "text/plain; charset=utf-8").header("Content-Length", data.length).build();
		} catch (SQLException | IOException e) {
			return Response.serverError().build();
		}
	}

	@PermitAll
	@GET
	@Path("tasks/{name}/icon.png")
	@Produces({ MediaType.APPLICATION_JSON })
	public SerializableValue<String> getImage(@PathParam("name") String task) {
		return new SerializableValue<String>(imgManager.getImage(task));
	}

	@PermitAll
	@POST
	@Path("tasks/{name}/icon.png")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void setImage(@PathParam("name") String name, SerializableValue<String> url) throws ProcessViewerException {
		try {
			imgManager.removeImage(name);
			imgManager.addImage(name, url.getValue());
		} catch (ConfigurationManagerException e) {
			throw new ProcessViewerException(e);
		}
	}

}