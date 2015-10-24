package com.aws.ec2.helper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.ecs.model.LogConfiguration;
import com.amazonaws.services.ecs.model.LogDriver;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.TaskDefinition;
import com.amazonaws.services.ecs.model.TaskDefinitionStatus;
import com.amazonaws.services.ecs.model.TransportProtocol;
import com.amazonaws.services.ecs.model.Ulimit;
import com.amazonaws.services.ecs.model.UlimitName;
import com.amazonaws.services.simpleworkflow.flow.DataConverterException;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestJsonDataConverter {

	public static void main(String a[]) throws DataConverterException, IOException{
		
		ObjectMapper mapper=new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);

        // This will allow including type information all non-final types.  This allows correct 
        // serialization/deserialization of generic collections, for example List<MyType>. 
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        mapper.addMixInAnnotations(PortMapping.class, IgnoreProtocoltrasportMixIn.class);
        mapper.addMixInAnnotations(Ulimit.class, IgnoreProtocoltrasportMixIn.class);
        mapper.addMixInAnnotations(LogConfiguration.class, IgnoreProtocoltrasportMixIn.class);
        mapper.addMixInAnnotations(TaskDefinition.class, IgnoreProtocoltrasportMixIn.class);
        JsonDataConverter obj= new JsonDataConverter(mapper);
		System.out.println(obj.fromData(FileUtils.readFileToString(new File("src/main/resources/simple-app-task-def.json")), RegisterTaskDefinitionRequest.class));
	}
	
	abstract class IgnoreProtocoltrasportMixIn
	{
	  @JsonIgnore public abstract void setProtocol(TransportProtocol protocol);
	  @JsonIgnore public abstract void setName(UlimitName name);
	  @JsonIgnore public abstract void setLogDriver(LogDriver driver);
	  @JsonIgnore public abstract void setStatus(TaskDefinitionStatus status);
	}
}
