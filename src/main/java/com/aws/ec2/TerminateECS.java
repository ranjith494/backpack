package com.aws.ec2;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.aws.ec2.helper.AwsHelper;

public class TerminateECS {

	private AmazonECSClient amazonECSClient;
	private AmazonEC2Client amazonEC2Client;
	private static AWSCredentials credentials;
	private static final Logger logger = LogManager
			.getLogger(TerminateECS.class.getName());

	public static void main(String a[]) {
		TerminateECS terminator = new TerminateECS(new AmazonECSClient(
				credentials), new AmazonEC2Client(credentials));
		terminator.terminate();
	}

	static {
		AwsHelper helper = AwsHelper.getInstance();
		credentials = helper.getAWSCredentials();
	}

	public TerminateECS() {

	}

	public TerminateECS(AmazonECSClient amazonECSClient,
			AmazonEC2Client amazonEC2Client) {
		super();
		this.amazonECSClient = amazonECSClient;
		this.amazonEC2Client = amazonEC2Client;
	}

	public void terminate() {
		AwsHelper helper = AwsHelper.getInstance();
		// Stop all Tasks running on ECS Instances
		logger.debug("Stop all Tasks running on ECS Instances");
		helper.stopTask(amazonECSClient);
		// Deregister Task Definition
		helper.deRegisterTaskDefinition("multi-browser-test-task:2",
				amazonECSClient);
		helper.deRegisterTaskDefinition("multi-browser-test-task:1",
				amazonECSClient);
		
		// Deregister all ECS Instances
		helper.deRegisterContainerInstances(amazonECSClient);
		// delete Services
//		helper.deleteServiceRequest("selenium-vnc", amazonECSClient);
		// Stop EC2 Instances
		List<String> instanctIdList = new ArrayList<String>();
		instanctIdList.add("i-663b62b2");
		try {
			helper.stopInstances(instanctIdList, amazonEC2Client);
		} catch (RemoteException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Terminate Ec2 Instances
		logger.debug("Terminate Ec2 Instances");
		helper.terminateInstances(instanctIdList, amazonEC2Client);
		// Delete Security group
		try {
			helper.deleteSecurityGroupRequestWithGroupName("ECSSecurityGroup",
					null, amazonEC2Client);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.warn("Unable to delete. Proceeding!!");
		}
		// Delete Key pair
		helper.deleteKeyPairRequest("myecskeypair", amazonEC2Client);

	}
}
