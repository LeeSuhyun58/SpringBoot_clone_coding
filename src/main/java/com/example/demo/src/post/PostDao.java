package com.example.demo.src.post;

import com.example.demo.src.post.model.GetPostImgRes;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PostImgUrlReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery =
                "SELECT p.postIdx as postIdx,\n" +
                        "       u.userIdx as userIdx,\n" +
                        "       u.nickName as nickName,\n" +
                        "       u.profileImgUrl as profileImgUrl,\n" +
                        "       p.content as content,\n" +
                        "       IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                        "       IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                        "       CASE WHEN timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                        "        THEN concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                        "        WHEN timestampdiff(minute, p.updatedAt, current_timestamp) < 60\n" +
                        "        THEN concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                        "        WHEN timestampdiff(hour, p.updatedAt, current_timestamp) < 24\n" +
                        "        THEN concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                        "        WHEN timestampdiff(day, p.updatedAt, current_timestamp) < 365\n" +
                        "        THEN concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                        "        ELSE timestampdiff(year, p.updatedAt, current_timestamp)\n" +
                        "        END AS updatedAt,\n" +
                        "        IF(Pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                        "        FROM Post as p\n" +
                        "        JOIN User as u on u.userIdx = p.userIdx\n" +
                        "        LEFT JOIN (SELECT postIdx, userIdx, count(likeIdx) as postLikeCount\n" +
                        "                   FROM PostLike\n" +
                        "                   WHERE status ='ACTIVE'\n" +
                        "                   GROUP BY postIdx) as pl on p.postIdx = pl.postIdx\n" +
                        "        LEFT JOIN (SELECT postIdx, COUNT(commentIdx) as commentCount\n" +
                        "                   FROM Comment\n" +
                        "                   WHERE status='ACTIVE') as c on c.postIdx = p.postIdx\n" +
                        "        LEFT JOIN Follow as f on f.followeeIdx = p.userIdx and f.status = 'ACTIVE'\n" +
                        "        LEFT JOIN PostLike as Pl on Pl.userIdx = f.followerIdx and Pl.postIdx = p.postIdx\n" +
                        "        WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                        "        group by p.postIdx";

        int selectPostsParam=userIdx;

        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        getPostImgRes=this.jdbcTemplate.query("SELECT pi.postImgUrlIdx, pi.imgUrl\n" +
                                        "             FROM PostImgUrl as pi\n" +
                                        "             JOIN Post as p on p.postIdx = pi.postIdx\n" +
                                        "             WHERE pi.status = 'ACTIVE' and p.postIdx = ?;",
                                (rk,rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl")
                                ), rs.getInt("postIdx")
                        )
                ),selectPostsParam);
    }

    //userid가 존재하는지 체크하는 함수
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);

    }

    public int insertPost(int userIdx, String content){
        String insertPostQuery = "insert into Post(userIdx, content) values (?,?)";
        Object [] insertPostsParams = new Object[] {userIdx, content};
        this.jdbcTemplate.update(insertPostQuery, insertPostsParams);

        String lastInsertIdxQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);

    }

    public int insertPostImgs(int postIdx, PostImgUrlReq postImgUrlReq){
        String insertPostImgQuery = "insert into PostImgUrl(postIdx, imgUrl) values (?,?)";
        Object [] insertPostsImgParams = new Object[] {postIdx, postImgUrlReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgQuery, insertPostsImgParams);

        String lastInsertIdxQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);

    }

    public int updatePost(int postIdx, String content){
        String updatePostQuery = "update Post set content=? where postIdx=?";
        Object [] updatePostsParams = new Object[] {content, postIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostsParams);
    }

    public int deletePost(int postIdx){
        String updatePostQuery = "update Post set status='INACTIVE' where postIdx=?";
        Object [] updatePostsParams = new Object[] {postIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostsParams);
    }
}
