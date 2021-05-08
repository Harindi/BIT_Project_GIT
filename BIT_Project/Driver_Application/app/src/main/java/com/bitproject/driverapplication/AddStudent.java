package com.bitproject.driverapplication;

public class AddStudent {

    private String key;

    private String studentName;
    private String school;
    private String contactNumber;
    private String fee;
    private String parentName;

    public AddStudent() {}

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public AddStudent(String studentName, String school, String contactNumber, String fee, String parentName) {
        this.studentName = studentName;
        this.school = school;
        this.contactNumber = contactNumber;
        this.fee = fee;
        this.parentName = parentName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
