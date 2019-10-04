package exception;

/**
 * @ClassName exception.LoginFailException
 * @Description 登陆失败时抛出异常
 * @Author 任耀
 * @Date 2019/9/11 21:17
 * @Version 1.0
 */
public class LoginFailException extends Exception {
    public LoginFailException() {
        this("登录失败！");
    }

    public LoginFailException(String message) {
        super(message);
    }
}
