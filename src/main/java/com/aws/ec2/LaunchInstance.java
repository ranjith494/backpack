package com.aws.ec2;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.amazonaws.services.ecs.model.CreateClusterRequest;
import com.aws.ec2.helper.AwsHelper;

public class LaunchInstance {

	public static void main(String a[]) throws RemoteException, InterruptedException {
		AwsHelper helper = AwsHelper.getInstance();
		AWSCredentials credentials = helper
				.getAWSCredentials();
		AmazonEC2Client amazonEC2Client = new AmazonEC2Client(credentials);
		String instanceid=null;
		String region = null;//"ec2.us-west-2.amazonaws.com";
//		amazonEC2Client.setEndpoint(region);
		try {
			/*helper.createSecurityGroupRequest("JavaSecurityGroup",
					"My security group", amazonEC2Client);

			IpPermission ipPermission = new IpPermission();

			ipPermission.withIpRanges("111.111.111.111/32", "150.150.150.150/32")
					.withIpProtocol("tcp").withFromPort(22).withToPort(22);
			helper.createAuthorizeSecurityGroupIngressRequest("JavaSecurityGroup",
					ipPermission, amazonEC2Client);
			helper.createKeyPairRequest("test-automated", amazonEC2Client);
			IamInstanceProfileSpecification instanceProfileSpecification=new IamInstanceProfileSpecification();
			instanceProfileSpecification.withName("ecsInstanceRole");
			//ami that has ECS Container agent pre installed ami-c16422a4
			instanceid=helper.launchAmi("ami-c16422a4", null, "test-automated", "t2.micro",
					null, null, null, null, null, null, instanceProfileSpecification,amazonEC2Client);
			System.out.println(instanceid);*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		helper.deleteKeyPairRequest("test-automated", amazonEC2Client);
		helper.deleteSecurityGroupRequestWithGroupName("JavaSecurityGroup", null, amazonEC2Client);
		/*List instanctIdList=new ArrayList();
		instanctIdList.add("i-69bccad6");
		helper.stopInstances(instanctIdList, amazonEC2Client);
		helper.terminateInstances(instanctIdList, amazonEC2Client);*/
		}
	}
}
