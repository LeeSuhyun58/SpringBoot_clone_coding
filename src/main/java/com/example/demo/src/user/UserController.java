package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.config.BaseResponseStatus.POST_USERS_EMPTY_EMAIL;
import static com.example.demo.config.BaseResponseStatus.POST_USERS_INVALID_EMAIL;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;


    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    //Query String
    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:9000/users
    public BaseResponse<GetUserFedRes> getUserFed(@PathVariable("userIdx")int userIdx) {
        try {

            GetUserFedRes getUserFedRes = userProvider.retrieveUserFed(userIdx,userIdx);
            return new BaseResponse<>(getUserFedRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{userIdx}/x") // (GET) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<GetUserRes> getUserByIdx(@PathVariable("userIdx")int userIdx) {
        try {

            GetUserRes getUsersRes = userProvider.getUsersByIdx(userIdx);
            return new BaseResponse<>(getUsersRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * ???????????? API
     * [POST] /users
     *
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("") // (POST) 127.0.0.1:9000/users
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email ????????? ?????? validation ???????????????. ??? ??? ??? ??????????????? ??????????????????!
        if (postUserReq.getEmail() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        // ????????? ????????????
        if (!isRegexEmail(postUserReq.getEmail())) {
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        try {
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * ?????????????????? API
     * [PATCH] /users/:userIdx
     *
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userIdx}") // (PATCH) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<String> modifyUserName(@PathVariable("userIdx") int userIdx, @RequestBody User user) {
        try {
            /* TODO: jwt??? ?????????????????? ?????? ???????????????!
            jwt?????? idx ??????.
            int userIdxByJwt = jwtService.getUserIdx();
            userIdx??? ????????? ????????? ????????? ??????
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            */

            PatchUserReq patchUserReq = new PatchUserReq(userIdx, user.getNickName());
            userService.modifyUserName(patchUserReq);

            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    @ResponseBody
    @PatchMapping("/{userIdx}/status") // (PATCH) 127.0.0.1:9000/users/:userIdx/status
    public BaseResponse<String> deleteUserStatus(@PathVariable("userIdx") int userIdx){
        try {

            userService.deleteUserStatus(userIdx);

            String result = "????????? ?????????????????????.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}