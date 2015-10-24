package com.aws.ec2.helper;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.CreateServiceRequest;
import com.amazonaws.services.ecs.model.DeleteServiceRequest;
import com.amazonaws.services.ecs.model.DeregisterContainerInstanceRequest;
import com.amazonaws.services.ecs.model.DeregisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.ListContainerInstancesResult;
import com.amazonaws.services.ecs.model.ListTasksResult;
import com.amazonaws.services.ecs.model.MountPoint;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionResult;
import com.amazonaws.services.ecs.model.ServiceNotActiveException;
import com.amazonaws.services.ecs.model.StopTaskRequest;
import com.amazonaws.services.ecs.model.UpdateServiceRequest;
import com.amazonaws.services.ecs.model.Volume;
import com.amazonaws.util.Base64;
import com.aws.ec2.TerminateECS;
import com.aws.ec2.builder.RequestBuilder;
import com.aws.ec2.builder.SeleniumTaskDefinitionRequestBuilder;

public class AwsHelper {
	
	private static AwsHelper instance;
	private static final Logger logger = LogManager.getLogger(AwsHelper.class.getName());
	
	private AwsHelper(){
		
	}
	
	public static AwsHelper getInstance(){
		if(instance==null){
			instance=new AwsHelper();
		}
		return instance;
	}

	public AWSCredentials getAWSCredentials(){
		AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		
		return credentials;
	}
	
	public String createSecurityGroupRequest(String securityGroupName,
			String description, AmazonEC2Client amazonEC2Client) {
		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
		csgr.withGroupName(securityGroupName).withDescription(description);
		CreateSecurityGroupResult result = amazonEC2Client
				.createSecurityGroup(csgr);
		if (result != null) {
			return result.getGroupId();
		} else
			return "";
	}
	
	public void deleteSecurityGroupRequestWithGroupName(String securityGroupName, String description,AmazonEC2Client amazonEC2Client){
		DeleteSecurityGroupRequest dsgr = new DeleteSecurityGroupRequest();    	
		dsgr.withGroupName(securityGroupName);
		amazonEC2Client.deleteSecurityGroup(dsgr);
	}
	
	public void deleteSecurityGroupRequestWithGroupId(String securityGroupId, String description,AmazonEC2Client amazonEC2Client){
		DeleteSecurityGroupRequest dsgr = new DeleteSecurityGroupRequest();    	
		dsgr.withGroupId(securityGroupId);
		amazonEC2Client.deleteSecurityGroup(dsgr);
	}
	
	public void createAuthorizeSecurityGroupIngressRequest(String name,IpPermission ipPermission,AmazonEC2Client amazonEC2Client){
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				new AuthorizeSecurityGroupIngressRequest();
			authorizeSecurityGroupIngressRequest.withGroupName(name).withIpPermissions(ipPermission);
			amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
	}
	
	public String createKeyPairRequest(String name,AmazonEC2Client amazonEC2Client){
		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
		createKeyPairRequest.withKeyName(name);
		CreateKeyPairResult result=amazonEC2Client.createKeyPair(createKeyPairRequest);
		if (result != null) {
			return result.getKeyPair().getKeyName();
		} else
			return "";
	}
	public void deleteKeyPairRequest(String name,AmazonEC2Client amazonEC2Client){
		DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest();
		deleteKeyPairRequest.withKeyName(name);
		amazonEC2Client.deleteKeyPair(deleteKeyPairRequest);
	}
	
	public void waitForState(String instanceId, String expectedState, int timeout, AmazonEC2Client ec2Client) 
		    throws RemoteException, InterruptedException { 
		        long pollInterval = 5000L; 
		        long timeoutInterval = timeout * 60L * 1000L; 
		        long start = System.currentTimeMillis(); 
		 
		        String state = expectedState+"a"; 
		 
		        while (!state.equals(expectedState)) { 
		            if (System.currentTimeMillis() - start > timeoutInterval) { 
		                throw new RemoteException( 
		                        "Timeout while waiting for instance to hit " + expectedState + " state."); 
		            } 
		            Instance instance = getInstanceById(instanceId, ec2Client); 
		            if (instance != null) { 
		                state = instance.getState().getName(); 
		                if (instance.getStateReason() != null && 
		                        "Server.InternalError".equals(instance.getStateReason().getCode())) { 
		                    throw new RemoteException(instance.getStateReason().getMessage()); 
		                } 
		            } 
		            // check to see if the instance failed at startup 
		 
		            Thread.sleep(pollInterval); 
		        } 
		    } 
	
	 public Instance getInstanceById(String instanceId, AmazonEC2 ec2Client) { 
	        List<String> ids = new ArrayList<String>(); 
	        ids.add(instanceId); 
	        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(ids); 
	        DescribeInstancesResult result = ec2Client.describeInstances(request); 
	 
	        Instance instance = null; 
	        List<Reservation> reservations = result.getReservations(); 
	 
	        if (reservations != null && !reservations.isEmpty()) { 
	            // there should only be 1 reservation and 1 instance 
	            instance = reservations.get(0).getInstances().get(0); 
	        } 
	        else { 
	        } 
	 
	        return instance; 
	    } 
	 
	 public void terminateInstances(List<String> instanceIds, AmazonEC2Client ec2Client) { 
	        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceIds); 
	        ec2Client.terminateInstances(request); 
	    } 
	 
	    public void stopInstances(List<String> instanceIds, AmazonEC2Client ec2Client) 
	    throws RemoteException, InterruptedException { 
	        StopInstancesRequest stopRequest = new StopInstancesRequest(instanceIds); 
	        ec2Client.stopInstances(stopRequest); 
	        for (String instanceId : instanceIds) { 
	            waitForState(instanceId, "stopped", 8, ec2Client); 
	        } 
	    } 
	 
	    public void startInstances(List<String> instanceIds, AmazonEC2Client ec2Client) 
	    throws RemoteException, InterruptedException { 
	        StartInstancesRequest startRequest = new StartInstancesRequest(instanceIds); 
	        ec2Client.startInstances(startRequest); 
	        for (String instanceId : instanceIds) { 
	            waitForState(instanceId, "running", 8, ec2Client); 
	        } 
	    } 
	    
	    public String launchAmi(String amiId, String subnetId, String keyPair, String instanceType, 
	            String userData, List<String> groups, List<BlockDeviceMapping> blockMaps, 
	            String ariId, String akiId, String zone, IamInstanceProfileSpecification iamInstanceProfile, AmazonEC2Client ec2Client) {
	        String instanceId = null; 
	        RunInstancesRequest request =  new RunInstancesRequest() 
	                                            .withImageId(amiId) 
	                                            .withMinCount(1) 
	                                            .withMaxCount(1).withIamInstanceProfile(iamInstanceProfile); 
	        if (subnetId != null && !subnetId.isEmpty()) { 
	            // launch in VPC 
	            request = request.withSubnetId(subnetId); 
	        } 
	        else if (zone != null && !zone.isEmpty()) { 
	            // launch in EC2 
	            // TODO - check for valid zones 
	            Placement placement = new Placement().withAvailabilityZone(zone); 
	            request = request.withPlacement(placement); 
	        } 
	        else { 
//	            log.error("No place to launch the instance specified." + 
//	                      "\nPlease specify either a subnet or region"); 
	        } 
	        if (keyPair != null) { 
	            request = request.withKeyName(keyPair); 
	        } 
	        if (instanceType != null) { 
	            request = request.withInstanceType(instanceType); 
	        } 
	        if (userData != null) { 
	            request = request.withUserData(Base64.encodeAsString(userData.getBytes())); 
	        } 
	        if (groups != null && !groups.isEmpty()) { 
	            request = request.withSecurityGroups(groups); 
	        } 
	        if (blockMaps != null && !blockMaps.isEmpty()) { 
	            request = request.withBlockDeviceMappings(blockMaps); 
	        } 
	        if (ariId != null && !ariId.isEmpty()) { 
	            request = request.withRamdiskId(ariId); 
	        } 
	        if (akiId != null && !akiId.isEmpty()) { 
	            request = request.withKernelId(akiId); 
	        } 
	 
	        RunInstancesResult result = ec2Client.runInstances(request); 
	 
	        List<Instance> instances = result.getReservation().getInstances(); 
	 
	        if (instances == null) { 
	            instanceId = null; 
//	            log.error("List of instances is null!"); 
	        } 
	        else if (instances.size() == 0) { 
	            instanceId = null; 
//	            log.error("List of instances is empty!"); 
	        } 
	        else if (instances.size() == 1) { 
	            instanceId = instances.get(0).getInstanceId(); 
//	            log.info("Created instance with Id: " + instanceId ); 
	        } 
	        else if (instances.size() > 1 ) { 
//	            log.error("Too many instances! This is not supported!"); 
	        } 
	        
	        return instanceId; 
	    } 
	
	    
	    /**
	     * ECS Methods
	     */
	    
	    
	public ContainerDefinition createContainerDefinition(String name,
			String image, Integer memory, List<PortMapping> portMappings,
			List<MountPoint> mountPoints, List<String> entryPoint,
			Boolean essential, Integer cpu, String... links) {
		ContainerDefinition containerDef = new ContainerDefinition();
		containerDef.withLinks(links).withName(name).withImage(image).withMemory(memory)
				.withPortMappings(portMappings).withMountPoints(mountPoints)
				.withEntryPoint(entryPoint).withEssential(essential).withCpu(cpu);
		return containerDef;
	}

	public PortMapping createPortMappings(Integer containerPort, Integer hostPort, String protocol){
		PortMapping portMapping=new PortMapping();
		portMapping.withContainerPort(containerPort).withHostPort(hostPort).withProtocol(protocol);
		return portMapping;
	}
	public MountPoint createMountPoint(String containerPath, String sourceVolume){
		MountPoint mp= new MountPoint();
		mp.withContainerPath(containerPath).withSourceVolume(sourceVolume);
		return mp;
	}
	public List<String> createEntryPoint(String... entryPoints ){
		List<String>  entryPointsList=new ArrayList<String>();
		for(int i=0;i<entryPoints.length;i++){
			entryPointsList.add(entryPoints[i]);
		}
		return entryPointsList;
		
	}
	public Volume createVolume(String name){
		Volume v=new Volume();
		v.setName(name);
		return v;
	}
	public RegisterTaskDefinitionRequest createTaskDefinitionRequest(String familyName,
			List<ContainerDefinition> containerDefinitions,
			List<Volume> volumes) {
		RegisterTaskDefinitionRequest registerTaskDefinitionRequest = new RegisterTaskDefinitionRequest();
		registerTaskDefinitionRequest.withFamily(familyName)
				.withContainerDefinitions(containerDefinitions)
				.withVolumes(volumes);
		return registerTaskDefinitionRequest;
	}
	
	/**
	 * the familyName should have the revision as well. Format is familyName:revision
	 * @param familyName
	 */
	public void deRegisterTaskDefinition(String familyName, AmazonECSClient amazonECSClient){
		DeregisterTaskDefinitionRequest deregisterTaskDefinitionRequest=new DeregisterTaskDefinitionRequest();
		deregisterTaskDefinitionRequest.withTaskDefinition(familyName);
		amazonECSClient.deregisterTaskDefinition(deregisterTaskDefinitionRequest);
	}
	
	public void stopTask(AmazonECSClient amazonECSClient){
		ListTasksResult listTaskResult=amazonECSClient.listTasks();
		List <String>taskArnList=listTaskResult.getTaskArns();
		for(String taskArn: taskArnList){
		StopTaskRequest stopTaskRequest=new StopTaskRequest();
		stopTaskRequest.withTask(taskArn);
		amazonECSClient.stopTask(stopTaskRequest);
		}
	}
	
	public CreateServiceRequest createServiceRequest( int desiredCount,String serviceName,String taskDef) {
		CreateServiceRequest createServiceRequest= new CreateServiceRequest();
		createServiceRequest.withDesiredCount(desiredCount).withServiceName(serviceName).withTaskDefinition(taskDef);
		return createServiceRequest;
	}
	
	public void deleteServiceRequest(String serviceName, AmazonECSClient amazonECSClient){
		//We need to first update desired count to 0
		UpdateServiceRequest updateServiceRequest=new UpdateServiceRequest();
		updateServiceRequest.withService(serviceName);
		updateServiceRequest.withDesiredCount(0);
		try {
			amazonECSClient.updateService(updateServiceRequest);
		} catch (ServiceNotActiveException e) {
			// TODO Auto-generated catch block
			logger.warn(" Update Service failed.Service Not active. Continuing to delete ");
		}
		DeleteServiceRequest deleteServiceRequest=new DeleteServiceRequest();
		deleteServiceRequest.withService(serviceName);
		amazonECSClient.deleteService(deleteServiceRequest);
	}
	
	/**
	 * This will deregister all instances from default cluster
	 * @param amazonECSClient
	 */
	public void deRegisterContainerInstances(AmazonECSClient amazonECSClient){
		ListContainerInstancesResult listContainerInstancesResult= amazonECSClient.listContainerInstances();
		List<String> listContainerInstanceArns= listContainerInstancesResult.getContainerInstanceArns();
		for(String containerINstanceArn: listContainerInstanceArns){
			DeregisterContainerInstanceRequest deregisterContainerInstanceRequest=new DeregisterContainerInstanceRequest();
			deregisterContainerInstanceRequest.withContainerInstance(containerINstanceArn);
			amazonECSClient.deregisterContainerInstance(deregisterContainerInstanceRequest);
		}
	}
	
	public void registerTaskAndcreateService(AmazonECSClient amazonECSClient) {
		RequestBuilder<RegisterTaskDefinitionRequest> builder = new SeleniumTaskDefinitionRequestBuilder();
		RegisterTaskDefinitionRequest registerTaskDefinitionRequest = builder
				.build();
		System.out.println("Registering task");
		RegisterTaskDefinitionResult registerTaskResult =amazonECSClient.registerTaskDefinition(registerTaskDefinitionRequest);
		String taskName=registerTaskResult.getTaskDefinition().getFamily()+":"+registerTaskResult.getTaskDefinition().getRevision();
		System.out.println("Task registered: "+ taskName);
		/*CreateServiceRequest createServiceRequest =createServiceRequest(1, "selenium-vnc",
						"selenium-chrome-vnc-task-automated");
		System.out.println("Creating Service");
		amazonECSClient.createService(createServiceRequest);
		System.out.println("Service Created");*/
	}

	public String configureAndLaunchEC2Instance(AmazonEC2Client amazonEC2Client) {
		String instanceid = null;
		try {
			System.out.println("Creating Security Group");
			createSecurityGroupRequest("ECSSecurityGroup",
					"My security group", amazonEC2Client);

			IpPermission ipPermission = new IpPermission();

			ipPermission
					.withIpRanges("0.0.0.0/0")
					.withIpProtocol("tcp").withFromPort(22).withToPort(22);
			createAuthorizeSecurityGroupIngressRequest(
					"ECSSecurityGroup", ipPermission, amazonEC2Client);
			System.out.println("Creating Key Pair");
//			createKeyPairRequest("myecskeypair", amazonEC2Client);
			System.out.println("Associating instance Profile ecsInstance Role");
			IamInstanceProfileSpecification instanceProfileSpecification = new IamInstanceProfileSpecification();
			instanceProfileSpecification.withName("ecsInstanceRole");
			System.out
					.println("Launching EC2 with image ami-c16422a4 - has ECS container agent pre installed");
			// ami that has ECS Container agent pre installed ami-c16422a4
			List <String>securityGroupList=new ArrayList<String>();
			securityGroupList.add("ECSSecurityGroup");
			instanceid = launchAmi("ami-c16422a4", null,
					"test", "t2.small", null, securityGroupList, null, null, null,
					null, instanceProfileSpecification, amazonEC2Client);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instanceid;
	}    
	
	
	
}
