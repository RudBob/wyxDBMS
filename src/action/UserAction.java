package action;

import bean.User;
import exception.LoginFailException;

/**
 * @ClassName UserAction
 * @Description 用户相关的行为
 * @Author 任耀
 * @Date 2019/9/14 21:38
 * @Version 1.0
 */
public class UserAction {
    public User login() throws LoginFailException {
        User user = User.getUser("user1", "abc");
        if (null == user) {
            throw new LoginFailException();
        } else {
            System.out.println(user.getName() + "登陆成功!");
        }
        return user;
    }
}
