package payloads;

public class TaskRequest {
	private String name;
	private String type;
	private long num_experiments;
	private String sortfile;
	
	public TaskRequest() {
		
	}

	public TaskRequest(String name, String type, long experiments, String sort) {
		this.name = name;
		this.type = type;
		this.setNum_experiments(experiments);
		this.setSortfile(sort);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSortfile() {
		return sortfile;
	}

	public void setSortfile(String sortfile) {
		this.sortfile = sortfile;
	}

	public long getNum_experiments() {
		return num_experiments;
	}

	public void setNum_experiments(long num_experiments) {
		this.num_experiments = num_experiments;
	}

	
}
