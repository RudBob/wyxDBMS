package main;

import action.DataAction;
import action.TableAction;
import action.UserAction;
import bean.*;
import exception.LoginFailException;

import java.util.*;
import java.util.regex.Matcher;

import util.PatternModelStr;

/**
 * 匹配各个操作
 */
public class Operations {
    private DataAction dataAction = new DataAction();
    private TableAction tableAction = new TableAction();
    private UserAction userAction = new UserAction();

    /**
     * 程序的入口.
     */
    public void dbms() {
        //User user = new bean.User("user1", "abc");
        User user = null;
        try {
            user = userAction.login();
        } catch (LoginFailException e) {
            e.printStackTrace();
            return;
        }
        //bean.User.grant(user.getName(), bean.User.READ_ONLY);
        //user.grant(bean.User.READ_ONLY);

        // 进入默认的表中
        user.intoDefaultTable();

        // 开始接收用户的输入，并处理.
        dealCommend(user);

    }

    /**
     * 处理用户输入的命令。
     *
     * @param user 用户信息，判断用户是否有权限进行部分操作。
     */
    private void dealCommend(User user) {
        Scanner sc = new Scanner(System.in);
        String cmd;
        while (!"exit".equals(cmd = sc.nextLine())) {
            // 匹配各个命令.
            matchCommend(user, cmd);
        }
    }

    /**
     * 挨个匹配常见命令，因为要判断用户的权限，而且应该保留操作日志.
     *
     * @param user 用户信息
     * @param cmd  用户输入的命令。
     */
    private void matchCommend(User user, String cmd) {
        // 因为存在嵌套子句，所以每一种可能性都要进行匹配
        // 例: update table1 set name = 'zhangsan' where select name from table where name = 'lisi';
        matchGrantAdmin(user, cmd);

        matcherRevokeAdmin(user, cmd);

        matcherInsert(user, cmd);

        matcherCreateTable(user, cmd);

        matcherAlterTable_add(user, cmd);

        matcherDelete(user, cmd);

        matcherUpdate(user, cmd);

        matcherDropTable(user, cmd);

        matcherSelect(cmd);

        matcherDeleteIndex(user, cmd);
    }

    private void matcherDeleteIndex(User user, String cmd) {
        Matcher matcherDeleteIndex = PatternModelStr.PATTERN_DELETE_INDEX.matcher(cmd);
        // 因为可能是嵌套子句，所以要多次匹配.
        while (matcherDeleteIndex.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.deleteIndex(matcherDeleteIndex);
        }
    }

    private void matcherSelect(String cmd) {
        Matcher matcherSelect = PatternModelStr.PATTERN_SELECT.matcher(cmd);
        while (matcherSelect.find()) {
            dataAction.select(matcherSelect);
        }
    }

    private void matcherDropTable(User user, String cmd) {
        Matcher matcherDropTable = PatternModelStr.PATTERN_DROP_TABLE.matcher(cmd);
        while (matcherDropTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            tableAction.dropTable(matcherDropTable);
        }
    }

    private void matcherUpdate(User user, String cmd) {
        Matcher matcherUpdate = PatternModelStr.PATTERN_UPDATE.matcher(cmd);
        while (matcherUpdate.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.update(matcherUpdate);
        }
    }

    private void matcherDelete(User user, String cmd) {
        Matcher matcherDelete = PatternModelStr.PATTERN_DELETE.matcher(cmd);
        while (matcherDelete.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.delete(matcherDelete);
        }
    }

    private void matcherAlterTable_add(User user, String cmd) {
        Matcher matcherAlterTable_add = PatternModelStr.PATTERN_ALTER_TABLE_ADD.matcher(cmd);
        while (matcherAlterTable_add.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            tableAction.alterTableAdd(matcherAlterTable_add);
        }
    }

    private void matcherCreateTable(User user, String cmd) {
        Matcher matcherCreateTable = PatternModelStr.PATTERN_CREATE_TABLE.matcher(cmd);
        while (matcherCreateTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            TableAction.createTable(matcherCreateTable);
        }
    }

    private void matcherInsert(User user, String cmd) {
        Matcher matcherInsert = PatternModelStr.PATTERN_INSERT.matcher(cmd);
        while (matcherInsert.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.insert(matcherInsert);
        }
    }

    private void matcherRevokeAdmin(User user, String cmd) {
        Matcher matcherRevokeAdmin = PatternModelStr.PATTERN_REVOKE_ADMIN.matcher(cmd);
        while (matcherRevokeAdmin.find()) {
            User revokeUser = User.getUser(matcherRevokeAdmin.group(1));
            if (null == revokeUser) {
                System.out.println("取消授权失败!");
            }
            if (user.getName().equals(revokeUser.getName())) {
                //如果是当前操作的用户，就直接更改当前用户权限
                user.grant(User.READ_ONLY);
                System.out.println("用户:" + user.getName() + "已取消授权！");
            } else {
                revokeUser.grant(User.READ_ONLY);
                System.out.println("用户:" + revokeUser.getName() + "已取消授权！");
            }
        }
    }

    private void matchGrantAdmin(User user, String cmd) {
        Matcher matcherGrantAdmin = PatternModelStr.PATTERN_GRANT_ADMIN.matcher(cmd);
        while (matcherGrantAdmin.find()) {
            User grantUser = User.getUser(matcherGrantAdmin.group(1));
            if (null == grantUser) {
                System.out.println("授权失败！");
            } else if (user.getName().equals(grantUser.getName())) {
                //如果是当前操作的用户，就直接更改当前用户权限
                user.grant(User.ADMIN);
                System.out.println("用户:" + user.getName() + "授权成功！");
            } else {
                grantUser.grant(User.ADMIN);
                System.out.println("用户:" + grantUser.getName() + "授权成功!");
            }
        }
    }
}

