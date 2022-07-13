package com.example.demo.src.user.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetUserFedRes {
    private boolean isMyFed;
    private GetUserInfoRes getUserInfo;
    private List<GetUserPostsRes> getUserPosts;

}
