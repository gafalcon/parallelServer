package pcs.models;

public class WorkerNode {

	String url;
	NodeStatus status;
	
	
	public WorkerNode(String url, NodeStatus status) {
		this.url = url;
		this.status = status;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public NodeStatus getStatus() {
		return status;
	}
	public void setStatus(NodeStatus status) {
		this.status = status;
	}
	

}
