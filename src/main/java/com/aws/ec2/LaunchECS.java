package com.aws.ec2;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.DescribeTasksResult;
import com.amazonaws.services.ecs.model.Task;
import com.aws.ec2.helper.AwsHelper;

public class LaunchECS {
	private AmazonECSClient amazonECSClient;
	private AmazonEC2Client amazonEC2Client;
	private static AWSCredentials credentials;

	static {
		AwsHelper helper = AwsHelper.getInstance();
		credentials = helper.getAWSCredentials();
	}

	public LaunchECS() {

	}

	public LaunchECS(AmazonECSClient amazonECSClient,
			AmazonEC2Client amazonEC2Client) {
		super();
		this.amazonECSClient = amazonECSClient;
		this.amazonEC2Client = amazonEC2Client;
	}

	public static void main(String a[]) {

		LaunchECS launcher = new LaunchECS(new AmazonECSClient(credentials),
				new AmazonEC2Client(credentials));
		launcher.launch();
	}

	public void launch() {
		AwsHelper helper = AwsHelper.getInstance();
		System.out.println("Creating Task and service");
//		helper.registerTaskAndcreateService(amazonECSClient);
		System.out.println("Configuring and Launching EC2 Instance");
		String instanceid = helper
				.configureAndLaunchEC2Instance(amazonEC2Client);
		System.out.println("EC2 instance launched successfuly - instanceid= "
				+ instanceid);
		/*DescribeTasksRequest descReq=new DescribeTasksRequest();
		descReq.withTasks("selenium-chrome-vnc-task-automated:10");
		DescribeTasksResult result=amazonECSClient.describeTasks(descReq);
		List<Task> tasks=result.getTasks();
		for(Task task:tasks){
			System.out.println(task.getLastStatus());
		}*/
	}
	
	
}
