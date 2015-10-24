package server.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import server.obj.Account;

@Path("/accountservice/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountService {

	Map<String, Account> accounts = new HashMap<String, Account>();
	private static String bucketName = "ranjith.kethapally";

	public void init() {

		Account newAccount1 = new Account();
		newAccount1.setId(1);
		newAccount1.setName("Alvin Reyes");

		Account newAccount2 = new Account();
		newAccount2.setId(2);
		newAccount2.setName("Rachelle Ann de Guzman Reyes");

		accounts.put("1", newAccount1);
		accounts.put("2", newAccount2);

	}

	public AccountService() {
		init();
	}

	@GET
	@Path("/accounts/{id}/")
	public Account getAccount(@PathParam("id") String id) {
		Account c = accounts.get(id);
		return c;
	}

	@GET
	@Path("/accounts/getall")
	public List<Account> getAllAccounts(Account account) {
		List<Account> accountList = new ArrayList<Account>();
		for (int i = 0; i <= accounts.size(); i++) {
			accountList.add((Account) accounts.get(i));
		}
		return accountList;
	}
	
	@GET
	@Path("/backpack/logs")
	@Produces("application/json")
	public List<String> getTestLogsFromS3() throws InterruptedException, IOException {
		AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix("l");
		ObjectListing objectListing;
		List<String> list = new ArrayList<String>();
		List<S3ObjectSummary> objectList = new ArrayList<S3ObjectSummary>();
		int count = 1;
        try {
            while(true) {
            	System.out.println("Thread sleeping for 10 Secs. Loop: "+count);
    			Thread.sleep(10000);
    			count++;
    			objectListing = s3Client.listObjects(listObjectsRequest);
    			objectList = objectListing.getObjectSummaries();
    			System.out.println("Bucket contains "+ objectList.size() + " files.");
    			if(objectList.size() == 4)
    				break;
    	    }
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            	S3Object s3object = s3Client.getObject(new GetObjectRequest(bucketName, objectSummary.getKey()));
                saveFile(s3object.getObjectContent(), s3object.getKey());
                list.add(s3object.getKey());
			}
            System.out.println("Download of log files completed.");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
        return list;
	}
	
	private static void saveFile(InputStream input, String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));        
        File file = new File("C:/Users/Ranjith Reddy/Work/workspace/BackPack/src/main/webapp/resources/"+fileName);      
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;            
            writer.write(line + "\n");
        }
        writer.close();
	}

}