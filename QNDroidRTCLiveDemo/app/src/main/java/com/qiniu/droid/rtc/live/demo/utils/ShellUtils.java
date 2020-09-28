package com.qiniu.droid.rtc.live.demo.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ShellUtils {

    private static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * 使用命令行执行命令
     *
     * @param command         待执行命令
     * @param isNeedResultMsg 是否需要返回结果
     * @return 执行结果
     */
    public static CommandResult execCmd(final String command, final boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, isNeedResultMsg);
    }

    /**
     * 使用命令行批量执行命令
     *
     * @param commands        待执行命令
     * @param isNeedResultMsg 是否需要返回结果
     * @return 执行结果
     */
    public static CommandResult execCmd(final List<String> commands, final boolean isNeedResultMsg) {
        return execCmd(commands == null ? null : commands.toArray(new String[]{}), isNeedResultMsg);
    }

    /**
     * 使用命令行批量执行命令
     *
     * @param commands        待执行命令
     * @param isNeedResultMsg 是否需要返回结果
     * @return 执行结果
     */
    public static CommandResult execCmd(final String[] commands, final boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, "", "");
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(LINE_SEP);
                os.flush();
            }
            os.writeBytes("exit" + LINE_SEP);
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8")
                );
                errorResult = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), "UTF-8")
                );
                String line;
                if ((line = successResult.readLine()) != null) {
                    successMsg.append(line);
                    while ((line = successResult.readLine()) != null) {
                        successMsg.append(LINE_SEP).append(line);
                    }
                }
                if ((line = errorResult.readLine()) != null) {
                    errorMsg.append(line);
                    while ((line = errorResult.readLine()) != null) {
                        errorMsg.append(LINE_SEP).append(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (successResult != null) {
                    successResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(
                result,
                successMsg == null ? "" : successMsg.toString(),
                errorMsg == null ? "" : errorMsg.toString()
        );
    }

    /**
     * 命令行执行结果
     */
    public static class CommandResult {
        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(final int result, final String successMsg, final String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "result: " + result + "\n" +
                    "successMsg: " + successMsg + "\n" +
                    "errorMsg: " + errorMsg;
        }
    }
}
