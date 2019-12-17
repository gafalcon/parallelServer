package pcs;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import pcs.models.MergeSortTask;
import pcs.models.MergeTask;
import pcs.models.PITask;
import pcs.models.SortTask;
import pcs.models.Task;
import pcs.models.TaskStatus;
import pcs.models.TaskType;

public class DB {

	static MongoClient mongoClient;
	static MongoDatabase db;
	static MongoCollection<Document> piTasks;
	static MongoCollection<Document> mergeSortTasks;

	public static void initDB() {
		mongoClient = MongoClients.create();
		db = mongoClient.getDatabase("parallel");
		piTasks = db.getCollection("piTasks");
		mergeSortTasks = db.getCollection("mergesortTasks");
	}
	
	public static Document piTaskToDoc(PITask task) {
		Document pidoc = new Document()
				.append("name", task.getName())
				.append("n_experiments", task.getN_experims())
				.append("status", task.getStatus().toString())
				.append("n_success_experims", task.getN_success_experims())
				.append("n_experims_completed", task.getN_experims_completed());

		List<Document> subTasks = new ArrayList<>();
		for (Task subtask : task.getSubtasks()) {
			PITask subpitask = (PITask) subtask;
			subTasks.add(piTaskToDoc(subpitask));
		}
		pidoc.append("subtasks", subTasks);
		return pidoc;
	}
	
	public static Document mergeSortTaskToDoc(MergeSortTask t) {
		Document msdoc = new Document();
		msdoc.append("name", t.getName());
		msdoc.append("status", t.getStatus().toString());
		msdoc.append("type", t.getTask_type().toString());
		msdoc.append("fileSize", t.getFileSize());
		msdoc.append("isleft", t.isleftTask());
		if (t instanceof MergeTask) {
			MergeTask mt = (MergeTask) t;
			msdoc.append("leftFile", mt.getLeftFile());
			msdoc.append("rightFile", mt.getRightFile());
			msdoc.append("mergedFile", mt.getMergedFile());
			
		} else {
			SortTask st = (SortTask) t;
			msdoc.append("unsortedFile", st.getUnsortedFile());
			msdoc.append("sortedFile", st.getSortedFile());
		}
		
		List<Document> subTasks = new ArrayList<>();
		for (Task subtask: t.getSubtasks()) {
			MergeSortTask mst = (MergeSortTask) subtask;
			subTasks.add(mergeSortTaskToDoc(mst));
		}
		msdoc.append("subtasks", subTasks);
		return msdoc;
	}
	
	public static void newPITask(PITask task) {
		Document pidoc = piTaskToDoc(task);
		piTasks.insertOne(pidoc);
		System.out.println("new pi task:"+ pidoc.getObjectId("_id"));
		task._id = pidoc.get("_id").toString();
	}
	
	public static void newMergeSortTask(MergeSortTask task) {
		Document msdoc = mergeSortTaskToDoc(task);
		mergeSortTasks.insertOne(msdoc);
		System.out.println("new merge sort task");
		task._id = msdoc.get("_id").toString();
	}
	
	public static PITask docToPITask(Document d) {
		PITask t = new PITask(d.getString("name"));
		t.setN_experims(d.getLong("n_experiments"));
		t.setN_experims_completed(d.getLong("n_experims_completed"));
		t.setN_success_experims(d.getLong("n_success_experims"));
		t.setStatus(TaskStatus.valueOf(d.getString("status")));
		List<Document> l = d.getList("subtasks", Document.class);
		ArrayList<Task>	subtasks = new ArrayList<Task>();
		for (Document document : l) {
			PITask st = docToPITask(document);
			subtasks.add(st);
			st.setParentTask(t);
		}
		t.setSubtasks(subtasks);
		return t;
	}
	
	public static MergeSortTask docToMergeSortTask(Document d) {
		if (d.getString("type").equals("MERGE")) {
			MergeTask t = new MergeTask(d.getString("name"), TaskType.MERGE);
			t.setFileSize(d.getLong("fileSize"));
			t.setLeftFile(d.getString("leftFile"));
			t.setRightFile(d.getString("rightFile"));
			t.setMergedFile(d.getString("mergedFile"));
			t.setStatus(TaskStatus.valueOf(d.getString("status")));
			t.setleftTask(d.getBoolean("isleft", true));
			List<Document> l = d.getList("subtasks", Document.class);
			ArrayList<Task>	subtasks = new ArrayList<Task>();
			for (Document document : l) {
				MergeSortTask st = docToMergeSortTask(document);
				subtasks.add(st);
				st.setParentTask(t);
			}
			t.setSubtasks(subtasks);
			return t;
		}else {
			SortTask t = new SortTask(d.getString("name"), TaskType.SORT);
			t.setStatus(TaskStatus.valueOf(d.getString("status")));
			t.setFileSize(d.getLong("fileSize"));
			t.setSortedFile(d.getString("sortedFile"));
			t.setUnsortedFile(d.getString("unsortedFile"));
			t.setleftTask(d.getBoolean("isleft", true));
			return t;
		}
		
	}
	public static List<Task> getPITasks(boolean completed) {
		System.out.println("get pi tasks");
		ArrayList<Task> tasks = new ArrayList<Task>();
		Bson filter;
		if (completed) {
			filter = eq("status", "FINISHED");
		}else {
			filter = ne("status", "FINISHED");
		}
		piTasks.find(filter).forEach((Document d) -> {
			System.out.println(d);
			PITask t = docToPITask(d);
			t._id = d.get("_id").toString();
			tasks.add(t);
		});
		return tasks;
	}
	
	public static List<Task> getMergeSortTasks(boolean completed) {
		System.out.println("get mergesort tasks");
		ArrayList<Task> tasks = new ArrayList<Task>();
		Bson filter;
		if (completed) {
			filter = eq("status", "FINISHED");
		}else {
			filter = ne("status", "FINISHED");
		}
		mergeSortTasks.find(filter).forEach((Document d) -> {
			System.out.println(d);
			MergeSortTask t = docToMergeSortTask(d);
			t._id = d.get("_id").toString();
			tasks.add(t);
		});
		return tasks;
	}

	public static void updatePITask(PITask t) {
		piTasks.replaceOne(eq("_id", new ObjectId(t._id)), 
				piTaskToDoc(t));
	}

	public static void updateMergeSortTask(MergeSortTask t) {
		MergeSortTask toUpdate = (MergeSortTask) t.head();
		mergeSortTasks.replaceOne(eq("_id", new ObjectId(toUpdate._id)), 
				mergeSortTaskToDoc(toUpdate));
	}
	
	
}
