package com.qhs.blog.controller;

import com.qhs.blog.serviceImpl.captchaServiceImpl;
import com.qhs.blog.serviceImpl.tokenServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QHS on 2017/5/28.
 * 2017年5月30日17:14:38整合完毕JWT
 */
@RestController
@RequestMapping(value = "/api/captcha")
public class captchaController {
    @Autowired
    private captchaServiceImpl captchaService;

    @Autowired
    private tokenServiceImpl tokenService;

    /**
     * 拼接图片的URL
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> genCaptcha(HttpServletRequest req, HttpServletResponse resp) {

        Map<String, Object> params = new HashMap<>();
//        String token = req.getParameter("token");
//        有效性检验优化
//        Assert.hasText(token);
//        captchaService.genCaptcha(token, req, resp)
        //轮子本体中，Token由前端生成并传值，这里修改成由后端生成
        //用Token的目的：将验证码和某次操作关联起来
        //生成一个JWT的payload Map，调用tokenService生成token
        Date date = new Date();
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", "getCaptcha");//表明操作
        payload.put("iat", date.getTime());//签发时间是服务器当前时间
        payload.put("ext", date.getTime() + 1000 * 60 * 10);//过期时间10分钟
        String token = tokenService.createToken(payload);
        //用Token直接生成一个URL，写进json返回
        String URL = "/api/captcha/get?token=" + token;
        params.put("URL", URL);
        return params;
    }

    /**
     * 验证码的具体地址
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public void getCaptcha(@RequestParam String token, HttpServletRequest req, HttpServletResponse resp) {
        // 禁止图像缓存
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentType("image/jpeg");
        // 将图像输出到Servlet输出流中
        try (ServletOutputStream sos = resp.getOutputStream()) {
            BufferedImage buffImg = captchaService.genCaptcha(token);
            ImageIO.write(buffImg, "jpeg", sos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询图片验证码
     */

    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    public Map<String, Object> findCaptcha(@RequestParam String token, @RequestParam String reqCaptchaCode) {

//        String token = req.getParameter("token");
//        String reqCaptchaCode = req.getParameter("captchaCode");

        Assert.hasText(token);
        Assert.hasText(reqCaptchaCode);
        String captchaCode = captchaService.findCaptcha(token);
        Map<String, Object> params = new HashMap<>();
        boolean isValid = false;
        if (reqCaptchaCode.equals(captchaCode)) {
            isValid = true;
            //发一个新的token给客户端
            Date date = new Date();
            Map<String, Object> payload = new HashMap<>();
            payload.put("value", "captchaAuthed");//表明操作
            payload.put("iat", date.getTime());//签发时间是服务器当前时间
            payload.put("ext", date.getTime() + 1000 * 60 * 10);//过期时间10分钟
            token = tokenService.createToken(payload);
            //将新token和是否有效一起装进去
            params.put("valid", isValid);
            params.put("token",token);
        }
        params.put("valid", isValid);
        return params;
    }
}