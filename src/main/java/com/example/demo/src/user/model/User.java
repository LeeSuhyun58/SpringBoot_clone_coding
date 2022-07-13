package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int userIdx;
    private String name;
    private String nickName;
    private String profileImgUrl;
    private String email;
    private String pwd;
    private String status;
}
