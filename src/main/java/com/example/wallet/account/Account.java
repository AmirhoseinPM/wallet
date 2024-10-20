package com.example.wallet.account;

import com.example.wallet.account.helper.Gender;
import com.example.wallet.account.helper.MilitaryStatus;
import com.example.wallet.wallet.Wallet;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import java.util.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "phoneNumber", "email" , "nationalId"})
})
public class Account {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "National Id is required")
    @Pattern(regexp = "^\\d{10}$",
            message = "National id contains 10 number!!")
    @Column(unique = true)
    private String nationalId;

    @NotNull(message = "phone number is required")
    @Pattern(regexp = "^\\d{11}$",
            message = "Your phone number is not valid!!")
    @Column(unique = true)
    private String phoneNumber;

    @NotNull(message = "Email is required")
    @Email(message = "Non valid email field")
    @Column(name = "email", unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 8, max=60, message = "Password contains 8 character at least")
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "firstName is required")
    @Size(min = 3, max = 50 ,
            message = "first name must be between 3 and 50 character")
    private String firstName;

    @NotNull(message = "lastName is required")
    @Size(min = 3, max = 50 ,
            message = "last name must be between 3 and 50 character")
    private String lastName;

    @NotNull(message = "birtDate is required in yyyy-MM-dd format")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd format
    private LocalDate birthDate;

    @NotNull(message = "gender is required. choices are: male, female, others")
    private Gender gender;

    private boolean isActive = false;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Date createdAt;


    private MilitaryStatus militaryStatus;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    // mapped by refers to Person field name in Wallet ManyToOne relation
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Wallet> wallets;

    // add wallet for person in service layer
    public void addWallet(Wallet wallet) {
        if (wallets == null) {
            wallets = new HashSet<>();
        }
        wallets.add(wallet);
    }

    // constructors
    public Account() {
    }
    public Account(String nationalId, String phoneNumber, String email, String password,
                   String firstName, String lastName, LocalDate birthDate, Gender gender,
                   MilitaryStatus militaryStatus, Date createdAt) {
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.militaryStatus = militaryStatus;
        this.createdAt = createdAt;
    }
    public Account(String nationalId, String phoneNumber, String email, String password,
                   String firstName, String lastName, LocalDate birthDate, Gender gender,
                   MilitaryStatus militaryStatus) {
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.militaryStatus = militaryStatus;
    }

    public Account(String nationalId, String phoneNumber,
                   String email, String password, String firstName,
                   String lastName, LocalDate birthDate, Gender gender,
                   MilitaryStatus militaryStatus, Set<Wallet> wallets) {
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.militaryStatus = militaryStatus;
        this.wallets = wallets;
    }

    // getter and setter ----------------------------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birtDate) {
        this.birthDate = birtDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MilitaryStatus getMilitaryStatus() {
        return militaryStatus;
    }

    public void setMilitaryStatus(MilitaryStatus militaryStatus) {
        this.militaryStatus = militaryStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Wallet> getWallets() {
        return wallets;
    }

    public void setWallets(Set<Wallet> wallets) {
        this.wallets = wallets;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", nationalId='" + nationalId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", createdAt=" + createdAt +
                ", militaryStatus='" + militaryStatus + '\'' +
                '}';
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
