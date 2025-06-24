package main.com.apod.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)  // This will ignore unknown fields
public class ApodResponse {
    private String date;
    private String explanation;
    private String media_type;
    private String title;
    private String url;
    private String copyright;
    private String hdurl;  // Add this new field
    private String service_version;  // Add other possible fields
    // ... any other fields NASA might return

    // Add getters and setters for new fields
    public String getHdurl() { return hdurl; }
    public void setHdurl(String hdurl) { this.hdurl = hdurl; }
    
    public String getService_version() { return service_version; }
    public void setService_version(String service_version) { this.service_version = service_version; }

    // Keep all your existing getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getMedia_type() { return media_type; }
    public void setMedia_type(String media_type) { this.media_type = media_type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }
}