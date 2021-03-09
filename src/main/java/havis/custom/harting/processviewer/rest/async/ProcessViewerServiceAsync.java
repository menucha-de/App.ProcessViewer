package havis.custom.harting.processviewer.rest.async;

import havis.custom.harting.processviewer.Configuration;
import havis.custom.harting.processviewer.HistoryEntry;
import havis.custom.harting.processviewer.ReadPoint;
import havis.custom.harting.processviewer.Task;
import havis.net.rest.shared.data.SerializableValue;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../rest/processviewer")
public interface ProcessViewerServiceAsync extends RestService {

	@GET
	@Path("tasks/history")
	public void getHistorySize(MethodCallback<SerializableValue<Integer>> callback);

	@GET
	@Path("tasks/history/{limit}/{offset}")
	public void getHistory(@PathParam("limit") int limit, @PathParam("offset") int offset, MethodCallback<List<HistoryEntry>> callback);

	@GET
	@Path("tasks/history/{limit}/{offset}")
	public void getHistory(@PathParam("limit") int limit, @PathParam("offset") int offset, @QueryParam("epc") String epc,
			MethodCallback<List<HistoryEntry>> callback);

	@DELETE
	@Path("tasks/history")
	public void deleteHistory(MethodCallback<Void> result);

	@GET
	@Path("tasks")
	public void getTasks(MethodCallback<Configuration> tasks);

	@POST
	@Path("tasks")
	public void addTask(Task task, MethodCallback<Void> result);

	@POST
	@Path("tasks/{name}/readpoints")
	public void addReadPoint(@PathParam("name") String name, ReadPoint readPoint, MethodCallback<Void> result);

	@DELETE
	@Path("tasks/{name}")
	public void deleteTask(@PathParam("name") String name, MethodCallback<Void> result);

	@PUT
	@Path("tasks/{name}")
	public void updateTask(@PathParam("name") String name, Task task, MethodCallback<Void> result);

	@PUT
	@Path("tasks/{task}/readpoints/{name}")
	public void updateReadPoint(@PathParam("task") String task, @PathParam("name") String name, ReadPoint readPoint, MethodCallback<Void> result);

	@DELETE
	@Path("tasks/{task}/readpoints/{name}")
	public void deleteReadPoint(@PathParam("task") String task, @PathParam("name") String name, MethodCallback<Void> result);

	@GET
	@Path("tasks/{name}/icon.png")
	public void getImage(@PathParam("name") String task, MethodCallback<SerializableValue<String>> result);

	@POST
	@Path("tasks/{name}/icon.png")
	public void setImage(@PathParam("name") String name, SerializableValue<String> url, MethodCallback<Void> result);

}
