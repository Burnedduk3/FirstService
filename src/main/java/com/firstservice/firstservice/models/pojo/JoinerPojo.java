package com.firstservice.firstservice.models.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class JoinerPojo {
    Integer identificationNumber;
    String name;
    String lastName;
    String stack;
    String role;
    String englishLevel;
    String domainExperience;

    @Override
    public String toString() {
        return "{" +
                "\"identificationNumber\":" + identificationNumber +
                ", \"name\":\"" + name + '"' +
                ", \"lastName\":\"" + lastName + '"' +
                ", \"stack\":\"" + stack + '"' +
                ", \"role\":\"" + role + '"' +
                ", \"englishLevel\":\"" + englishLevel + '"' +
                ", \"domainExperience\":\"" + domainExperience + '"' +
                '}';
    }
}

