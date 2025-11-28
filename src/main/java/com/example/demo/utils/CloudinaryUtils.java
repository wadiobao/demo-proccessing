package com.example.demo.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryUtils {
	
	private  String cloudName;
    private  String apiKey;
    private  String apiSecret;
    private Map<String, Object> params;
    Cloudinary cloudinary;
    
    public CloudinaryUtils(String cloudName, String apiKey, String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        Map<String, String> config = new HashMap<String, String>();
		config.put("cloud_name", cloudName);
		config.put("api_key", apiKey);
		config.put("api_secret", apiSecret);
		this.cloudinary = new Cloudinary(config);
		params = new HashMap<>();
		params.put("asset_folder", "question_images");
    }


	public String[] upload(String fileName) throws IOException {
		Map<String, Object> uploadResult = cloudinary.uploader().upload(fileName, params);
		String secureUrl = (String) uploadResult.get("secure_url");
		String publicId =  (String) uploadResult.get("public_id");
		return new String[] {publicId,secureUrl};
	}
	
	public void delete(List<String> publicId) throws Exception {
		cloudinary.api().deleteResources(publicId, params);
	}
	

}
