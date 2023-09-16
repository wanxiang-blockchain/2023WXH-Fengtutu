package com.bage.controller;
/*
 * 疯兔兔画图相关
 * */
import com.alibaba.fastjson.JSONObject;
import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AiCreatSdDataDto;
import com.bage.domain.dto.AiExpandDataDto;
import com.bage.domain.dto.TextExamineDataDto;
import com.bage.domain.entity.AIUser;
import com.bage.domain.entity.AiTranslate;
import com.bage.domain.entity.AiUserImgs;
import com.bage.domain.vo.AIAdCountVo;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.mapper.AIUserMapper;
import com.bage.service.AIUserService;
import com.bage.service.AiTranslateService;
import com.bage.service.AiUserImgsService;
import com.bage.utils.BeanCopyUtils;
import com.bage.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.util.Auth;
import com.bage.utils.RedisCache;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@RestController
@RequestMapping("/aiphototo") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Data
@ConfigurationProperties(prefix = "aioss") //这个注解可以帮我们把我们自定义的springBoot的公共配置（application.yml）引入进来（注意要配合@Data注解才生效）
@Api(tags = "画图",description = "画图相关接口") //swagger的注解
public class AIPhotoToSseSdController {


    private String accessKey;
    private String secretKey;
    private String bucket;
    //将类转换成字符串
    private final ObjectMapper objectMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AIUserMapper aIUserMapper;

    @Autowired//直接用我们的service
    private AiTranslateService aiTranslateService;

    @Autowired//直接用我们的service
    private AIUserService aIUserService;

    @Autowired//直接用我们的service
    private AiUserImgsService aiUserImgsService;

    private Queue<RequestWithFuture> requestQueue = new ConcurrentLinkedQueue<RequestWithFuture>();
    private Queue<RequestWithFutureExpand> requestQueueExpand = new ConcurrentLinkedQueue<RequestWithFutureExpand>();
    private Lock lock = new ReentrantLock();
    private boolean isProcessing = false;


    /**
     * 获取七牛token
     **/
    @PostMapping("/qiniuToken")
    @ApiOperation(value = "七牛",notes = "获取七牛token接口")
    public ResponseResult qiniuToken(@RequestBody String token) {
        String upToken = null;
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            upToken = auth.uploadToken(bucket);
        } catch (Exception ex) {
            //ignore
        }
        return ResponseResult.okResult(upToken);
    }

    /**
     *  生成图片
     **/
    @PostMapping("/creatSd")
    @ApiOperation(value = "画图主接口",notes = "调用python画图接口")
    public ResponseResult creatSd(@RequestBody AiCreatSdDataDto creatSdData) {
        //如果没有传入用户名
        if(!StringUtils.hasText(creatSdData.getToken())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }

        //根据user_name查询用户
        LambdaQueryWrapper<AIUser> queryWrapper = new LambdaQueryWrapper<>();
        String id = creatSdData.getToken();
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(id);
        } catch (Exception e) {
            e.printStackTrace();
            //token超时  token非法
            //响应告诉前端需要重新登录
        }
        String userId = claims.getSubject();
        queryWrapper.eq(AIUser::getUserName,userId );
        AIUser aIUser = aIUserMapper.selectOne(queryWrapper);
        AIAdCountVo aIAdCountVo = BeanCopyUtils.copyBean(aIUser, AIAdCountVo.class);

        //可用数为0则不能继续画图
        if(Integer.parseInt(aIAdCountVo.getUsableCount())==0){
            throw new SystemException(AppHttpCodeEnum.USABLECOUNT_ERROR);
        }else{
            //否则可用数-1
            String newUsableCount = String.valueOf((Integer.parseInt(aIAdCountVo.getUsableCount())-1));
            aIUser.setUsableCount(newUsableCount);
            aIUserMapper.updateById(aIUser);
        }
//        return ResponseResult.okResult(aIAdCountVo);
        CompletableFuture<ResponseResult> future = new CompletableFuture<>();
        lock.lock();
        try {
            requestQueue.offer(new RequestWithFuture(creatSdData, future));
            if (!isProcessing) {
                isProcessing = true;
                processNextRequest();
            }
        } finally {
            lock.unlock();
        }
        return future.join();
    }
    private void processNextRequest() {
        RequestWithFuture requestWithFuture = (RequestWithFuture) requestQueue.poll();
        if (requestWithFuture != null) {
            AiCreatSdDataDto request = requestWithFuture.getRequest();

            // 调用外部服务的代码
            ResponseResult response = callExternalService(request);

            // 处理外部服务返回的结果
            // ...

            // 将结果设置到CompletableFuture对象中
            requestWithFuture.getFuture().complete(response);

            // 处理完成后递归调用以处理下一个请求
            processNextRequest();
        } else {
            lock.lock();
            try {
                isProcessing = false;
            } finally {
                lock.unlock();
            }
        }
    }
    private static class RequestWithFuture {
        private AiCreatSdDataDto request;
        private CompletableFuture<ResponseResult> future;

        public RequestWithFuture(AiCreatSdDataDto request, CompletableFuture<ResponseResult> future) {
            this.request = request;
            this.future = future;
        }

        public AiCreatSdDataDto getRequest() {
            return request;
        }

        public CompletableFuture<ResponseResult> getFuture() {
            return future;
        }
    }
    /**
    * 调用外部服务请求生成图片
    **/
    private ResponseResult callExternalService(AiCreatSdDataDto creatSdData) {
        //重新设置翻译长度
        AiTranslate translate = new AiTranslate();
        //TODO 这里如果改成getPromptcn会出现走不到画图方法的问题，具体原因未知，有时间改下
        if(creatSdData.getPromptcn() != null){
            translate.setTranslationuselength(creatSdData.getPromptcn());
        }else{
            translate.setTranslationuselength(creatSdData.getPrompt());
        }
        aiTranslateService.translationJudgment(translate);
        // 调用外部服务的代码
        // 返回外部服务的响应结果
        StringBuilder response = null;
        try {
            //测试地址
//            String url = "https://www.aiphototo.com:29851/creatHub";
            //生产地址
//            String url = "https://www.aiphototo.com:49112/creatHub";
            //测试地址无https
            String url = "http://127.0.0.1:6111/creatHub";//配合内网穿透到生产地址 ssh -CNg -L 6111:127.0.0.1:6111 root@region-42.seetacloud.com -p 16528  密码iWvwjee5rf
            //因为creatSdData为AiCreatSdDataDto类型，所以需要将其转换成String的形势才能整个传给图片服务器
            String requestBody = objectMapper.writeValueAsString(creatSdData);//将类 类型转换成string类型

            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
//            HttpsURLConnection connection = (HttpsURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);
            System.out.println("调用了图片生成的服务");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();

//            int responseCode = connection.getResponseCode();
//                System.out.println("Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();
            // 转成Json对象 获取images
            JSONObject jsonObject = JSONObject.parseObject(String.valueOf(response));
            //获取base64图片流
            String base64Arr = jsonObject.getString("images");
            //将base64图片存到服务器的指定位置并返回图片在服务器上的具体url
//            String imgUrl = base64ImageSave(base64Arr);
            //增加图片审核
            boolean auditResult = imgexaminevxFun(base64Arr);
            int auditResultStatus;
            auditResultStatus = auditResult?0:1;
            //将生成好的图片url保存到数据库
            AiUserImgs aiUserImgs = new AiUserImgs();//这里初始化不能声明为null，否则塞不进去值
            String id = creatSdData.getToken();
            Claims claims = null;
            try {
                claims = JwtUtil.parseJWT(id);
            } catch (Exception e) {
                e.printStackTrace();
                //token超时  token非法
                //响应告诉前端需要重新登录
            }
            String userId = claims.getSubject();
            aiUserImgs.setUserName(userId);
//            aiUserImgs.setImgUrl(imgUrl);
            aiUserImgs.setPrompt(creatSdData.getPrompt());
            aiUserImgs.setPromptCn(creatSdData.getPromptcn());
            aiUserImgs.setModel(creatSdData.getModel());
            aiUserImgs.setNegative(creatSdData.getNegative());
            aiUserImgs.setCn(creatSdData.getCn());
            aiUserImgs.setSteps(creatSdData.getSteps());
            aiUserImgs.setExamineStatus(auditResultStatus);//设置审核状态
            aiUserImgsService.setUserImgs(aiUserImgs);
            //图片审核通过
            if(auditResult){
//                //如果图片保存失败则直接返回所有内容
//                if(imgUrl == "Failed to upload image."){
//                    //将图片服务器的内容整个返回给前端
                    return ResponseResult.okResult(response);
//                }else{
                    //只把图片url给前端
//                    return ResponseResult.okResult(imgUrl);
//                }
            }else{
                //图片可用次数+1 TODO 这里可以封装起来和上面复用
                LambdaQueryWrapper<AIUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AIUser::getUserName,userId );
                AIUser aIUser = aIUserMapper.selectOne(queryWrapper);
                AIAdCountVo aIAdCountVo = BeanCopyUtils.copyBean(aIUser, AIAdCountVo.class);
                String newUsableCount = String.valueOf((Integer.parseInt(aIAdCountVo.getUsableCount())+1));
                aIUser.setUsableCount(newUsableCount);
                aIUserMapper.updateById(aIUser);

                return ResponseResult.errorResult(AppHttpCodeEnum.PICTURE_REVIEW_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.USACREAT_ERROR);
        }
    }







    /**
     *  变大变高清
     **/
    @PostMapping("/expand")
    @ApiOperation(value = "变大变高清",notes = "调用python变大变高清接口")
    public ResponseResult expand(@RequestBody AiExpandDataDto expandDataDto) {
        //如果没有传入用户名
        if(!StringUtils.hasText(expandDataDto.getToken())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }

        //根据user_name查询用户
        LambdaQueryWrapper<AIUser> queryWrapper = new LambdaQueryWrapper<>();
        String id = expandDataDto.getToken();
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(id);
        } catch (Exception e) {
            e.printStackTrace();
            //token超时  token非法
            //响应告诉前端需要重新登录
        }
        String userId = claims.getSubject();
        queryWrapper.eq(AIUser::getUserName,userId );
        AIUser aIUser = aIUserMapper.selectOne(queryWrapper);
        AIAdCountVo aIAdCountVo = BeanCopyUtils.copyBean(aIUser, AIAdCountVo.class);

        //可用数为0则不能继续画图
        if(Integer.parseInt(aIAdCountVo.getUsableCount())==0){
            throw new SystemException(AppHttpCodeEnum.USABLECOUNT_ERROR);
        }else{
            //否则可用数-1
            String newUsableCount = String.valueOf((Integer.parseInt(aIAdCountVo.getUsableCount())-1));
            aIUser.setUsableCount(newUsableCount);
            aIUserMapper.updateById(aIUser);
        }
//        return ResponseResult.okResult(aIAdCountVo);


        CompletableFuture<ResponseResult> future = new CompletableFuture<>();
        lock.lock();
        try {
            requestQueueExpand.offer(new RequestWithFutureExpand(expandDataDto, future));
            if (!isProcessing) {
                isProcessing = true;
                processNextRequestExpand();
            }
        } finally {
            lock.unlock();
        }
        return future.join();
    }
    private void processNextRequestExpand() {
        RequestWithFutureExpand requestWithFutureExpand = (RequestWithFutureExpand) requestQueueExpand.poll();
        if (requestWithFutureExpand != null) {
            AiExpandDataDto request = requestWithFutureExpand.getRequest();

            // 调用外部服务的代码
            ResponseResult response = callExternalServiceExpand(request);

            // 处理外部服务返回的结果
            // ...

            // 将结果设置到CompletableFutureExpand对象中
            requestWithFutureExpand.getFuture().complete(response);

            // 处理完成后递归调用以处理下一个请求
            processNextRequest();
        } else {
            lock.lock();
            try {
                isProcessing = false;
            } finally {
                lock.unlock();
            }
        }
    }
    private static class RequestWithFutureExpand {
        private AiExpandDataDto request;
        private CompletableFuture<ResponseResult> future;

        public RequestWithFutureExpand(AiExpandDataDto request, CompletableFuture<ResponseResult> future) {
            this.request = request;
            this.future = future;
        }

        public AiExpandDataDto getRequest() {
            return request;
        }

        public CompletableFuture<ResponseResult> getFuture() {
            return future;
        }
    }
    /**
     * 调用外部服务请求生成图片
     **/
    private ResponseResult callExternalServiceExpand(AiExpandDataDto expandDataDto) {
        StringBuilder response = null;
        try {
            String url = "http://127.0.0.1:6111/expand";//配合内网穿透到生产地址 ssh -CNg -L 6111:127.0.0.1:6111 root@region-42.seetacloud.com -p 16528  密码iWvwjee5rf
            //因为creatSdData为AiCreatSdDataDto类型，所以需要将其转换成String的形势才能整个传给图片服务器
            String requestBody = objectMapper.writeValueAsString(expandDataDto);//将类 类型转换成string类型
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
//            HttpsURLConnection connection = (HttpsURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);
            System.out.println("调用了图片生成的服务");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();

//            int responseCode = connection.getResponseCode();
//                System.out.println("Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();
            //将图片服务器的内容整个返回给前端
            return ResponseResult.okResult(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.USACREAT_ERROR);
        }
//            try {
//                // 处理请求的逻辑
//                Thread.sleep(10000); // 模拟请求处理耗时
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

    }



    /**
     *  文本内容审核
     **/
    @PostMapping("/textexamine")
    @ApiOperation(value = "文本审核",notes = "调用微信审核接口")
    public ResponseResult textexamine(@RequestBody TextExamineDataDto textExamineDataDto){
        //从redis中获取用户信息
        String accessToken = redisCache.getCacheObject("accessToken");
        String id = textExamineDataDto.getToken();
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(id);
        } catch (Exception e) {
            e.printStackTrace();
            //token超时  token非法
            //响应告诉前端需要重新登录
        }
        String openid = claims.getSubject();
        // 构建请求 URL
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + accessToken;
        // 构建请求体内容
        String rawContent = textExamineDataDto.getPrompt();
        String requestBody = "{ \"openid\": \""+openid+"\", \"scene\": "+textExamineDataDto.getScene()+", \"version\": 2, \"content\": \"" + rawContent + "\" }";
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 写入请求体内容
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            // 获取响应
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // 处理响应结果
            String responseBody = responseBuilder.toString();
            return ResponseResult.okResult(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR);
    }


    /**
     *  图片内容审核（不好用，基本检测不出来）
     **/
    @PostMapping("/imgexamine")
    @ApiOperation(value = "文本审核",notes = "调用微信审核接口")
    public ResponseResult imgexamine(@RequestBody TextExamineDataDto textExamineDataDto){
        //从redis中获取用户信息
        String accessToken = redisCache.getCacheObject("accessToken");
        String id = textExamineDataDto.getToken();
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(id);
        } catch (Exception e) {
            e.printStackTrace();
            //token超时  token非法
            //响应告诉前端需要重新登录
        }
        String openid = claims.getSubject();
        // 构建请求 URL
        String url = "https://api.weixin.qq.com/wxa/media_check_async?access_token=" + accessToken;
        // 构建请求体内容
        String imageUrl = textExamineDataDto.getPrompt();
        String requestBody = "{\n" +
                "  \"media_url\": \"" + imageUrl + "\",\n" +
                "  \"media_type\": 2,\n" +
                "  \"version\": 2,\n" +
                "  \"scene\": 3,\n" +
                "  \"openid\": \""+openid+"\"\n" +
                "}";
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 写入请求体内容
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            // 获取响应
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // 处理响应结果
            String responseBody = responseBuilder.toString();
            return ResponseResult.okResult(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR);
    }

    /**
     *  将base64图片存入指定位置并返回图片url
     **/
    public String base64ImageSave(String base64Image) {
        // 获取Base64图片数据
//        String base64Data = base64Image.split(",")[1];//这个用来获取前端直接传过来的base64字段数据
//        String base64Data = base64Image;
        byte[] imageData = decodeBase64ImageFun(base64Image);
        // 将Base64数据解码为字节数组
//        byte[] imageData = Base64.decodeBase64(base64Image);

        try {
            // 指定保存图片的路径
            String username = System.getProperty("user.name");
            // 生成随机的文件名
            String fileName = UUID.randomUUID().toString();
            //存放图片的具体位置，桌面
//            String imagePath = "/Users/" + username + "/Desktop/ceshi/"+fileName+".png";
            //服务器上的保存位置
            String imagePath = "/data/"+fileName+".png";
            // 创建文件输出流
            FileOutputStream fos = new FileOutputStream(new File(imagePath));
            // 将图片数据写入文件
            FileCopyUtils.copy(imageData, fos);
            // 关闭文件输出流
            fos.close();
            return imagePath;
        } catch (IOException e) {
            isProcessing = false;//临时加的怕因为报错导致这个变量变不过来
            e.printStackTrace();
            return "Failed to upload image.";
        }
    }


    /**
     *  将base64图片存入指定位置并返回图片url
     **/
    @PostMapping("/upload")
    @ResponseBody
    public String uploadImage(@RequestBody String base64Image) {
        // 获取Base64图片数据
        String base64Data = base64Image.split(",")[1];//这个用来获取前端直接传过来的base64字段数据
//        String base64Data = base64Image;
        // 将Base64数据解码为字节数组
        byte[] imageData = Base64.decodeBase64(base64Data);

        try {
            // 指定保存图片的路径
//            String username = System.getProperty("user.name");
            // 生成随机的文件名
            String fileName = UUID.randomUUID().toString();
            //存放图片的具体位置，桌面
//            String imagePath = "/Users/" + username + "/Desktop/ceshi/"+fileName+".png";
//            String imagePath = "/app/nginx/html/imgs/"+fileName+".png";
            String imagePath = "/data/"+fileName+".png";
            // 创建文件输出流
            FileOutputStream fos = new FileOutputStream(new File(imagePath));
            // 将图片数据写入文件
            FileCopyUtils.copy(imageData, fos);
            // 关闭文件输出流
            fos.close();
            return imagePath;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload image.";
        }
    }



    /**
     *  图片审核接口
     *
     * @return*/
    @PostMapping("/imgexaminevx")
    public ResponseResult imgexaminevx(@RequestBody String base64Image) {
        byte[] imageData = decodeBase64Image(base64Image);

        // 调用微信的图像安全检测接口
        String accessToken = redisCache.getCacheObject("accessToken");
        String url = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + accessToken;

        try {
            URL apiURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Connection", "Keep-Alive");

            // 将图像数据写入请求体
            connection.getOutputStream().write(imageData);

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                InputStream inputStream = connection.getInputStream();
                byte[] responseBytes = readAllBytes(inputStream);
                String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                // 处理审核结果并返回
//                return responseBody;
                // 转成Json对象 获取errCode
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                //获取errCode
                int errCode = jsonObject.getIntValue("errcode");
                // 处理审核结果并返回
                if(errCode == 0){
                    return ResponseResult.okResult(true);
                }else{
                    return ResponseResult.errorResult(AppHttpCodeEnum.PICTURE_REVIEW_ERROR);
                }
            } else {
                // 处理错误情况
//                return "Error occurred during image review. Response code: " + responseCode;
                System.out.println("Error occurred during image review. Response code: " + responseCode);
                return ResponseResult.errorResult(AppHttpCodeEnum.PICTURE_REVIEW_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred during image review.");
            // 处理异常情况
            return ResponseResult.errorResult(AppHttpCodeEnum.PICTURE_REVIEW_ERROR);

        }
    }



    public boolean imgexaminevxFun(String base64Image) {
        byte[] imageData = decodeBase64ImageFun(base64Image);
        // 调用微信的图像安全检测接口
        String accessToken = redisCache.getCacheObject("accessToken");
        String url = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + accessToken;

        try {
            URL apiURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Connection", "Keep-Alive");

            // 将图像数据写入请求体
            connection.getOutputStream().write(imageData);

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                InputStream inputStream = connection.getInputStream();
                byte[] responseBytes = readAllBytes(inputStream);
                String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                // 转成Json对象 获取errCode
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                //获取errCode
                int errCode = jsonObject.getIntValue("errcode");
                // 处理审核结果并返回
                if(errCode == 0){
                    return true;
                }else{
                    return false;
                }
            } else {
                // 处理错误情况
                System.out.println("Error occurred during image review. Response code: " + responseCode);
            }
        } catch (IOException e) {
            isProcessing = false;//临时加的怕因为报错导致这个变量变不过来
            e.printStackTrace();
            // 处理异常情况
            System.out.println("Error occurred during image review.");
            return false;
        }
        System.out.println("Error occurred during image review.");
        return false;
    }
    private byte[] decodeBase64ImageFun(String base64Image) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] imageArray = mapper.readValue(base64Image, String[].class);
            String base64Data = imageArray[0]; // 提取数组中的第一个元素
            return DatatypeConverter.parseBase64Binary(base64Data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] decodeBase64Image(String base64Image) {
        String[] parts = base64Image.split(",");
        String base64Data = parts[1];

        return DatatypeConverter.parseBase64Binary(base64Data);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024];
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


}
