package com.ija.student_management_portal.dto;

import java.time.LocalDateTime;

public class StudentDTO {

//    private Long id;
    private String name;
    private String phoneNumber;
    private String alternateNumber;
    private String standard;
    private String address;
    private String guardiansName;
    private LocalDateTime admissionDate;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getAlternateNumber() {
        return alternateNumber;
    }

    public void setAlternateNumber(String alternateNumber) {
        this.alternateNumber = alternateNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGuardiansName() {
        return guardiansName;
    }

    public void setGuardiansName(String guardiansName) {
        this.guardiansName = guardiansName;
    }

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }




    public StudentDTO(String name, String phoneNumber, String alternateNumber, String standard, String address, String guardiansName, LocalDateTime admissionDate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.alternateNumber = alternateNumber;
        this.standard = standard;
        this.address = address;
        this.guardiansName = guardiansName;
        this.admissionDate = admissionDate;
    }

    public StudentDTO() {

    }

}
