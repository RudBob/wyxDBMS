package exception;

/**
 * @ClassName exception.LoginFailException
 * @Description TODO
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
