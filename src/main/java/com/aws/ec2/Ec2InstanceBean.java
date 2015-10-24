package com.aws.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class Ec2InstanceBean {
	AWSCredentials credentials;
	AmazonEC2Client amazonEC2Client;
}
