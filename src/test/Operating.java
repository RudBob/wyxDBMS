package test;

import action.DataAction;
import action.TableAction;
import action.UserAction;
import bean.*;
import exception.LoginFailException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 匹配各个操作
 */
public class Operating {
    DataAction dataAction = new DataAction();
    TableAction tableAction = new TableAction();
    private static final Pattern PATTERN_INSERT = Pattern.compile("insert\\s+into\\s+(\\w+)(\\(((\\w+,?)+)\\))?\\s+\\w+\\((([^\\)]+,?)+)\\);?");
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("create\\stable\\s(\\w+)\\s?\\(((?:\\s?\\w+\\s\\w+,?)+)\\)\\s?;");
    private static final Pattern PATTERN_ALTER_TABLE_ADD = Pattern.compile("alter\\stable\\s(\\w+)\\sadd\\s(\\w+\\s\\w+)\\s?;");
    private static final Pattern PATTERN_DELETE = Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern PATTERN_UPDATE = Pattern.compile("update\\s(\\w+)\\sset\\s(\\w+\\s?=\\s?[^,\\s]+(?:\\s?,\\s?\\w+\\s?=\\s?[^,\\s]+)*)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern PATTERN_DROP_TABLE = Pattern.compile("drop\\stable\\s(\\w+);");
    private static final Pattern PATTERN_SELECT = Pattern.compile("select\\s(\\*|(?:(?:\\w+(?:\\.\\w+)?)+(?:\\s?,\\s?\\w+(?:\\.\\w+)?)*))\\sfrom\\s(\\w+(?:\\s?,\\s?\\w+)*)(?:\\swhere\\s([^\\;]+\\s?;))?");
    private static final Pattern PATTERN_DELETE_INDEX = Pattern.compile("delete\\sindex\\s(\\w+)\\s?;");
    private static final Pattern PATTERN_GRANT_ADMIN = Pattern.compile("grant\\sadmin\\sto\\s([^;\\s]+)\\s?;");
    private static final Pattern PATTERN_REVOKE_ADMIN = Pattern.compile("revoke\\sadmin\\sfrom\\s([^;\\s]+)\\s?;");

    UserAction userAction = new UserAction();

    void dbms() {
        //bean.User user = new bean.User("user1", "abc");
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

    private void dealCommend(User user) {
        Scanner sc = new Scanner(System.in);
        String cmd;
        while (!"exit".equals(cmd = sc.nextLine())) {
            // 匹配各个命令.
            matchCommend(user, cmd);
        }
    }

    private void matchCommend(User user, String cmd) {
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
        Matcher matcherDeleteIndex = PATTERN_DELETE_INDEX.matcher(cmd);
        while (matcherDeleteIndex.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.deleteIndex(matcherDeleteIndex);
        }
    }

    private void matcherSelect(String cmd) {
        Matcher matcherSelect = PATTERN_SELECT.matcher(cmd);
        while (matcherSelect.find()) {
            dataAction.select(matcherSelect);
        }
    }

    private void matcherDropTable(User user, String cmd) {
        Matcher matcherDropTable = PATTERN_DROP_TABLE.matcher(cmd);
        while (matcherDropTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            tableAction.dropTable(matcherDropTable);
        }
    }

    private void matcherUpdate(User user, String cmd) {
        Matcher matcherUpdate = PATTERN_UPDATE.matcher(cmd);
        while (matcherUpdate.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.update(matcherUpdate);
        }
    }

    private void matcherDelete(User user, String cmd) {
        Matcher matcherDelete = PATTERN_DELETE.matcher(cmd);
        while (matcherDelete.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.delete(matcherDelete);
        }
    }

    private void matcherAlterTable_add(User user, String cmd) {
        Matcher matcherAlterTable_add = PATTERN_ALTER_TABLE_ADD.matcher(cmd);
        while (matcherAlterTable_add.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            tableAction.alterTableAdd(matcherAlterTable_add);
        }
    }

    private void matcherCreateTable(User user, String cmd) {
        Matcher matcherCreateTable = PATTERN_CREATE_TABLE.matcher(cmd);
        while (matcherCreateTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            TableAction.createTable(matcherCreateTable);
        }
    }

    private void matcherInsert(User user, String cmd) {
        Matcher matcherInsert = PATTERN_INSERT.matcher(cmd);
        while (matcherInsert.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dataAction.insert(matcherInsert);
        }
    }

    private void matcherRevokeAdmin(User user, String cmd) {
        Matcher matcherRevokeAdmin = PATTERN_REVOKE_ADMIN.matcher(cmd);
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
        Matcher matcherGrantAdmin = PATTERN_GRANT_ADMIN.matcher(cmd);
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

