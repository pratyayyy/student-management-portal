package com.ija.student_management_portal.dto;

public class StudentRowModel {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAlternateNumber(String alternateNumber) {
        this.alternateNumber = alternateNumber;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGuardiansName(String guardiansName) {
        this.guardiansName = guardiansName;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAlternateNumber() {
        return alternateNumber;
    }

    public String getStandard() {
        return standard;
    }

    public String getAddress() {
        return address;
    }

    public String getGuardiansName() {
        return guardiansName;
    }

    public String getStudentId() {
        return studentId;
    }

    private String phoneNumber;
    private String alternateNumber;
    private String standard;
    private String address;
    private String guardiansName;
    private String studentId;

}
