package pcs.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MergeSortTask extends Task{

	static String FILES_DIR = "./static/mergesortfiles/";
	static int MAX_NUM_EXPERIMS = 25;
	protected boolean leftTask = true;

	public MergeSortTask(String name, TaskType task_type) {
		super(name, task_type);
		// TODO Auto-generated constructor stub
	}

	public MergeSortTask(String name, TaskType task_type, TaskStatus status) {
		super(name, task_type, status);
		// TODO Auto-generated constructor stub
	}
	/*
	 * @name : name of task
	 * @unsortedFilename: url of file to sort
	 */
	public static Task createSortTask(String name, String unsortedFilename) throws IOException {
		Path unsortedFile = Paths.get(unsortedFilename);
		long len_file = Files.lines(unsortedFile).count();

		if (len_file <= MAX_NUM_EXPERIMS) {
			SortTask t = new SortTask(name, len_file, unsortedFile.getFileName().toString(),true);
			return t;
		}else {
			List<Long> splits= getSplits(len_file);
			Queue<String> splitFiles = splitTextFiles(unsortedFile, splits);
			Task t = createTaskTree(name, len_file, splitFiles, true);
			return t;
		}
	}

	/*
	 * @name: name of task
	 * @size: size of file
	 * @splitFiles: queue of split files
	 * Creates a tree to assign the files to be sorted and merged
	 */
	private static Task createTaskTree(String name, long size, Queue<String> splitFiles, boolean isleftTask) {
		if (size > MAX_NUM_EXPERIMS) {
			long s1 = size / 2;
			long s2 = s1 + (size % 2);
			
			Task left = createTaskTree(name, s1, splitFiles, true);
			Task right = createTaskTree(name, s2, splitFiles, false);
			MergeTask t = new MergeTask(name, size, left, right, TaskStatus.WAITING_DEPS, isleftTask);
			left.setParentTask(t);
			right.setParentTask(t);
			return t;

		}else {
			SortTask t = new SortTask(name, size, splitFiles.remove(), isleftTask);
			return t;
		}
	}

	/*
	 * @mergeFile: path of file to be split
	 * @splits: list of file sizes to be split
	 * Creates split files and returns queue with the names of created split files
	 */
	private static Queue<String> splitTextFiles(Path mergeFile, List<Long> splits) throws IOException{
        try(BufferedReader reader = Files.newBufferedReader(mergeFile)){
            String line = null;
            int lineNum = 1, i = 0;

            String filename = String.format(FILES_DIR + "%d_%s", i, mergeFile.getFileName());
            Path splitFile = Paths.get(filename);
            Queue<String> filenames = new LinkedList<String>();
            filenames.add(splitFile.getFileName().toString());
            BufferedWriter writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);

            Long numLinesToread = splits.get(i);
            while ((line = reader.readLine()) != null) {

                if(lineNum > numLinesToread){
                    writer.close();
                    lineNum = 1;
                    i++;
                    filename = String.format(FILES_DIR + "%d_%s", i, mergeFile.getFileName());
                    splitFile = Paths.get(filename);
                    filenames.add(splitFile.getFileName().toString());
                    writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);
                    numLinesToread = splits.get(i);
                }

                writer.append(line);
                writer.newLine();
                lineNum++;
            }

            writer.close();
            return filenames;
        }
	}
	/*
	 * @size: size of file to split
	 * Returns list with size of split files
	 */
	private static List<Long> getSplits(long size) {
		if (size > MAX_NUM_EXPERIMS) {
			long s1 = size / 2;
			long s2 = s1 + (size % 2);
			if (s1 == s2) {
				List<Long> both = getSplits(s1);
				both.addAll(both);
				return both;
			}else {
				List<Long> left = getSplits(s1);
				List<Long> right = getSplits(s2);
				left.addAll(right);
				return left;
			}

		}else {
			List<Long> l = new LinkedList<>();
			l.add(size);
			return l;
		}
	}

	public boolean isleftTask() {
		return leftTask;
	}

	public void setleftTask(boolean isleftTask) {
		this.leftTask = isleftTask;
	}

	@JsonIgnore
	public abstract String getResult();
}
