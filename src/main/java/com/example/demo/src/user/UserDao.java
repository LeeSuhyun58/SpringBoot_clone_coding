package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
//import com.example.demo.src.user.model.GetUserRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUsersInfoQuery = "select u.userIdx as userIdx,\n" +
                "                u.nickName as nickName,\n" +
                "                u.name as name,\n" +
                "                u.profileImgUrl as profileImgUrl,\n" +
                "                u.website as website,\n" +
                "                u.introduce as introduce,\n" +
                "                If(postCount is null, 0, postCount) as postCount,\n" +
                "                If(followerCount is null, 0, followerCount) as followerCount,\n" +
                "                If(followingCount is null, 0, followingCount) as followingCount\n" +
                "                From User as u\n" +
                "                left join (select userIdx, count(postIdx) as postCount from Post WHERE status = 'ACTIVE' group by userIdx) p on p.userIdx = u.userIdx\n" +
                "                left join (select followerIdx, count(followIdx) as followerCount from Follow WHERE status = 'ACTIVE' group by followerIdx) fc on fc.followerIdx = u.userIdx\n" +
                "                left join (select followeeIdx, count(followIdx) as followingCount from Follow WHERE status = 'ACTIVE' group by followeeIdx) f on f.followeeIdx = u.userIdx\n" +
                "                WHERE u.userIdx = ? and u.status = 'ACTIVE'";
        return this.jdbcTemplate.queryForObject(selectUsersInfoQuery,
                (rs, rowNum) -> new GetUserInfoRes(
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("website"),
                        rs.getString("introduce"),
                        rs.getInt("postCount"),
                        rs.getInt("followerCount"),
                        rs.getInt("followingCount"))
                , userIdx);
    }

    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUsersPostsQuery = "select p.postIdx as postIdx, \n" +
                " pi.imgUrl as postImgUrl\n" +
                " from Post as p\n" +
                " join PostImgUrl as pi on pi.postIdx = p.postIdx and pi.status = 'ACTIVE'\n" +
                " join User as u on u.userIdx = p.userIdx\n" +
                " WHERE p.status = 'ACTIVE' and u.userIdx = ?\n" +
                " group by p.postIdx\n" +
                " HAVING min(pi.postImgUrlIdx)\n" +
                " order by p.postIdx";
        return this.jdbcTemplate.query(selectUsersPostsQuery,
                (rs, rowNum) -> new GetUserPostsRes(
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")
                ), userIdx);
    }

    public GetUserRes getUsersByEmail(String email){
        String getUsersByEmailQuery = "select userIdx,name,nickName,email from User where email=?";
        String getUsersByEmailParams = email;
        return this.jdbcTemplate.queryForObject(getUsersByEmailQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByEmailParams);
    }

    public GetUserRes getUsersByIdx(int userIdx){
        String getUsersByIdxQuery = "select userIdx,name,nickName,email from User where userIdx=?";
        int getUsersByIdxParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUsersByIdxQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByIdxParams);
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (nickName, name, profileImgUrl, email, pwd) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getNickName(), postUserReq.getName(), postUserReq.getProfileImgUrl(), postUserReq.getEmail(), postUserReq.getPwd()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }

    public int deleteUserStatus(int userIdx){
        // userIdx 를 매개변수로 받아오고 해당 유저를 기준으로 ACTIVE 였던  status를 INACTIVE로 변경
        String deleteUserQuery = "update User set status = 'INACTIVE' where userIdx = ? ";
        Object[] deleteUserParams = new Object[]{userIdx};

        return this.jdbcTemplate.update(deleteUserQuery,deleteUserParams);
    }




}
