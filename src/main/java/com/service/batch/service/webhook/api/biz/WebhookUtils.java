package com.service.batch.service.webhook.api.biz;

public class WebhookUtils {
    //hs 김 0 10
    //hs '김' 0 10
    //hs '김 도' 0 10
    //hs "김 도" 0 10
    //hs 김,도 0 10
    public static String[] parseSplitText(String text) {
        if (text.contains("'") && text.length() == text.replace("'", "").length() + 2) {
            String[] split = text.split("'");

            String command = split[0].trim();
            String searchText = split[1].trim();
            String pageNo = split[2].trim().split(" ")[0];
            String pagePerCnt = split[2].trim().split(" ")[1];

            return new String[]{command, searchText, pageNo, pagePerCnt};
        } else if (text.contains("\"") && text.length() == text.replace("\"", "").length() + 2) {
            String[] split = text.split("\"");

            String command = split[0].trim();
            String searchText = split[1].trim();
            String pageNo = split[2].trim().split(" ")[0];
            String pagePerCnt = split[2].trim().split(" ")[1];

            return new String[]{command, searchText, pageNo, pagePerCnt};
//            return text.split("\"");
        } else {
            if(text.split(" ")[1].startsWith("'") && text.split(" ")[1].endsWith("\"")){
                throw new RuntimeException("parseSplitText error");
            }
            if(text.split(" ")[1].startsWith("\"") && text.split(" ")[1].endsWith("'")){
                throw new RuntimeException("parseSplitText error");
            }
            return text.split(" ");
        }
    }

    public static String[] parseSplitTextTwoWord(String text) {
        if (text.contains("'") && text.length() == text.replace("'", "").length() + 2) {
            String[] split = text.split("'");

            String command = split[0].trim();
            String searchText = split[1].trim();

            return new String[]{command, searchText};
        } else if (text.contains("\"") && text.length() == text.replace("\"", "").length() + 2) {
            String[] split = text.split("\"");

            String command = split[0].trim();
            String searchText = split[1].trim();

            return new String[]{command, searchText};
//            return text.split("\"");
        } else {
            if(text.split(" ")[1].startsWith("'") && text.split(" ")[1].endsWith("\"")){
                throw new RuntimeException("parseSplitText error");
            }
            if(text.split(" ")[1].startsWith("\"") && text.split(" ")[1].endsWith("'")){
                throw new RuntimeException("parseSplitText error");
            }
            return text.split(" ");
        }
    }

    public static int[] getPagingInfo(String[] split) {
        int pageNo = 0, pagePerCnt = 5;
        if (split.length == 3) {
            String[] paging = split[2].trim().split(" ");
            if (paging.length == 2) {
                pageNo = Integer.parseInt(paging[0]) + 1;
                pagePerCnt = Math.min(Integer.parseInt(paging[1]), 10);
            }
        } else if (split.length == 4) {
            pageNo = Integer.parseInt(split[2].trim());
            pagePerCnt = Integer.parseInt(split[3].trim());
        }
        return new int[]{pageNo, pagePerCnt};
    }
}
