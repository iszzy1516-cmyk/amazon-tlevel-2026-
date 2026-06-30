package com.amazon.tlevel.model;

public class Course {
    private String courseId;
    private String name;
    private String subject;
    private String location;
    private String description;
    private String entryRequirements;
    private String whatYouWillLearn;
    private String signUpLink;
    private String amazonInvolvement;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEntryRequirements() { return entryRequirements; }
    public void setEntryRequirements(String entryRequirements) { this.entryRequirements = entryRequirements; }
    public String getWhatYouWillLearn() { return whatYouWillLearn; }
    public void setWhatYouWillLearn(String whatYouWillLearn) { this.whatYouWillLearn = whatYouWillLearn; }
    public String getSignUpLink() { return signUpLink; }
    public void setSignUpLink(String signUpLink) { this.signUpLink = signUpLink; }
    public String getAmazonInvolvement() { return amazonInvolvement; }
    public void setAmazonInvolvement(String amazonInvolvement) { this.amazonInvolvement = amazonInvolvement; }
}
