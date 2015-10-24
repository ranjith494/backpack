package com.aws.ec2.builder;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.HostVolumeProperties;
import com.amazonaws.services.ecs.model.MountPoint;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.Volume;
import com.aws.ec2.helper.AwsHelper;

public class SeleniumTaskDefinitionRequestBuilder implements RequestBuilder<RegisterTaskDefinitionRequest>{

	@Override
	public RegisterTaskDefinitionRequest build() {
		AwsHelper helper= AwsHelper.getInstance();
		List<PortMapping> portMappings= new ArrayList<PortMapping>();
		portMappings.add(helper.createPortMappings(80, 80,"tcp"));
		portMappings.add(helper.createPortMappings(4444, 4444,"tcp"));
		MountPoint mountpoint=new MountPoint().withContainerPath("/opt/tests").withSourceVolume("my-vol");
		ArrayList<MountPoint> mountPoints=new ArrayList<MountPoint>();
//		mountPoints.add(mountpoint);
		ContainerDefinition containerDef=helper.createContainerDefinition("selenium-chrome-vnc-container", "geetharam/selenium-chrome-vnc", 300, portMappings, mountPoints, null, true,10);
		ContainerDefinition containerDef1=helper.createContainerDefinition("protractor-tests", "geetharam/protractor_tests", 1500, null, null, null, false,10,"selenium-chrome-vnc-container");
		List <ContainerDefinition> containerDefinitions=new ArrayList<ContainerDefinition>();
		
		containerDefinitions.add(containerDef);
		containerDefinitions.add(containerDef1);
		Volume volume=new Volume();
		HostVolumeProperties hostvolprop=new HostVolumeProperties();
		hostvolprop.withSourcePath("/home/ec2-user");
		volume.withHost(hostvolprop).withName("my-vol");
		List <Volume> volumes=new ArrayList<Volume>();
		RegisterTaskDefinitionRequest registerTaskDefinitionRequest=helper.createTaskDefinitionRequest("selenium-chrome-vnc-task-automated", containerDefinitions, volumes);
		return registerTaskDefinitionRequest;
	}

}
