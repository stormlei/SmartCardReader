package com.qpsoft.smartcardreader;

public class CardInfo {
    private String name;
    private String gender;
    private String nationality;
    private String nativePlace;
    private String birthday;
    private String address;
    private String cardNumber;//身份证号
    private String issuingAuthority;
    private String startValidateDate;
    private String endValidateDate;
    private String khNumber;//社保卡号

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNativePlace() {
        return nativePlace;
    }

    public void setNativePlace(String nativePlace) {
        this.nativePlace = nativePlace;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getStartValidateDate() {
        return startValidateDate;
    }

    public void setStartValidateDate(String startValidateDate) {
        this.startValidateDate = startValidateDate;
    }

    public String getEndValidateDate() {
        return endValidateDate;
    }

    public void setEndValidateDate(String endValidateDate) {
        this.endValidateDate = endValidateDate;
    }

    public String getKhNumber() {
        return khNumber;
    }

    public void setKhNumber(String khNumber) {
        this.khNumber = khNumber;
    }
}
