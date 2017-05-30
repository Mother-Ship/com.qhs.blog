package com.qhs.blog.serviceImpl;

import com.qhs.blog.bean.User;
import com.qhs.blog.mapper.userMapper;
import com.qhs.blog.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 2017年5月30日22:41:24实现用户注册逻辑
 */

@Service
public class userServiceImpl implements userService {


    @Autowired
    private userMapper ud;
    @Autowired
    private tokenServiceImpl tokenService;
    //用户注册方法
    public Map<String , Object> userReg(User user) {
        Map<String, Object> result = new HashMap<>();
        //检验用户名、密码、邮箱是否为空

        if (user.getEmail() != null ||
            user.getName() != null ||
            user.getPwd() != null) {
            //如果都不为空，检验数据库中是否已经存在
            if(ud.checkEmail(user)==null || ud.checkEmail(user)==null){
                ud.add(user);
                result.put("result","success");

                //颁布象征用户身份的Token
                Date date = new Date();
                Map<String, Object> payload = new HashMap<>();
                payload.put("uid", user.getName());//之前注册成功的用户名
                payload.put("iat", date.getTime());//签发时间是服务器当前时间
                payload.put("ext", date.getTime() + 1000 * 60 * 60 * 24 * 7);//过期时间一周
                String token = tokenService.createToken(payload);
                result.put("token",token);
            }else{
                result.put("result","exist");
            }
        }else{
            result.put("result","incomplete");
        }
        return result;
    }


    public Map<String , Object> userLogin(User user) {

        return null;
    }

    public Map<String , Object> userLogout(User user) {
        return null;
    }

    public Map<String , Object> userEdit() {
        return null;
    }

}
